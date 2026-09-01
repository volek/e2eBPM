# Gradle Wrapper

Baseline проекта: Gradle 6.8.

В архиве намеренно не подменяется бинарный `gradle-wrapper.jar` произвольной версией.

Создать wrapper в доверенной среде с Gradle 6.8:

```bash
gradle wrapper --gradle-version 6.8
```

После этого в репозитории должны появиться:
- `gradlew`
- `gradlew.bat`
- `gradle/wrapper/gradle-wrapper.jar`

`gradle-wrapper.properties` уже зафиксирован на 6.8.

Если в инфраструктуре BAMN имеется утверждённый wrapper, предпочтительно скопировать его из baseline-проекта BAMN.
