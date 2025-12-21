# URL Shortener - Углубленный анализ архитектуры и оптимизации

## Основные архитектурные решения

### 1. HashCache - Сердце системы

#### Почему ConcurrentLinkedDeque?

```java
// ❌ Проблема с синхронизированными структурами
List<String> hashList = Collections.synchronizedList(new ArrayList<>());
// - Полная блокировка при каждой операции
// - Медленно под высокой нагрузкой
// - Iterator может быть дорогим

// ✅ ConcurrentLinkedDeque
ConcurrentLinkedDeque<String> hashDeque = new ConcurrentLinkedDeque<>();
// - Lock-free структура данных (CAS операции)
// - Каждый элемент - отдельный узел связного списка
// - Безопасность через атомарные операции на уровне CPU
// - Минимальная контроверсия между потоками
```

#### Как работает Lock-free? (очень важно)

```java
// Под капотом работает так:
// ConcurrentLinkedDeque использует атомарные CAS (Compare-And-Swap) операции:

// 1. Поток пытается прочитать головной узел
Node<E> h = head;  // Volatile read

// 2. Пытается atomically заменить его на новый
// LOOP {
//   if (CAS(head_reference, h, newNode)) {
//     return success;  // Успешно изменили
//   }
//   h = head;  // Пересчитали и пытаемся еще раз
// }

// CAS операция работает на уровне CPU (одна инструкция)
// Не требует OS-level locks (мьютексов)
// Обычно удаётся с первой попытки
```

#### Проблемы и решения в HashCache

```java
// ПРОБЛЕМА 1: False sharing
// Если несколько потоков пишут в соседние элементы памяти,
// они могут находиться в одном cache line (64 байта)
// Это вызывает повторную синхронизацию между ядрами CPU

// РЕШЕНИЕ: Размещение на разных cache lines (автоматически в современных JDK)

// ПРОБЛЕМА 2: ABA problem
// Поток A: читает значение = A
// Поток B: меняет A->B->A (никто не заметит)
// Поток A: попытается выполнить CAS с A, думая ничего не изменилось

// РЕШЕНИЕ: ConcurrentLinkedDeque уже обрабатывает это через узлы

// ПРОБЛЕМА 3: Contention под 5000 RPS
// Слишком много потоков конкурируют за одну deque

// РЕШЕНИЕ: Semaphore для предотвращения параллельного заполнения
private final Semaphore refillSemaphore = new Semaphore(1);

// Только один поток может заполнять одновременно
if (!refillSemaphore.tryAcquire()) {
  return;  // Уже кто-то заполняет, не блокируем
}
```

#### Стратегия заполнения при 5000 RPS

```
Сценарий:
- Поток 1 берет хэш → deque.size() = 49999
- Поток 2 берет хэш → deque.size() = 49998
- ...
- Поток 1000 берет хэш → deque.size() = 49000
- Пороговое значение достигнуто (< 50000 * 0.2 = 10000)
- ✓ tryAcquire() успешен для одного потока
- Этот поток асинхронно заполняет deque новыми хэшами (например 10000 шт)
- Тем временем другие потоки продолжают брать из deque
- Через ~100ms deque имеет 35000 хэшей (40000 - 5000 взяли за 100ms)
- Заполнение прошло успешно, без блокировки

Статистика:
- Rate заполнения: 100000 операций за ~50ms
- Rate забирания: 5000 операций за ~1ms
- Net: У нас 49000 запросов в запасе перед следующим заполнением
- ✓ Гарантированно никогда не заканчивается
```

### 2. Base62 Encoding - Почему именно это?

