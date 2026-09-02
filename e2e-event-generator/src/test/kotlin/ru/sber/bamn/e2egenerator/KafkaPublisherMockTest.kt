package ru.sber.bamn.e2egenerator

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.TopicPartition
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.util.concurrent.SettableListenableFuture
import ru.sber.bamn.e2egenerator.contract.CommonHeaders
import ru.sber.bamn.e2egenerator.contract.ProcessModelEvent
import ru.sber.bamn.e2egenerator.kafka.KafkaMessageFactory
import ru.sber.bamn.e2egenerator.kafka.KafkaPublisher

class KafkaPublisherMockTest {

    @Test
    fun `publish delegates to kafka template with model topic key`() {
        val kafkaTemplate = mockk<KafkaTemplate<String, String>>()
        val publisher = KafkaPublisher(kafkaTemplate, KafkaMessageFactory(ObjectMapper()))

        val recordSlot = slot<ProducerRecord<String, String>>()

        every { kafkaTemplate.send(capture(recordSlot)) } answers {
            val metadata = RecordMetadata(TopicPartition("model-update-event", 0), 0, 0, 0, 0, 0)
            val future = SettableListenableFuture<SendResult<String, String>>()
            future.set(SendResult(recordSlot.captured, metadata))
            future
        }

        val event = ProcessModelEvent(
            processId = "Process_A",
            processDefinitionId = "Process_A:1:poc-a-v1",
            resourceName = null,
            processName = "Loan Application",
            processVersion = "1",
            businessFamily = null,
            ownerRole = null,
            contextVisible = true,
            processVersionInternal = "1",
            schema = "UE9DCQ==",
            moduleId = "bpmx",
            bamProjectId = null,
            deleteReason = null,
            instancesSuspended = false,
            suspended = false,
            retryPolicyModels = emptyList(),
            operation = null,
            created = "2020-09-13T12:26:40Z",
            tags = emptyMap(),
            maskPatterns = emptyList()
        )

        val headers = CommonHeaders("model.event", "poc.generator", "1.0", "2020-09-13T12:26:40Z", "ce-123")

        publisher.publish("model-update-event", event.processDefinitionId, headers, event)

        verify(exactly = 1) { kafkaTemplate.send(match<ProducerRecord<String, String>> { true }) }
        assertEquals("Process_A:1:poc-a-v1", recordSlot.captured.key())
        assertEquals("ce-123", String(recordSlot.captured.headers().lastHeader("ce_id").value()))
    }
}
