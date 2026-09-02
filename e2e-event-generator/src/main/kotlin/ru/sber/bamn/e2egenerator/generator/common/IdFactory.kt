package ru.sber.bamn.e2egenerator.generator.common

import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.UUID

@Component
class IdFactory {
    private var seed: Long = 1001L

    fun configure(seed: Long) {
        this.seed = seed
    }

    fun deterministic(namespace: String, value: String): String {
        return UUID.nameUUIDFromBytes("$namespace:$value".toByteArray(StandardCharsets.UTF_8)).toString()
    }

    fun eventId(context: GenerationContext, snapshotIndex: Int): String {
        return deterministic("ce-id", "$seed:${context.caseIndex}:$snapshotIndex")
    }

    fun eventIdFromValue(value: String): String {
        return deterministic("ce-id", value)
    }
}
