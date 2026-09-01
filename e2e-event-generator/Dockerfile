ARG BUILD_JDK11_IMAGE
FROM ${BUILD_JDK11_IMAGE} AS build

WORKDIR /workspace

COPY . .

# Requires trusted Gradle wrapper 6.8 to be present.
RUN ./gradlew clean check bootJar --no-daemon


ARG RUNTIME_JRE11_IMAGE
FROM ${RUNTIME_JRE11_IMAGE}

WORKDIR /app

COPY --from=build /workspace/build/libs/e2e-event-generator.jar /app/e2e-event-generator.jar

USER 10001

ENTRYPOINT ["java", "-jar", "/app/e2e-event-generator.jar"]
