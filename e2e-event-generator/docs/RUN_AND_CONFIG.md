# Запуск и конфигурация E2E Event Generator

Документ описывает сборку, запуск генератора и все доступные параметры конфигурации.

Источник defaults: [`src/main/resources/application.yml`](../src/main/resources/application.yml).

---

## 1. Требования

| Компонент | Версия / требование |
|-----------|---------------------|
| Java (runtime и сборка) | **11** (JVM target 11) |
| Gradle | **6.8** (wrapper) |
| Kafka | Доступный bootstrap; topics создаются заранее или auto-create на брокере |
| Flow CE headers | `ce_type`, `ce_source`, `ce_specversion` — заполнить перед интеграцией с реальным Flow |

Генератор **не формирует `E2E_ID`** — это ответственность BAMN Correlation Engine.

### Java 11

```bash
# Пример: пользовательская установка Temurin 11
source scripts/env-java11.sh
java -version   # должен показать 11.x
```

Для Gradle можно зафиксировать JDK в [`gradle.properties`](../gradle.properties):

```properties
org.gradle.java.home=/path/to/jdk-11
```

---

## 2. Сборка

Из каталога `e2e-event-generator/`:

```bash
./gradlew clean check bootJar
```

Артефакт:

```text
build/libs/e2e-event-generator.jar
```

Скрипт-обёртка: [`scripts/build.sh`](../scripts/build.sh).

---

## 3. Способы запуска

### 3.1 JAR (основной)

```bash
java -jar build/libs/e2e-event-generator.jar
```

С явными параметрами:

```bash
java -jar build/libs/e2e-event-generator.jar \
  --generator.command=run \
  --generator.scenario=full-poc \
  --generator.mode=BATCH \
  --generator.seed=1001
```

Через переменные окружения (см. раздел 4):

```bash
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export GENERATOR_SCENARIO=happy-path
java -jar build/libs/e2e-event-generator.jar
```

Скрипт: [`scripts/run-jar.sh`](../scripts/run-jar.sh).

### 3.2 Gradle (без JAR)

```bash
./gradlew bootRun \
  --args='--generator.scenario=negative --generator.mode=BATCH'
```

### 3.3 Docker

Сборка (нужны утверждённые internal Java 11 images):

```bash
docker build \
  --build-arg BUILD_JDK11_IMAGE=<approved-jdk11-image> \
  --build-arg RUNTIME_JRE11_IMAGE=<approved-jre11-image> \
  -t e2e-event-generator:0.1.0 .
```

Запуск:

```bash
docker run --rm \
  -e KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
  -e GENERATOR_SCENARIO=full-poc \
  -e GENERATOR_MODE=BATCH \
  -e FLOW_MODEL_TOPIC=model-update-event \
  -e FLOW_INSTANCE_TOPIC=instance-update-event \
  -e LEGACY_TOPIC=legacy-events \
  e2e-event-generator:0.1.0
```

Скрипт: [`scripts/build-docker.sh`](../scripts/build-docker.sh).

### 3.4 docker-compose

[`docker-compose.yml`](../docker-compose.yml) — заготовка с placeholder Kafka image.

Перед запуском задать:

```bash
export KAFKA_IMAGE=<approved-kafka-image>
export BUILD_JDK11_IMAGE=<approved-jdk11-image>
export RUNTIME_JRE11_IMAGE=<approved-jre11-image>
docker compose up --build
```

---

## 4. Параметры приложения

Конфигурация задаётся тремя способами (приоритет Spring Boot):

1. Аргументы командной строки: `--generator.scenario=happy-path`
2. Переменные окружения: `GENERATOR_SCENARIO=happy-path`
3. `application.yml` (defaults)

Kebab-case в YAML ↔ UPPER_SNAKE_CASE в env ↔ dot-notation в CLI.

### 4.1 Generator (`generator.*`)

| YAML / CLI | Переменная окружения | Default | Описание |
|------------|---------------------|---------|----------|
| `generator.command` | `GENERATOR_COMMAND` | `run` | Команда запуска. Поддерживается только **`run`**. |
| `generator.scenario` | `GENERATOR_SCENARIO` | `full-poc` | Имя YAML-сценария **без** расширения (файл: `src/main/resources/scenarios/<name>.yml`). |
| `generator.mode` | `GENERATOR_MODE` | `BATCH` | **`BATCH`** — один прогон сценария и выход; **`CONTINUOUS`** — повторный прогон в цикле. |
| `generator.seed` | `GENERATOR_SEED` | `1001` | Deterministic seed для `ce_id`, instance IDs и timestamps. Если `> 0`, перекрывает `seed` из YAML. |
| `generator.continuous-delay-ms` | `GENERATOR_CONTINUOUS_DELAY_MS` | `5000` | Пауза (мс) между итерациями в режиме `CONTINUOUS`. |

