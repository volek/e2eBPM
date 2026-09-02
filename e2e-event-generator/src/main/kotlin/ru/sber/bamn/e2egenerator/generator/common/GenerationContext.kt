package ru.sber.bamn.e2egenerator.generator.common

import ru.sber.bamn.e2egenerator.scenario.CaseType
import java.time.Instant
import java.time.ZoneOffset

data class GenerationContext(
    val seed: Long,
    val caseIndex: Int,
    val caseType: CaseType,
    val correlationValue: String,
    val ambiguousCorrelationValue: String? = null
) {
    private val baseEpochSeconds: Long = 1_600_000_000L + seed + caseIndex

    fun timestamp(offsetSeconds: Long = 0L): String =
        Instant.ofEpochSecond(baseEpochSeconds + offsetSeconds)
            .atOffset(ZoneOffset.UTC)
            .toString()

    fun eventSequence(base: String, snapshotIndex: Int): String =
        "$base-$caseIndex-$snapshotIndex"
}
