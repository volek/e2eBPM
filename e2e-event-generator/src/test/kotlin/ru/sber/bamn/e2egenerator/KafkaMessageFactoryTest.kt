package ru.sber.bamn.e2egenerator

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.producer.ProducerRecord
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ru.sber.bamn.e2egenerator.contract.CommonHeaders
import ru.sber.bamn.e2egenerator.contract.ProcessModelEvent
import ru.sber.bamn.e2egenerator.kafka.KafkaMessageFactory

class KafkaMessageFactoryTest {
    private val factory = KafkaMessageFactory(ObjectMapper())

    @Test
    fun `creates producer record with headers and json payload`() {
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

        val headers = CommonHeaders(
            ceType = "model.event",
            ceSource = "poc.generator",
            ceSpecversion = "1.0",
            ceTime = "2020-09-13T12:26:40Z",
            ceId = "ce-123"
        )

        val record: ProducerRecord<String, String> = factory.create(
            "model-update-event",
            event.processDefinitionId,
            headers,
            event
        )

        assertEquals("Process_A:1:poc-a-v1", record.key())
        assertEquals("ce-123", String(record.headers().lastHeader("ce_id").value()))
        assertEquals("model.event", String(record.headers().lastHeader("ce_type").value()))
    }
}
