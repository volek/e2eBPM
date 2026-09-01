# Замечания к Flow contract

Источник: `Flow_contract.pdf`.

## Каналы

- `ProcessModelEvent` -> `model-update-event`
- `ProcessInstanceEvent` -> `instance-update-event`

## Headers

- `ce_type`
- `ce_source`
- `ce_specversion`
- `ce_time`
- `ce_id`

Значения `ce_type`, `ce_source`, `ce_specversion` в предоставленной спецификации не подтверждены конкретными production samples. Поэтому они являются configuration values.

## Required / nullable

Документ прямо указывает, что все поля обязательны, но часть полей допускает `null`.

Следовательно:
- nullable required property не удаляется из JSON;
- serializer должен включать `null`.

## `variables`

`variables` — object и может содержать произвольный nested object.

Использовать:
- `Map<String, JsonNode>` либо
- `ObjectNode`.

## Опечатка `instancesSuspended`

В properties: `instancesSuspended`.
В одном required-list встречается `instansesSuspended`.

POC сериализует `instancesSuspended`, поскольку это имя объявлено property и используется в примере события.

## Состояние ProcessInstance

- RUNNING = 1
- COMPLETED = 2
- FAILED = 3
- SUSPENDED = 4
- INCIDENT = 5

## Состояние NodeInstance

- RUNNING = 0
- FAILED = 2
- INTERRUPTED = 3
- COMPLETED = 4

Не использовать один enum для двух шкал.