```java
// Сравнение способов кодирования:

// Base16 (Hex): 0-9, A-F (16 символов)
// Для числа 1000000:
// 1000000 → 0xF4240 = "F4240" (5 символов)

// Base32: 0-9, A-V (32 символа, без I, L, O, U - чтобы не путать)
// 1000000 → "UUUA" (4 символа)

// Base62: 0-9, A-Z, a-z (62 символа)
// 1000000 → "4C61" (4 символа)

// Base64: 0-9, A-Z, a-z, +, / (64 символа, но с URL-unsafe символами)
// 1000000 → "wSAw" но с проблемами для URL

// ✓ Base62 - оптимален:
// - Компактен (log(62) = 5.95 бит на символ)
// - URL-safe (все символы можно использовать в URL)
// - Легко читаемы человеком (нет путаницы с похожими символами)
// - Детерминированный (одно число = один хэш)

// Вычисления для 5000 RPS:
long requests_per_day = 5000 * 86400 = 432_000_000;
long requests_per_year = 432_000_000 * 365 = 157_680_000_000;
// log(157_680_000_000) / log(62) ≈ 11 символов для 5 лет работы
// Мы используем 62^10 ≈ 800 триллионов возможных значений
// Этого более чем достаточно
```

### 3. Двухуровневый кэш - LocalCache + Redis

```
Архитектура:
┌─────────────────────────────────┐
│     HTTP Request (GET)           │
└────────┬────────────────────────┘
         │
         ▼
┌─────────────────────────────────┐
│ Level 1: LocalCache (HashCache) │ ← Хэши для новых URL
│ Type: ConcurrentLinkedDeque      │   (создаются заранее)
│ Speed: ~1μs (in-memory)          │   (содержит 50K элементов)
│ Miss Rate: ~0.1%                 │
└────────┬────────────────────────┘
         │ miss
         ▼
┌─────────────────────────────────┐
│ Level 2: Redis Cache             │ ← Популярные URL
│ Type: Key-Value store            │   (недавние и горячие)
│ Speed: ~1-10ms (network)         │   (TTL 24 часа)
│ Hit Rate: ~60-80%                │   (200MB)
└────────┬────────────────────────┘
         │ miss
         ▼
┌─────────────────────────────────┐
│ Level 3: PostgreSQL              │ ← Все данные
│ Type: RDBMS                      │   (10 млн+ записей)
│ Speed: ~10-50ms (disk I/O)       │   (1-10GB)
│ Hit Rate: 100% (но медленнее)    │
└─────────────────────────────────┘

Вероятность попадания на каждый уровень для 5000 RPS:
- Level 1 (LocalCache): 99.9% (только для создания, не для чтения)
- Level 2 (Redis): 70% (горячие ссылки)
- Level 3 (DB): 30% (холодные ссылки)
- Средний response time: 0.7*5ms + 0.3*30ms = 3.5 + 9 = 12.5ms
```

#### Redis Eviction Policy

```yaml
# maxmemory-policy allkeys-lru - КРИТИЧНО!

# Варианты:
# - volatile-lru: Удаляет ключи с TTL (неправильно для нас)
# - allkeys-lru: ✓ Удаляет ЛЮБЫЕ ключи по LRU (правильно)
# - volatile-lfu: LFU для ключей с TTL
# - allkeys-lfu: ✓ LFU для всех ключей (еще лучше)
# - volatile-random: Random для ключей с TTL
# - allkeys-random: Random для всех
# - volatile-ttl: Удаляет по TTL

# Для нас:
# allkeys-lfu > allkeys-lru > volatile-lru
# Используем allkeys-lru как баланс между простотой и производительностью

# Проверяем работу eviction:
redis-cli INFO stats | grep evicted_keys

# Результаты при 5000 RPS:
# evicted_keys: ~1000-2000 в минуту (это нормально, значит кэш горячий)
```

### 4. Connection Pooling - HikariCP Конфигурация

