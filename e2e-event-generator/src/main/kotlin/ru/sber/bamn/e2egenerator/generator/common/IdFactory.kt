package ru.sber.bamn.e2egenerator.generator.common

import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.UUID

@Component
class IdFactory {
    fun deterministic(namespace: String, value: String): String {
        return UUID.nameUUIDFromBytes("$namespace:$value".toByteArray(StandardCharsets.UTF_8)).toString()
    }

    fun eventId(): String = UUID.randomUUID().toString()
}
