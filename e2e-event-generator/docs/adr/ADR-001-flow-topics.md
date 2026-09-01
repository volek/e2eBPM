# ADR-001 — Разделение Flow topics

## Решение

`ProcessModelEvent` и `ProcessInstanceEvent` публикуются в разные Kafka topics.

## Причина

Flow AsyncAPI задаёт разные channels:
- model-update-event
- instance-update-event

## Следствие

Generator имеет два независимых producer routes.
