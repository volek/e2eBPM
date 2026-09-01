package ru.sber.bamn.e2egenerator.generator.instance

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import ru.sber.bamn.e2egenerator.contract.NodeInstance
import ru.sber.bamn.e2egenerator.contract.NodeState
import ru.sber.bamn.e2egenerator.contract.ProcessInstanceEvent
import ru.sber.bamn.e2egenerator.contract.ProcessState
import ru.sber.bamn.e2egenerator.generator.common.IdFactory
import ru.sber.bamn.e2egenerator.scenario.ProcessDefinition
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Component
class FlowInstanceGenerator(
    private val objectMapper: ObjectMapper,
    private val idFactory: IdFactory
) {
    fun generateSnapshots(
        definition: ProcessDefinition,
        index: Int,
        correlationValue: String
    ): List<ProcessInstanceEvent> {
        val instanceId = idFactory.deterministic(definition.id, "instance-$index")
        val started = OffsetDateTime.now(ZoneOffset.UTC)

        val variableNode = objectMapper.valueToTree<com.fasterxml.jackson.databind.JsonNode>(correlationValue)

        return definition.nodes.mapIndexed { nodeIndex, node ->
            val now = started.plusSeconds(nodeIndex.toLong())
            val nodes = definition.nodes.take(nodeIndex + 1).mapIndexed { i, current ->
                val currentRunning = i == nodeIndex
                NodeInstance(
                    id = idFactory.deterministic(instanceId, current.id),
                    nodeId = current.id,
                    nodeDefinitionId = current.id,
                    nodeName = current.name,
                    nodeType = current.type,
                    error = null,
                    state = if (currentRunning) NodeState.RUNNING.code else NodeState.COMPLETED.code,
                    calledProcessInstanceIds = null,
                    retries = emptyList(),
                    htmTaskId = null,
                    triggerTime = started.plusSeconds(i.toLong()).toString(),
                    leaveTime = if (currentRunning) null else started.plusSeconds(i.toLong() + 1).toString(),
                    triggerNodeInstanceId = if (i == 0) null else idFactory.deterministic(instanceId, definition.nodes[i - 1].id),
                    creationOrder = i
                )
            }

            ProcessInstanceEvent(
                id = instanceId,
                parentInstanceId = null,
                rootInstanceId = instanceId,
                processId = definition.id,
                processDefinitionId = definition.definitionId,
                resourceName = null,
                rootProcessId = definition.id,
                processName = definition.name,
                startDate = started.toString(),
                endDate = null,
                state = ProcessState.RUNNING.code,
                businessKey = correlationValue,
                version = definition.version,
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
        } + completedSnapshot(definition, instanceId, correlationValue, started)
    }

    private fun completedSnapshot(
        definition: ProcessDefinition,
        instanceId: String,
        correlationValue: String,
        started: OffsetDateTime
    ): ProcessInstanceEvent {
        val variableNode = objectMapper.valueToTree<com.fasterxml.jackson.databind.JsonNode>(correlationValue)

        val nodes = definition.nodes.mapIndexed { i, node ->
            NodeInstance(
                id = idFactory.deterministic(instanceId, node.id),
                nodeId = node.id,
                nodeDefinitionId = node.id,
                nodeName = node.name,
                nodeType = node.type,
                error = null,
                state = NodeState.COMPLETED.code,
                calledProcessInstanceIds = null,
                retries = emptyList(),
                htmTaskId = null,
                triggerTime = started.plusSeconds(i.toLong()).toString(),
                leaveTime = started.plusSeconds(i.toLong() + 1).toString(),
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
            startDate = started.toString(),
            endDate = started.plusSeconds(definition.nodes.size.toLong() + 1).toString(),
            state = ProcessState.COMPLETED.code,
            businessKey = correlationValue,
            version = definition.version,
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
