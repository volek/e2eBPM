package ru.sber.bamn.e2egenerator.kafka

import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import ru.sber.bamn.e2egenerator.contract.CommonHeaders
import ru.sber.bamn.e2egenerator.contract.ProcessInstanceEvent
import ru.sber.bamn.e2egenerator.contract.ProcessModelEvent

@Component
class KafkaPublisher(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val messageFactory: KafkaMessageFactory
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun publish(topic: String, key: String, headers: CommonHeaders, payload: Any) {
        val record = messageFactory.create(topic, key, headers, payload)

        try {
            val metadata = kafkaTemplate.send(record).get().recordMetadata
            val processId = extractProcessId(payload)
            val processInstanceId = extractProcessInstanceId(payload)

            log.info(
                "Kafka event sent topic={} partition={} offset={} ce_id={} processId={} processInstanceId={}",
                metadata.topic(),
                metadata.partition(),
                metadata.offset(),
                headers.ceId,
                processId,
                processInstanceId
            )
        } catch (ex: Exception) {
            log.error("Kafka send failed topic={} ce_id={} key={}", topic, headers.ceId, key, ex)
            throw ex
        }
    }

    private fun extractProcessId(payload: Any): String? = when (payload) {
        is ProcessModelEvent -> payload.processId
        is ProcessInstanceEvent -> payload.processId
        else -> null
    }

    private fun extractProcessInstanceId(payload: Any): String? = when (payload) {
        is ProcessInstanceEvent -> payload.id
        else -> null
    }
}
