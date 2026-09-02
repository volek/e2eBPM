package ru.sber.bamn.e2egenerator.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.stereotype.Component
import ru.sber.bamn.e2egenerator.contract.CommonHeaders
import java.nio.charset.StandardCharsets

@Component
class KafkaMessageFactory(
    private val objectMapper: ObjectMapper
) {
    fun create(topic: String, key: String, headers: CommonHeaders, payload: Any): ProducerRecord<String, String> {
        val json = objectMapper.writeValueAsString(payload)
        val record = ProducerRecord<String, String>(topic, key, json)

        addHeader(record, "ce_type", headers.ceType)
        addHeader(record, "ce_source", headers.ceSource)
        addHeader(record, "ce_specversion", headers.ceSpecversion)
        addHeader(record, "ce_time", headers.ceTime)
        addHeader(record, "ce_id", headers.ceId)

        return record
    }

    private fun addHeader(record: ProducerRecord<String, String>, name: String, value: String) {
        record.headers().add(name, value.toByteArray(StandardCharsets.UTF_8))
    }
}
