# Url shortener service

Сервис сокращателя ссылок

# Использованные технологии

* [Spring Boot](https://spring.io/projects/spring-boot) – как основной фреймворк
* [PostgreSQL](https://www.postgresql.org/) – как основная реляционная база данных
* [Redis](https://redis.io/) – как кэш и очередь сообщений через pub/sub
* [testcontainers](https://testcontainers.com/) – для изолированного тестирования с базой данных
* [Liquibase](https://www.liquibase.org/) – для ведения миграций схемы БД
* [Gradle](https://gradle.org/) – как система сборки приложения

# База данных

* База поднимается в отдельном сервисе [infra](../infra)
* Redis также поднимается в [infra](../infra)
* Liquibase сам накатывает нужные миграции на PostgreSQL при старте приложения
* В тестах используется [testcontainers](https://testcontainers.com/), в котором тоже запускается отдельный инстанс
  postgres
* В коде продемонстрирована работа с JPA (Hibernate)

# Как начать работу с микросервисом?

1. Сначала нужно склонировать родительский репозиторий
```shell
git clone https://github.com/CorporationX/CorporationX.git
```

2. Перейти в нужный микросервис

# Как запустить локально?

Сначала нужно развернуть базу данных из директории [infra](../infra)

Далее собрать gradle проект

```shell
# Нужно запустить из корневой директории, где лежит build.gradle.kts
gradle build
```

Запустить JAR-файл

```shell
java -jar build/libs/ServiceTemplate-1.0.jar
```

Но рекомендуется все это делать сделать через IDE

# Код
Реализована логика системы постов, лайков, комментариев
Схема представлена ниже:

<img width="3476" height="1029" alt="image" src="https://github.com/user-attachments/assets/5b9c78d8-c07f-421f-99a9-7c7ce45de2b3" />

# Тесты
Используемые инструменты тестирования:
* SpringBootTest
* MockMvc
* Testcontainers
* AssertJ
* JUnit5
* Parameterized tests

