# ADR-003 — Неоднозначности Flow contract

## instancesSuspended

Использовать JSON property `instancesSuspended`.

Причина:
- именно так property объявлено в schema;
- required-list содержит опечатку `instansesSuspended`.

## Required nullable

Поле должно присутствовать в JSON даже при `null`.

## variables

Хранить как generic Jackson JSON tree.
