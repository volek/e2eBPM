import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.32"
    kotlin("plugin.spring") version "1.4.32"

    id("org.springframework.boot") version "2.4.5"

    id("com.diffplug.spotless") version "5.12.4"
    id("io.gitlab.arturbosch.detekt") version "1.16.0"
    id("org.sonarqube") version "3.3"

    jacoco
}

group = "ru.sber.bamn"
version = "0.1.0"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

repositories {
    // Replace/augment with approved internal Maven repositories in bank environment.
    mavenCentral()
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:2.4.5"))

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation("org.springframework.kafka:spring-kafka:2.7.0")
    implementation("org.apache.kafka:kafka-clients:3.0.0")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.3")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.12.3")

    implementation("org.jetbrains.kotlin:kotlin-reflect:1.4.32")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.4.32")

    implementation("io.micrometer:micrometer-registry-prometheus:1.6.6")
    implementation("net.logstash.logback:logstash-logback-encoder:6.6")

    testImplementation("org.junit.jupiter:junit-jupiter:5.8.0-M1")
    testImplementation("org.springframework.kafka:spring-kafka-test:2.7.0")
    testImplementation("com.ninja-squad:springmockk:3.1.0")
}

configurations.all {
    resolutionStrategy {
        force("org.apache.kafka:kafka-clients:3.0.0")

        force("com.fasterxml.jackson.core:jackson-core:2.12.3")
        force("com.fasterxml.jackson.core:jackson-annotations:2.12.3")
        force("com.fasterxml.jackson.core:jackson-databind:2.12.3")
        force("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.3")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = freeCompilerArgs + "-Xjsr305=strict"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.bootJar {
    archiveFileName.set("e2e-event-generator.jar")
}

spotless {
    kotlin {
        target("src/**/*.kt")
        ktlint("0.41.0")
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktlint("0.41.0")
    }
}

detekt {
    toolVersion = "1.16.0"
    buildUponDefaultConfig = true
}

jacoco {
    toolVersion = "0.8.7"
}

springBoot {
    buildInfo()
}