#### Команда `run`

При старте генератор:

1. Публикует `ProcessModelEvent` для каждого Flow-процесса из сценария → **model topic**.
2. Разворачивает `cases` из YAML в план logical cases.
3. Для каждого case генерирует и публикует instance/legacy snapshots → **instance** / **legacy** topics.

### 4.2 Kafka topics (`generator.kafka.topics.*`)

| YAML / CLI | Переменная окружения | Default | Record key |
|------------|---------------------|---------|------------|
| `generator.kafka.topics.flow-models` | `FLOW_MODEL_TOPIC` | `model-update-event` | `processDefinitionId` |
| `generator.kafka.topics.flow-instances` | `FLOW_INSTANCE_TOPIC` | `instance-update-event` | `id` (instance) |
| `generator.kafka.topics.legacy-instances` | `LEGACY_TOPIC` | `legacy-events` | `id` (instance) |

Topics **независимы** — model / instance / legacy не смешиваются (см. [ADR-001](adr/ADR-001-flow-topics.md)).

### 4.3 CloudEvents headers (`generator.headers.*`)

| YAML / CLI | Переменная окружения | Default | Применяется к |
|------------|---------------------|---------|---------------|
| `generator.headers.flow-model.ce-type` | `FLOW_MODEL_CE_TYPE` | *(пусто)* | `ProcessModelEvent` |
| `generator.headers.flow-instance.ce-type` | `FLOW_INSTANCE_CE_TYPE` | *(пусто)* | `ProcessInstanceEvent` (Flow) |
| `generator.headers.flow-model.ce-source` | `FLOW_CE_SOURCE` | *(пусто)* | Flow model + instance |
| `generator.headers.flow-instance.ce-source` | `FLOW_CE_SOURCE` | *(пусто)* | Flow model + instance |
| `generator.headers.legacy-instance.ce-type` | `LEGACY_INSTANCE_CE_TYPE` | `poc.legacy.instance` | Legacy instance |
| `generator.headers.legacy-instance.ce-source` | `LEGACY_CE_SOURCE` | `poc.legacy.generator` | Legacy instance |
| `generator.headers.ce-specversion` | `CE_SPEC_VERSION` | *(пусто)* | Все потоки |

На каждой записи также выставляются (генерируются кодом):

| Header | Источник |
|--------|----------|
| `ce_time` | Deterministic timestamp из seed/case/snapshot |
| `ce_id` | Deterministic UUID из seed (кроме case `duplicateEvent`) |

> Production-значения `ce_type`, `ce_source`, `ce_specversion` для Flow **не зафиксированы** в контракте — заполните из реальных примеров Flow перед интеграционным тестированием. См. [CONTRACT_NOTES.md](CONTRACT_NOTES.md).

### 4.4 Spring Kafka (`spring.kafka.*`)

| YAML / CLI | Переменная окружения | Default | Описание |
|------------|---------------------|---------|----------|
| `spring.kafka.bootstrap-servers` | `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Адрес Kafka broker |

Producer (зашито в `application.yml`, менять только при необходимости):

- `enable.idempotence: true`
- `acks: all`
- serializers: `String` / `String`
- payload: JSON; CloudEvents metadata — **Kafka headers**

### 4.5 Actuator

Подключён `spring-boot-starter-actuator` (defaults Spring Boot 2.4.5):

- HTTP port: **8080**
- Endpoints: `/actuator/health`, `/actuator/prometheus`, и др. (по умолчанию SB 2.4)

Отдельная конфигурация actuator в проекте не переопределена.

---

## 5. Готовые сценарии (YAML)

Файлы: [`src/main/resources/scenarios/`](../src/main/resources/scenarios/).

| `GENERATOR_SCENARIO` | Файл | Назначение |
|----------------------|------|------------|
| `happy-path` | `happy-path.yml` | 10 happy cases, Process A + B + Legacy |
| `full-poc` | `full-poc.yml` | Полный POC: 3 Flow + Legacy, все типы cases |
| `negative` | `negative.yml` | Компактный набор negative cases |
| `load-smoke` | `load-smoke.yml` | 1000 happy instances, без задержек |

Запуск:

```bash
java -jar build/libs/e2e-event-generator.jar --generator.scenario=negative
```

### 5.1 Структура YAML-сценария

```yaml
name: full-poc          # логическое имя
seed: 1001              # seed сценария (перекрывается generator.seed если > 0)

