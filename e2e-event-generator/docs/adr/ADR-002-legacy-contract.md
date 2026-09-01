# ADR-002 — Формат Legacy событий в POC

## Решение

Legacy Simulator публикует payload, совместимый с Flow `ProcessInstanceEvent`, в отдельный Legacy topic.

## Статус

Только POC-решение.

Это не утверждение о production контракте COTE или конкретной Legacy АС.

## Причина

POC проверяет BAMN correlation, а не разработку отдельного Legacy transport contract.