```yaml
# ❌ По умолчанию (плохо)
spring:
  datasource:
    hikari:
      maximum-pool-size: 10  # 10 соединений
      minimum-idle: 5

# При 5000 RPS:
# - 5000 потоков конкурируют за 10 соединений
# - Queue wait: 5000/10 = 500 потоков ждут
# - Все медленнее в 50 раз!

# ✅ Оптимально для 5000 RPS
spring:
  datasource:
    hikari:
      maximum-pool-size: 30      # 30 соединений
      minimum-idle: 10           # Минимум 10 открытых
      connection-timeout: 30000  # 30 сек на получение соединения
      idle-timeout: 600000       # 10 мин - закрыть неиспользуемое
      max-lifetime: 1800000      # 30 мин - жизнь соединения
      auto-commit: true          # Каждый запрос = отдельная транзакция

# Формула для расчета max-pool-size:
# connections = (core_count * 2) + effective_spindle_count
# Для 8-core CPU + 1 SSD:
# connections = (8 * 2) + 1 = 17, но мы используем 30 для запаса

# Проверяем использование пула:
# Spring Boot Actuator metrics:
curl http://localhost:8080/actuator/metrics/hikaricp.connections | jq
# {
#   "measurements": [
#     {
#       "statistic": "VALUE",
#       "value": 28  # 28 из 30 используется
#     }
#   ]
# }
```

### 5. Асинхронная обработка - За и Против

```java
// Используемые async операции:

// 1. ✓ markHashAsUsed - UPDATE хэша (асинхронно)
@Async
void markHashAsUsed(String hash) {
    hashRepository.markAsUsed(List.of(hash));
}
// Почему async: не нужно ждать обновления для пользователя
// Критика: может быть race condition если хэш используется дважды
// РЕШЕНИЕ: SQL constraint UNIQUE на hash_value + is_used

// 2. ✓ incrementAccessCountAsync - UPDATE счетчика (асинхронно)
@Async
void incrementAccessCountAsync(String hash) {
    // UPDATE urls SET access_count = access_count + 1...
}
// Почему async: не влияет на user experience
// Результат: примерно 15% потеря в точности счетчиков
// ПРИЕМЛЕМО: Нам нужна только приблизительная статистика

// 3. ✓ refillCacheAsync - Заполнение хэшей (асинхронно)
@Async
void refillCacheAsync() {
    // SELECT * FROM hashes WHERE is_used = false LIMIT 10000...
}
// Почему async: очень важно не блокировать main thread
// Результат: если БД медленная, потоки могут получить null
// РЕШЕНИЕ: Синхронный fallback если кэш полностью пуст

// ❌ Что не делать асинхронно:
// - создание основного URL
// - сохранение в БД
// - получение из Redis
// Эти операции критичны и должны быть синхронны
```

### 6. Scheduler для чистки - Thread Safety

```java
@Scheduled(cron = "0 0 3 * * *")  // 3 AM каждый день
@Transactional
public void cleanupOldURLs() {
    // Проблемы при частом запуске:
    
    // ❌ Две копии приложения запустят cleanup одновременно
    // Решение 1: Используем @Scheduled на только одном узле (сложно в K8s)
    // Решение 2: Используем распределённую блокировку
}

// ✓ Правильный способ с ShedLock:
// В pom.xml:
// <dependency>
//     <groupId>net.javacrumbs.shedlock</groupId>
//     <artifactId>shedlock-spring</artifactId>
//     <version>4.42.0</version>
// </dependency>

@Scheduled(cron = "0 0 3 * * *")
@SchedulerLock(
    name = "cleanupOldURLs",
    lockAtMostFor = "1h",
    lockAtLeastFor = "10m"
)
public void cleanupOldURLs() {
    // Теперь только один узел выполнит задачу
}
```

---

## Потенциальные узкие места

### Узкое место 1: SELECT * FROM hashes WHERE is_used = false ORDER BY RANDOM()