timing:
  delayMsBetweenSnapshots: 150   # пауза между snapshot-публикациями
  jitterMs: 50                   # случайный jitter (deterministic от seed)

processes:               # Flow processes
  - id: Process_A
    definitionId: "Process_A:1:poc-a-v1"
    bpmnResource: bpmn/process-a.bpmn
    instanceCount: 15
    correlationVariable: applicationId
    nodes: [...]

legacy:                  # Legacy sources (тот же DTO ProcessInstanceEvent)
  - id: Legacy_Scoring
    correlationVariable: applicationRef
    nodes: [...]

cases:                   # распределение logical cases
  happy: 10
  delayedLegacy: 1
  missingLegacy: 1
  missingProcessB: 1
  retry: 1
  failedProcess: 1
  duplicateEvent: 1
  ambiguous: 1
  invalidContract: 1
```

### 5.2 Типы cases

| Ключ YAML | Поведение |
|-----------|-----------|
| `happy` | Process A + Legacy + Process B (+ C если есть в сценарии) |
| `delayedLegacy` | Flow A и B сначала; Legacy с увеличенной задержкой |
| `missingLegacy` | Flow A + B без Legacy |
| `missingProcessB` | Flow A + Legacy без Process B |
| `retry` | Process A с retry snapshots, затем COMPLETED |
| `failedProcess` | Process A завершается в FAILED |
| `duplicateEvent` | Повторная публикация snapshot с **тем же `ce_id`** |
| `ambiguous` | Один business key `AMBIGUOUS-SHARED-KEY` для correlated case |
| `invalidContract` | Намеренно невалидный payload (validation bypass, negative mode) |

Корреляция между источниками: одинаковое значение `APP-0001`, `APP-0002`, … в разных variable names (`applicationId`, `creditApplicationId`, `applicationRef`).

---

## 6. Примеры запуска

### Happy path (batch)

```bash
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
java -jar build/libs/e2e-event-generator.jar \
  --generator.scenario=happy-path \
  --generator.mode=BATCH \
  --generator.seed=1001
```

### Full POC с Flow CloudEvents

```bash
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export FLOW_MODEL_CE_TYPE=<confirmed-model-type>
export FLOW_INSTANCE_CE_TYPE=<confirmed-instance-type>
export FLOW_CE_SOURCE=<confirmed-source>
export CE_SPEC_VERSION=<confirmed-spec-version>

java -jar build/libs/e2e-event-generator.jar \
  --generator.scenario=full-poc
```

### Continuous mode

```bash
java -jar build/libs/e2e-event-generator.jar \
  --generator.scenario=happy-path \
  --generator.mode=CONTINUOUS \
  --generator.continuous-delay-ms=10000
```

Генератор будет повторять сценарий каждые 10 секунд до остановки процесса (`Ctrl+C` / `docker stop`).

### Negative scenarios

```bash
java -jar build/libs/e2e-event-generator.jar \
  --generator.scenario=negative \
  --generator.seed=2001
```

### Load smoke

```bash
java -jar build/libs/e2e-event-generator.jar \
  --generator.scenario=load-smoke \
  --generator.mode=BATCH
```

---

## 7. Минимальная конфигурация для POC

**Обязательно:**

```bash
KAFKA_BOOTSTRAP_SERVERS=<host:port>
```

**Рекомендуется перед интеграцией с BAMN / Flow:**

```bash
FLOW_MODEL_TOPIC=model-update-event
FLOW_INSTANCE_TOPIC=instance-update-event
LEGACY_TOPIC=legacy-events

FLOW_MODEL_CE_TYPE=<from Flow samples>
FLOW_INSTANCE_CE_TYPE=<from Flow samples>
FLOW_CE_SOURCE=<from Flow samples>
CE_SPEC_VERSION=<from Flow samples>
```

**Legacy POC defaults** (можно не задавать):

```bash
LEGACY_INSTANCE_CE_TYPE=poc.legacy.instance
LEGACY_CE_SOURCE=poc.legacy.generator
```

---

## 8. Логирование успешной отправки

При каждой успешной публикации в Kafka логируется:

```text
topic, partition, offset, ce_id, processId, processInstanceId
```

Ошибки send **не глотаются** — исключение пробрасывается наверх.

---

## 9. Связанные документы

- [README.md](../README.md) — обзор проекта
- [ARCHITECTURE.md](ARCHITECTURE.md) — компоненты и потоки данных
- [CONTRACT_NOTES.md](CONTRACT_NOTES.md) — правила Flow contract
- [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md) — план реализации
