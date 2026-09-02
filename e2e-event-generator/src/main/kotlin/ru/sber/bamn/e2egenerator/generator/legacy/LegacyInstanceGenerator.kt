package ru.sber.bamn.e2egenerator.generator.legacy

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import ru.sber.bamn.e2egenerator.contract.NodeInstance
import ru.sber.bamn.e2egenerator.contract.NodeState
import ru.sber.bamn.e2egenerator.contract.ProcessInstanceEvent
import ru.sber.bamn.e2egenerator.contract.ProcessState
import ru.sber.bamn.e2egenerator.generator.common.GenerationContext
import ru.sber.bamn.e2egenerator.generator.common.IdFactory
import ru.sber.bamn.e2egenerator.scenario.LegacyDefinition

@Component
class LegacyInstanceGenerator(
    private val objectMapper: ObjectMapper,
    private val idFactory: IdFactory
) {
    fun generateSnapshots(
        definition: LegacyDefinition,
        context: GenerationContext
    ): List<ProcessInstanceEvent> {
        val instanceId = idFactory.deterministic(definition.id, "legacy-${context.caseIndex}")
        val correlation = context.ambiguousCorrelationValue ?: context.correlationValue
        val variableNode = objectMapper.valueToTree<com.fasterxml.jackson.databind.JsonNode>(correlation)

        val running = definition.nodes.mapIndexed { nodeIndex, _ ->
            buildSnapshot(
                definition = definition,
                context = context,
                instanceId = instanceId,
                correlation = correlation,
                variableNode = variableNode,
                nodeIndex = nodeIndex,
                processState = ProcessState.RUNNING,
                endDate = null,
                markRunningAt = nodeIndex
            )
        }

        val completed = buildSnapshot(
            definition = definition,
            context = context,
            instanceId = instanceId,
            correlation = correlation,
            variableNode = variableNode,
            nodeIndex = definition.nodes.lastIndex,
            processState = ProcessState.COMPLETED,
            endDate = context.timestamp(definition.nodes.size.toLong() + 1),
            markRunningAt = -1
        )

        return running + completed
    }

    private fun buildSnapshot(
        definition: LegacyDefinition,
        context: GenerationContext,
        instanceId: String,
        correlation: String,
        variableNode: com.fasterxml.jackson.databind.JsonNode,
        nodeIndex: Int,
        processState: ProcessState,
        endDate: String?,
        markRunningAt: Int
    ): ProcessInstanceEvent {
        val nodes = definition.nodes.take(nodeIndex + 1).mapIndexed { i, current ->
            NodeInstance(
                id = idFactory.deterministic(instanceId, current.id),
                nodeId = current.id,
                nodeDefinitionId = current.id,
                nodeName = current.name,
                nodeType = current.type,
                error = null,
                state = if (i == markRunningAt && processState != ProcessState.COMPLETED) {
                    NodeState.RUNNING.code
                } else {
                    NodeState.COMPLETED.code
                },
                calledProcessInstanceIds = null,
                retries = emptyList(),
                htmTaskId = null,
                triggerTime = context.timestamp(i.toLong()),
                leaveTime = if (i == markRunningAt && processState != ProcessState.COMPLETED) {
                    null
                } else {
                    context.timestamp(i.toLong() + 1)
                },
                triggerNodeInstanceId = if (i == 0) null else idFactory.deterministic(instanceId, definition.nodes[i - 1].id),
                creationOrder = i
            )
        }

        return ProcessInstanceEvent(
            id = instanceId,
            parentInstanceId = null,
            rootInstanceId = instanceId,
            processId = definition.id,
            processDefinitionId = definition.definitionId,
            resourceName = null,
            rootProcessId = definition.id,
            processName = definition.name,
            startDate = context.timestamp(),
            endDate = endDate,
            state = processState.code,
            businessKey = correlation,
            version = 1,
            bamProjectId = null,
            extIds = null,
            error = null,
            moduleId = definition.moduleId,
            engineVersion = null,
            enginePodName = null,
            retryCount = 0,
            ownerRole = null,
            idempotencyKey = null,
            operation = null,
            nodeInstances = nodes,
            variables = mapOf(definition.correlationVariable to variableNode),
            contextSize = null
        )
    }
}
