package ru.sber.bamn.e2egenerator.generator.model

import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import ru.sber.bamn.e2egenerator.contract.ProcessModelEvent
import ru.sber.bamn.e2egenerator.scenario.ProcessDefinition
import java.nio.charset.StandardCharsets
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.Base64

@Component
class FlowModelGenerator {

    fun generate(definition: ProcessDefinition): ProcessModelEvent {
        val bpmn = ClassPathResource(definition.bpmnResource)
            .inputStream
            .bufferedReader(StandardCharsets.UTF_8)
            .use { it.readText() }

        val encoded = Base64.getEncoder().encodeToString(bpmn.toByteArray(StandardCharsets.UTF_8))

        return ProcessModelEvent(
            processId = definition.id,
            processDefinitionId = definition.definitionId,
            resourceName = null,
            processName = definition.name,
            processVersion = definition.version.toString(),
            businessFamily = null,
            ownerRole = null,
            contextVisible = true,
            processVersionInternal = definition.version.toString(),
            schema = encoded,
            moduleId = definition.moduleId,
            bamProjectId = null,
            deleteReason = null,
            instancesSuspended = false,
            suspended = false,
            retryPolicyModels = emptyList(),
            operation = null,
            created = OffsetDateTime.now(ZoneOffset.UTC).toString(),
            tags = emptyMap(),
            maskPatterns = emptyList()
        )
    }
}
