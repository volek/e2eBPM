package ru.sber.bamn.e2egenerator.generator.instance

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import ru.sber.bamn.e2egenerator.contract.ErrorInfo
import ru.sber.bamn.e2egenerator.contract.NodeInstance
import ru.sber.bamn.e2egenerator.contract.NodeState
import ru.sber.bamn.e2egenerator.contract.ProcessInstanceEvent
import ru.sber.bamn.e2egenerator.contract.ProcessState
import ru.sber.bamn.e2egenerator.contract.RetryInstance
import ru.sber.bamn.e2egenerator.generator.common.GenerationContext
import ru.sber.bamn.e2egenerator.generator.common.IdFactory
import ru.sber.bamn.e2egenerator.scenario.CaseType
import ru.sber.bamn.e2egenerator.scenario.ProcessDefinition

@Component
class FlowInstanceGenerator(
    private val objectMapper: ObjectMapper,
    private val idFactory: IdFactory
) {
    fun generateSnapshots(
        definition: ProcessDefinition,
        context: GenerationContext
    ): List<ProcessInstanceEvent> {
        return when (context.caseType) {
            CaseType.FAILED_PROCESS -> generateFailedSnapshots(definition, context)
            CaseType.RETRY -> generateRetrySnapshots(definition, context)
            CaseType.INVALID_CONTRACT -> listOf(generateInvalidSnapshot(definition, context))
            else -> generateHappySnapshots(definition, context)
        }
    }

    fun generateInvalidSnapshot(definition: ProcessDefinition, context: GenerationContext): ProcessInstanceEvent {
        val happy = generateHappySnapshots(definition, context).first()
        return happy.copy(
            id = "",
            processId = "",
            processDefinitionId = ""
        )
    }

    private fun generateHappySnapshots(
        definition: ProcessDefinition,
        context: GenerationContext
    ): List<ProcessInstanceEvent> {
        val instanceId = idFactory.deterministic(definition.id, "instance-${context.caseIndex}")
        val correlation = effectiveCorrelation(context)
        val variableNode = objectMapper.valueToTree<com.fasterxml.jackson.databind.JsonNode>(correlation)

        val runningSnapshots = definition.nodes.mapIndexed { nodeIndex, _ ->
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

        return runningSnapshots + completed
    }

    private fun generateFailedSnapshots(
        definition: ProcessDefinition,
        context: GenerationContext
    ): List<ProcessInstanceEvent> {
        val instanceId = idFactory.deterministic(definition.id, "instance-${context.caseIndex}")
        val correlation = effectiveCorrelation(context)
        val variableNode = objectMapper.valueToTree<com.fasterxml.jackson.databind.JsonNode>(correlation)
        val failNodeIndex = definition.nodes.indexOfFirst { it.type == "serviceTask" }
            .takeIf { it >= 0 } ?: 1

        val running = buildSnapshot(
            definition = definition,
            context = context,
            instanceId = instanceId,
            correlation = correlation,
            variableNode = variableNode,
            nodeIndex = failNodeIndex,
            processState = ProcessState.RUNNING,
            endDate = null,
            markRunningAt = failNodeIndex
        )

        val failed = buildSnapshot(
            definition = definition,
            context = context,
            instanceId = instanceId,
            correlation = correlation,
            variableNode = variableNode,
            nodeIndex = failNodeIndex,
            processState = ProcessState.FAILED,
            endDate = context.timestamp(failNodeIndex.toLong() + 2),
            markRunningAt = failNodeIndex,
            failedAt = failNodeIndex,
            error = ErrorInfo(
                nodeId = definition.nodes[failNodeIndex].id,
                errorMessage = "Simulated failure for case ${context.caseIndex}"
            )
        )

        return listOf(running, failed)
    }

    private fun generateRetrySnapshots(
        definition: ProcessDefinition,
        context: GenerationContext
    ): List<ProcessInstanceEvent> {
        val instanceId = idFactory.deterministic(definition.id, "instance-${context.caseIndex}")
        val correlation = effectiveCorrelation(context)
        val variableNode = objectMapper.valueToTree<com.fasterxml.jackson.databind.JsonNode>(correlation)
        val retryNodeIndex = definition.nodes.indexOfFirst { it.type == "serviceTask" }
            .takeIf { it >= 0 } ?: 1

        val beforeRetry = buildSnapshot(
            definition = definition,
            context = context,
            instanceId = instanceId,
            correlation = correlation,
            variableNode = variableNode,
            nodeIndex = retryNodeIndex,
            processState = ProcessState.RUNNING,
            endDate = null,
            markRunningAt = retryNodeIndex,
            retryCount = 0
        )

        val retryAttempt = buildSnapshot(
            definition = definition,
            context = context,
            instanceId = instanceId,
            correlation = correlation,
            variableNode = variableNode,
            nodeIndex = retryNodeIndex,
            processState = ProcessState.RUNNING,
            endDate = null,
            markRunningAt = retryNodeIndex,
            retryCount = 1,
            retries = listOf(
                RetryInstance(
                    id = idFactory.deterministic(instanceId, "retry-1"),
                    reason = "Simulated retry",
                    result = null,
                    time = context.timestamp(retryNodeIndex.toLong() + 1),
                    strategy = "linear",
                    planedDate = context.timestamp(retryNodeIndex.toLong() + 1),
                    factStartDate = context.timestamp(retryNodeIndex.toLong() + 1),
                    factEndDate = null,
                    retryPolicyId = null
                )
            ),
            nodeStateOverride = NodeState.INTERRUPTED
        )

        val completed = buildSnapshot(
            definition = definition,
            context = context,
            instanceId = instanceId,
            correlation = correlation,
            variableNode = variableNode,
            nodeIndex = definition.nodes.lastIndex,
            processState = ProcessState.COMPLETED,
            endDate = context.timestamp(definition.nodes.size.toLong() + 2),
            markRunningAt = -1,
            retryCount = 1
        )

        return listOf(beforeRetry, retryAttempt, completed)
    }

    private fun buildSnapshot(
        definition: ProcessDefinition,
        context: GenerationContext,
        instanceId: String,
        correlation: String,
        variableNode: com.fasterxml.jackson.databind.JsonNode,
        nodeIndex: Int,
        processState: ProcessState,
        endDate: String?,
        markRunningAt: Int,
        failedAt: Int = -1,
        error: ErrorInfo? = null,
        retryCount: Int = 0,
        retries: List<RetryInstance> = emptyList(),
        nodeStateOverride: NodeState? = null
    ): ProcessInstanceEvent {
        val nodes = definition.nodes.take(nodeIndex + 1).mapIndexed { i, current ->
            val nodeState = when {
                i == failedAt -> NodeState.FAILED
                i == markRunningAt && processState != ProcessState.COMPLETED -> nodeStateOverride ?: NodeState.RUNNING
                else -> NodeState.COMPLETED
            }

            NodeInstance(
                id = idFactory.deterministic(instanceId, current.id),
                nodeId = current.id,
                nodeDefinitionId = current.id,
                nodeName = current.name,
                nodeType = current.type,
                error = if (i == failedAt) error?.errorMessage else null,
                state = nodeState.code,
                calledProcessInstanceIds = null,
                retries = if (i == markRunningAt) retries else emptyList(),
                htmTaskId = null,
                triggerTime = context.timestamp(i.toLong()),
                leaveTime = if (nodeState == NodeState.RUNNING) null else context.timestamp(i.toLong() + 1),
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
            version = definition.version,
            bamProjectId = null,
            extIds = null,
            error = error,
            moduleId = definition.moduleId,
            engineVersion = null,
            enginePodName = null,
            retryCount = retryCount,
            ownerRole = null,
            idempotencyKey = null,
            operation = null,
            nodeInstances = nodes,
            variables = mapOf(definition.correlationVariable to variableNode),
            contextSize = null
        )
    }

    private fun effectiveCorrelation(context: GenerationContext): String =
        context.ambiguousCorrelationValue ?: context.correlationValue
}
