package ru.sber.bamn.e2egenerator.generator.common

import org.springframework.stereotype.Component
import ru.sber.bamn.e2egenerator.config.GeneratorProperties
import ru.sber.bamn.e2egenerator.contract.CommonHeaders
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Component
class HeaderFactory(
    private val properties: GeneratorProperties,
    private val idFactory: IdFactory
) {
    fun flowModel(): CommonHeaders =
        create(
            properties.headers.flowModel.ceType,
            properties.headers.flowModel.ceSource
        )

    fun flowInstance(): CommonHeaders =
        create(
            properties.headers.flowInstance.ceType,
            properties.headers.flowInstance.ceSource
        )

    fun legacyInstance(): CommonHeaders =
        create(
            properties.headers.legacyInstance.ceType,
            properties.headers.legacyInstance.ceSource
        )

    private fun create(type: String, source: String): CommonHeaders {
        return CommonHeaders(
            ceType = type,
            ceSource = source,
            ceSpecversion = properties.headers.ceSpecversion,
            ceTime = OffsetDateTime.now(ZoneOffset.UTC).toString(),
            ceId = idFactory.eventId()
        )
    }
}