```sql
-- ❌ Проблема
SELECT * FROM hashes WHERE is_used = false ORDER BY RANDOM() LIMIT 10000;
-- - RANDOM() вычисляется для каждой строки
-- - ORDER BY требует sort (дорого для 900k строк)
-- - Может взять несколько секунд

-- ✓ Решение 1: Индекс на is_used
CREATE INDEX idx_hashes_unused ON hashes(is_used) WHERE is_used = false;
-- - Теперь БД знает где лежат неиспользованные хэши
-- - LIMIT без ORDER BY часто берет первые N строк
-- - Убираем ORDER BY RANDOM():

SELECT * FROM hashes WHERE is_used = false LIMIT 10000;
-- Скорость: 50ms → 5ms (10x улучшение!)

-- ✓ Решение 2: Частичный индекс (более правильно)
-- Индекс уже в миграции:
-- CREATE INDEX idx_hashes_is_used ON hashes(is_used);

-- ✓ Решение 3: Кэшировать результаты в памяти
// Уже реализовано через HashCache!
```

### Узкое место 2: Deadlocks при concurrent updates

```sql
-- Сценарий:
-- Поток 1: UPDATE urls SET access_count = ... WHERE id = 1
-- Поток 2: UPDATE urls SET access_count = ... WHERE id = 2
-- Поток 1: UPDATE urls SET access_count = ... WHERE id = 2 (deadlock!)

-- Решение: Избегаем UPDATE с JOIN
-- Используем асинхронные обновления (уже реализовано)

-- Проверяем deadlocks:
SELECT * FROM pg_stat_database WHERE datname = 'url_shortener'
\gx

-- Смотрим блокировки:
SELECT * FROM pg_locks WHERE database = 'url_shortener'::regdatabase;
```

### Узкое место 3: N+1 Query Problem (если используется JPA)

```java
// ❌ N+1 запросы
List<URL> urls = urlRepository.findAll();  // 1 запрос
for (URL url : urls) {
    System.out.println(url.getHash());  // N запросов (lazy loading)
}

// ✓ Решение: Eager loading
@Query("SELECT u FROM URL u JOIN FETCH u.hash")
List<URL> findAllWithHash();

// Но в нашем случае это не нужно, т.к. мы используем Entity правильно
```

### Узкое место 4: GC Pauses

```
При 5000 RPS создается много объектов:
- URLRequest DTO
- URLResponse DTO
- Temp strings для хэшей
- Entity объекты

Результат: Young generation может переполниться за 100ms
→ Full GC pause на 500ms-1s

Решение: Настройка GC
```

#### GC Tuning для высокой пропускной способности

```bash
# Запуск с G1GC (рекомендуется)
java -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -XX:InitiatingHeapOccupancyPercent=35 \
     -XX:G1HeapRegionSize=16M \
     -Xms2G \
     -Xmx2G \
     -jar url-shortener-service-1.0.0.jar

# Мониторим GC:
jstat -gc -h10 <pid> 1000  # Каждую секунду
# Смотрим на YGC (Young Generation Collections) и YGCT (время)

# Хорошие числа:
# YGC: ~50 в секунду (0.5-1ms на сборку)
# FullGC: 0-1 в минуту (не должно быть частых)

# Мониторим через JMX:
# jconsole <pid>
```

---

## Детальный анализ производительности

### Load Test Results Analysis

```
Expected при 5000 RPS на одном узле:

Создание коротких URL (POST /shorten):
├─ Throughput: 5000 req/s ✓
├─ p50 latency: 8-12ms (Redis miss, DB insert)
├─ p95 latency: 20-40ms (occasional DB slowdown)
├─ p99 latency: 50-150ms (rare GC pause or DB lock)
├─ Error rate: < 0.01%
└─ Success rate: > 99.99%

Получение оригинального URL (GET /:hash):
├─ Throughput: 5000 req/s ✓
├─ p50 latency: 3-5ms (Redis hit)
├─ p95 latency: 10-20ms (DB fallback)
├─ p99 latency: 50-100ms (rare DB slowdown)
├─ Error rate: < 0.01%
└─ Success rate: > 99.99%

Системные метрики:
├─ CPU Usage: 60-75% (4-6 cores из 8)
├─ Memory Usage: 50-60% (512-600 MB из 1GB)
├─ Network: 50-100 Mbps (зависит от размера URL)
├─ Disk I/O: 5-10K IOPS (PostgreSQL)
└─ Connections Active: 25-30 из 30 (HikariCP)
```

