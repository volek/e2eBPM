# Инструкции для реализации в Cursor

Выполнять задачи последовательно. После каждого этапа запускать тесты и не переходить дальше при красной сборке.

## Task 01 — Bootstrap

Prompt для Cursor:

> Создай/проверь Kotlin Spring Boot проект генератора. Используй строго Java 11, Kotlin 1.4.32, Spring Boot 2.4.5, Gradle 6.8. Не обновляй зависимости. JVM target 11. Сборка должна формировать `build/libs/e2e-event-generator.jar`.

Acceptance:
- проект импортируется;
- compilation target = 11;
- `bootJar` создаётся.

## Task 02 — Flow contract DTO

> Реализуй contract DTO из `docs/reference/Flow_contract.pdf`. Не сокращай `ProcessModelEvent` и `ProcessInstanceEvent`. Required nullable поля должны присутствовать в JSON как null. `variables` должен поддерживать произвольный nested JSON. Используй Jackson 2.12.3.

Acceptance:
- fixtures parse;
- roundtrip serialization работает.

## Task 03 — CloudEvents/Kafka headers

> Реализуй `HeaderFactory` и `KafkaMessageFactory`. Используй имена headers ровно из Flow contract: ce_type, ce_source, ce_specversion, ce_time, ce_id. Значения ce_type/source/specversion не придумывай — читай из application.yml/env.

## Task 04 — Model generator

> Реализуй загрузку BPMN XML из classpath, Base64 encoding и построение `ProcessModelEvent`. ProcessModelEvent должен отправляться только в configured Flow model topic.

## Task 05 — Instance lifecycle

> Реализуй генерацию snapshot-событий `ProcessInstanceEvent` одного instance. Тот же `id`, новый `ce_id` на каждом snapshot. Process state и Node state не смешивать.

## Task 06 — Legacy generator

> Реализуй Legacy generator на том же `ProcessInstanceEvent` DTO. Legacy producer публикует только в отдельный Legacy topic. Не создавай Legacy ProcessModelEvent.

## Task 07 — Scenario YAML

> Реализуй `ScenarioLoader` на Jackson 2.12.3. Сценарии должны быть external YAML и поддерживать deterministic seed.

## Task 08 — Correlation test data

> Сделай одинаковые logical keys между Flow A, Legacy и Flow B, но не генерируй E2E_ID. Например: applicationId / applicationRef / creditApplicationId.

## Task 09 — Negative scenarios

> Добавь delay, missing, retry, failed, duplicate ce_id, ambiguous correlation key и invalid-contract режимы. Invalid transport event отправляется только в специально включённом negative mode.

## Task 10 — Tests

> Используй JUnit Jupiter 5.8.0-M1, SpringMockK 3.1.0, spring-kafka-test 2.7.0. Testcontainers не добавляй без отдельного согласования версии.

## Task 11 — Docker

> Docker image должен запускать тот же bootJar под Java 11. Не фиксируй публичный vendor image: используй build args BUILD_JDK11_IMAGE и RUNTIME_JRE11_IMAGE.

## Запреты для Cursor

- Java 17/21 запрещены.
- Kotlin 1.5+ запрещён без отдельного решения.
- Spring Boot 2.5+/3.x запрещён.
- Gradle 7/8 запрещён.
- Не заменять Kafka Clients 3.0.0.
- Не обновлять Jackson 2.12.3.
- Не добавлять Testcontainers автоматически.
- Не генерировать E2E_ID.
- Не объединять model и instance events в один topic.
