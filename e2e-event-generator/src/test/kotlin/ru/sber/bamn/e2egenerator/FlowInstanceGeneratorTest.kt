package ru.sber.bamn.e2egenerator

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.sber.bamn.e2egenerator.contract.ProcessState
import ru.sber.bamn.e2egenerator.generator.common.GenerationContext
import ru.sber.bamn.e2egenerator.generator.common.IdFactory
import ru.sber.bamn.e2egenerator.generator.instance.FlowInstanceGenerator
import ru.sber.bamn.e2egenerator.scenario.CaseType
import ru.sber.bamn.e2egenerator.scenario.ProcessDefinition
import ru.sber.bamn.e2egenerator.scenario.ScenarioNode

class FlowInstanceGeneratorTest {
    private val generator = FlowInstanceGenerator(ObjectMapper(), IdFactory().apply { configure(1001) })

    private val definition = ProcessDefinition(
        id = "Process_A",
        definitionId = "Process_A:1:poc-a-v1",
        name = "Loan Application",
        bpmnResource = "bpmn/process-a.bpmn",
        correlationVariable = "applicationId",
        nodes = listOf(
            ScenarioNode("Event_A_Start", "Start", "startEvent"),
            ScenarioNode("Activity_A_Validate", "Validate", "serviceTask"),
            ScenarioNode("Event_A_End", "End", "endEvent")
        )
    )

    @Test
    fun `snapshots keep same instance id`() {
        val context = GenerationContext(1001, 1, CaseType.HAPPY, "APP-0001")
        val snapshots = generator.generateSnapshots(definition, context)

        assertTrue(snapshots.size >= 2)
        val ids = snapshots.map { it.id }.distinct()
        assertEquals(1, ids.size)
    }

    @Test
    fun `failed case ends with failed state`() {
        val context = GenerationContext(1001, 2, CaseType.FAILED_PROCESS, "APP-0002")
        val snapshots = generator.generateSnapshots(definition, context)

        assertEquals(ProcessState.FAILED.code, snapshots.last().state)
    }

    @Test
    fun `retry case increases retry count`() {
        val context = GenerationContext(1001, 3, CaseType.RETRY, "APP-0003")
        val snapshots = generator.generateSnapshots(definition, context)

        assertTrue(snapshots.any { it.retryCount > 0 })
    }
}
