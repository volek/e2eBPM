package ru.sber.bamn.e2egenerator

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.sber.bamn.e2egenerator.generator.common.GenerationContext
import ru.sber.bamn.e2egenerator.generator.model.FlowModelGenerator
import ru.sber.bamn.e2egenerator.scenario.CaseType
import ru.sber.bamn.e2egenerator.scenario.ProcessDefinition
import ru.sber.bamn.e2egenerator.scenario.ScenarioNode
import java.util.Base64

class FlowModelGeneratorTest {
    private val generator = FlowModelGenerator()

    @Test
    fun `bpmn is base64 encoded into schema`() {
        val definition = ProcessDefinition(
            id = "Process_A",
            definitionId = "Process_A:1:poc-a-v1",
            name = "Loan Application",
            bpmnResource = "bpmn/process-a.bpmn",
            correlationVariable = "applicationId",
            nodes = listOf(ScenarioNode("n1", "Start", "startEvent"))
        )
        val context = GenerationContext(
            seed = 1001,
            caseIndex = 1,
            caseType = CaseType.HAPPY,
            correlationValue = "APP-0001"
        )

        val event = generator.generate(definition, context)
        val decoded = String(Base64.getDecoder().decode(event.schema))

        assertTrue(decoded.contains("Process_A"))
        assertEquals("Process_A", event.processId)
        assertEquals("Process_A:1:poc-a-v1", event.processDefinitionId)
    }
}
