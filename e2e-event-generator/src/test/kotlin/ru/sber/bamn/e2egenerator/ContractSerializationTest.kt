package ru.sber.bamn.e2egenerator

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.sber.bamn.e2egenerator.contract.ProcessInstanceEvent
import ru.sber.bamn.e2egenerator.contract.ProcessModelEvent

class ContractSerializationTest {
    private val mapper = ObjectMapper().registerModule(KotlinModule())

    @Test
    fun `process model roundtrip preserves nullable required fields`() {
        val json = readFixture("fixtures/process-model-event.json")
        val event = mapper.readValue(json, ProcessModelEvent::class.java)
        val serialized = mapper.readTree(mapper.writeValueAsString(event))

        assertTrue(serialized.has("resourceName"))
        assertTrue(serialized.get("resourceName").isNull)
        assertTrue(serialized.has("instancesSuspended"))
        assertEquals(false, serialized.get("instancesSuspended").booleanValue())
        assertEquals(event.processId, serialized.get("processId").asText())
    }

    @Test
    fun `process instance roundtrip preserves nested variables`() {
        val json = readFixture("fixtures/process-instance-event.json")
        val event = mapper.readValue(json, ProcessInstanceEvent::class.java)
        val serialized = mapper.readTree(mapper.writeValueAsString(event))

        assertTrue(serialized.has("extIds"))
        assertTrue(serialized.get("extIds").isNull)
        assertTrue(serialized.has("variables"))
        val nested = serialized.get("variables").get("nested").get("level1").get("level2")
        assertEquals("value", nested.asText())
    }

    @Test
    fun `semantic roundtrip for process instance`() {
        val json = readFixture("fixtures/process-instance-event.json")
        val original = mapper.readTree(json)
        val event = mapper.readValue(json, ProcessInstanceEvent::class.java)
        val roundtrip = mapper.readTree(mapper.writeValueAsString(event))

        assertEquals(original.get("id").asText(), roundtrip.get("id").asText())
        assertEquals(original.get("state").intValue(), roundtrip.get("state").intValue())
        assertEquals(
            original.get("variables").get("applicationId").asText(),
            roundtrip.get("variables").get("applicationId").asText()
        )
    }

    private fun readFixture(path: String): String =
        javaClass.classLoader.getResource(path)!!.readText()
}
