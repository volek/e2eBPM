package ru.sber.bamn.e2egenerator.generator.common

import org.springframework.stereotype.Component
import ru.sber.bamn.e2egenerator.config.GeneratorProperties
import ru.sber.bamn.e2egenerator.contract.CommonHeaders

@Component
class HeaderFactory(
    private val properties: GeneratorProperties,
    private val idFactory: IdFactory
) {
    fun flowModel(context: GenerationContext? = null, snapshotIndex: Int = 0): CommonHeaders =
        create(
            properties.headers.flowModel.ceType,
            properties.headers.flowModel.ceSource,
            context,
            snapshotIndex
        )

    fun flowInstance(context: GenerationContext, snapshotIndex: Int, fixedCeId: String? = null): CommonHeaders =
        create(
            properties.headers.flowInstance.ceType,
            properties.headers.flowInstance.ceSource,
            context,
            snapshotIndex,
            fixedCeId
        )

    fun legacyInstance(context: GenerationContext, snapshotIndex: Int): CommonHeaders =
        create(
            properties.headers.legacyInstance.ceType,
            properties.headers.legacyInstance.ceSource,
            context,
            snapshotIndex
        )

    private fun create(
        type: String,
        source: String,
        context: GenerationContext?,
        snapshotIndex: Int,
        fixedCeId: String? = null
    ): CommonHeaders {
        val ceTime = context?.timestamp(snapshotIndex.toLong()) ?: java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC).toString()
        val ceId = fixedCeId ?: context?.let { idFactory.eventId(it, snapshotIndex) } ?: idFactory.eventIdFromValue("$ceTime-$snapshotIndex")

        return CommonHeaders(
            ceType = type,
            ceSource = source,
            ceSpecversion = properties.headers.ceSpecversion,
            ceTime = ceTime,
            ceId = ceId
        )
    }
}