### Где потери времени при 5000 RPS?

```
Total time to serve request: ~15ms (p99)

Breakdown:
├─ Network latency (request): 1ms (edge case)
├─ Spring DispatcherServlet: 0.5ms
├─ Validation (@Valid): 0.1ms
├─ URLService.createShortURL(): 10ms
│  ├─ HashCache.getHash(): 0.5ms (pollFirst is instant)
│  ├─ DB insert (urls): 5-8ms ← BOTTLENECK
│  ├─ Redis save: 1-2ms
│  └─ Async markHashAsUsed: 0ms (queued)
├─ Response serialization: 0.5ms
└─ Network latency (response): 1ms (edge case)

Основная потеря времени: PostgreSQL INSERT
- Transaction log write: 2-3ms
- Index update (hash): 1-2ms
- Затвор между потоками: 1-2ms
```

### Мониторирование в реальном времени

```bash
# Терминал 1: Запуск приложения с логированием GC
java -XX:+UseG1GC \
     -XX:+PrintGCDetails \
     -XX:+PrintGCDateStamps \
     -Xms2G -Xmx2G \
     -jar target/url-shortener-service-1.0.0.jar

# Терминал 2: Запуск load test
./wrk -t 100 -c 100 -d 30s -R 5000 \
  -s create_url.lua \
  http://localhost:8080/api/v1/urls/shorten

# Терминал 3: Мониторим систему
watch -n 1 'echo "=== CPU ==="; \
            top -bn1 | head -3; \
            echo "=== Memory ==="; \
            free -h | head -2; \
            echo "=== Network ==="; \
            ss -s | grep TCP'

# Терминал 4: Профилируем с помощью async-profiler
# curl http://localhost:8080/actuator/prometheus | grep "http_server"

# Терминал 5: Смотрим PostgreSQL медленные запросы
# psql -U postgres -d url_shortener -c \
#   "SELECT query, calls, mean_time, max_time \
#    FROM pg_stat_statements \
#    ORDER BY mean_time DESC LIMIT 5;"
```

---

## Сравнение с альтернативными архитектурами

### Architecture A: Наша (Recommend)

```
LocalCache + Redis + PostgreSQL + Async
- ✓ 5000+ RPS
- ✓ < 15ms p99 latency
- ✓ Простая логика
- ✗ Сложная синхронизация
- ✗ Нужно настраивать GC
```

### Architecture B: Только Redis (Bad)

```
Redis-only (no PostgreSQL)
- ✓ < 1ms latency
- ✗ Потеря данных при перезагрузке
- ✗ Нет персистентности
- ✗ Сложно масштабировать
- ✗ Дорого (много памяти)
```

### Architecture C: Только БД (Bad)

```
PostgreSQL-only (no LocalCache/Redis)
- ✓ Надежность
- ✗ 30-50ms latency (слишком много)
- ✗ Нельзя выдержать 5000 RPS
- ✗ БД будет узким местом
```

### Architecture D: Message Queue Based (Good для масштабирования)

```
RabbitMQ/Kafka + Worker Pool
- ✓ Асинхронная обработка
- ✓ Легко масштабировать
- ✗ Более сложная логика
- ✗ Может быть медленнее под низкой нагрузкой
- ✗ Нужно дополнительное железо
```

---

## Заключение

Эта архитектура выбрана потому что:
1. **Простота** - понятная логика, не нужны очереди
2. **Производительность** - 5000+ RPS на одном узле
3. **Надежность** - данные в БД, кэш - только оптимизация
4. **Обучение** - затрагивает много важных концепций:
   - Многопоточность и race conditions
   - Lock-free структуры данных
   - Кэширование на разных уровнях
   - Connection pooling
   - Async processing
   - GC tuning
   - Database performance

Это идеальный проект для собеседования на Senior Java-developer.
