package ru.sber.bamn.e2egenerator.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import ru.sber.bamn.e2egenerator.contract.CommonHeaders
import java.nio.charset.StandardCharsets

@Component
class KafkaPublisher(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun publish(topic: String, key: String, headers: CommonHeaders, payload: Any) {
        val json = objectMapper.writeValueAsString(payload)
        val record = ProducerRecord<String, String>(topic, key, json)

        addHeader(record, "ce_type", headers.ceType)
        addHeader(record, "ce_source", headers.ceSource)
        addHeader(record, "ce_specversion", headers.ceSpecversion)
        addHeader(record, "ce_time", headers.ceTime)
        addHeader(record, "ce_id", headers.ceId)

        val metadata = kafkaTemplate.send(record).get().recordMetadata

        log.info(
            "Kafka event sent topic={} partition={} offset={} ce_id={} key={}",
            metadata.topic(),
            metadata.partition(),
            metadata.offset(),
            headers.ceId,
            key
        )
    }

    private fun addHeader(record: ProducerRecord<String, String>, name: String, value: String) {
        record.headers().add(name, value.toByteArray(StandardCharsets.UTF_8))
    }
}
