# Makefile для docker-compose инфраструктуры

# Файл docker-compose
COMPOSE_FILE = docker-compose.yaml

# Загружаем переменные из .env
include .env
export $(shell sed 's/=.*//' .env)

.PHONY: up build down restart logs ps shell clean prune

## 🚀 Поднять все контейнеры в фоне
up:
	@echo "🚀 Поднимаем инфраструктуру..."
	docker compose -f $(COMPOSE_FILE) up -d

## 🔧 Пересобрать образы и поднять
build:
	@echo "🔧 Пересборка контейнеров..."
	docker compose -f $(COMPOSE_FILE) up -d --build

## 🧨 Остановить все контейнеры
down:
	@echo "🧨 Останавливаем контейнеры..."
	docker compose -f $(COMPOSE_FILE) down

## 🔁 Перезапуск контейнеров
restart:
	@echo "🔁 Перезапуск контейнеров..."
	docker compose -f $(COMPOSE_FILE) down
	docker compose -f $(COMPOSE_FILE) up -d

## 📜 Логи контейнеров
logs:
	docker compose -f $(COMPOSE_FILE) logs -f --tail=100

## 🧩 Список контейнеров
ps:
	docker compose -f $(COMPOSE_FILE) ps

## 🐚 Зайти в контейнер
# Пример: make shell service=minio
shell:
	@docker exec -it $(service) /bin/sh

## 🧹 Полная очистка (контейнеры + volume'ы)
clean:
	@echo "🧹 Удаляем контейнеры и volume'ы..."
	docker compose -f $(COMPOSE_FILE) down -v

## 🔥 Глобальная очистка Docker (осторожно!)
prune:
	@echo "🔥 Полная очистка Docker (все неиспользуемые образы, volume'ы и сети)..."
	docker system prune -a --volumes -f
