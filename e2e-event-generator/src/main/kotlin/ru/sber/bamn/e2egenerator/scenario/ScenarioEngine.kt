package ru.sber.bamn.e2egenerator.scenario

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import ru.sber.bamn.e2egenerator.config.GeneratorProperties
import ru.sber.bamn.e2egenerator.contract.CommonHeaders
import ru.sber.bamn.e2egenerator.contract.ProcessInstanceEvent
import ru.sber.bamn.e2egenerator.generator.common.GenerationContext
import ru.sber.bamn.e2egenerator.generator.common.HeaderFactory
import ru.sber.bamn.e2egenerator.generator.common.IdFactory
import ru.sber.bamn.e2egenerator.generator.instance.FlowInstanceGenerator
import ru.sber.bamn.e2egenerator.generator.legacy.LegacyInstanceGenerator
import ru.sber.bamn.e2egenerator.generator.model.FlowModelGenerator
import ru.sber.bamn.e2egenerator.kafka.KafkaPublisher
import ru.sber.bamn.e2egenerator.validation.ContractValidator
import ru.sber.bamn.e2egenerator.validation.ValidationContext

@Component
class ScenarioEngine(
    private val properties: GeneratorProperties,
    private val idFactory: IdFactory,
    private val modelGenerator: FlowModelGenerator,
    private val instanceGenerator: FlowInstanceGenerator,
    private val legacyGenerator: LegacyInstanceGenerator,
    private val headerFactory: HeaderFactory,
    private val validator: ContractValidator,
    private val publisher: KafkaPublisher,
    private val scheduler: ScenarioScheduler
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun run(scenario: Scenario) {
        val seed = properties.seed.takeIf { it > 0 } ?: scenario.seed
        idFactory.configure(seed)

        publishModels(scenario, seed)

        val casePlan = CasePlanner.plan(scenario.cases)
        if (casePlan.isEmpty()) {
            log.warn("Scenario {} has no cases configured", scenario.name)
            return
        }

        casePlan.forEachIndexed { index, caseType ->
            val caseIndex = index + 1
            val correlationValue = "APP-%04d".format(caseIndex)
            val context = GenerationContext(
                seed = seed,
                caseIndex = caseIndex,
                caseType = caseType,
                correlationValue = correlationValue,
                ambiguousCorrelationValue = if (caseType == CaseType.AMBIGUOUS) AMBIGUOUS_KEY else null
            )

            log.info("Running case index={} type={} correlation={}", caseIndex, caseType, correlationValue)
            executeCase(scenario, context)
        }

        log.info("Scenario completed name={} cases={}", scenario.name, casePlan.size)
    }

    private fun executeCase(scenario: Scenario, context: GenerationContext) {
        when (context.caseType) {
            CaseType.INVALID_CONTRACT -> executeInvalidContract(context)
            CaseType.DUPLICATE_EVENT -> executeDuplicateEvent(scenario, context)
            CaseType.MISSING_LEGACY -> executeMissingLegacy(scenario, context)
            CaseType.MISSING_PROCESS_B -> executeMissingProcessB(scenario, context)
            CaseType.DELAYED_LEGACY -> executeDelayedLegacy(scenario, context)
            else -> executeStandardCase(scenario, context)
        }
    }

    private fun executeStandardCase(scenario: Scenario, context: GenerationContext) {
        val processA = scenario.processes.firstOrNull { it.id == "Process_A" } ?: scenario.processes.first()
        publishFlowProcess(scenario, processA, context)

        scenario.legacy.firstOrNull()?.let { legacy ->
            publishLegacy(scenario, legacy, context)
        }

        scenario.processes.filter { it.id == "Process_B" }.forEach { processB ->
            publishFlowProcess(scenario, processB, context.copy(caseType = CaseType.HAPPY))
        }

        scenario.processes.filter { it.id == "Process_C" }.forEach { processC ->
            publishFlowProcess(scenario, processC, context.copy(caseType = CaseType.HAPPY))
        }
    }

    private fun executeMissingLegacy(scenario: Scenario, context: GenerationContext) {
        val processA = scenario.processes.firstOrNull { it.id == "Process_A" } ?: scenario.processes.first()
        publishFlowProcess(scenario, processA, context.copy(caseType = CaseType.HAPPY))

        scenario.processes.filter { it.id == "Process_B" }.forEach { processB ->
            publishFlowProcess(scenario, processB, context.copy(caseType = CaseType.HAPPY))
        }
    }

    private fun executeMissingProcessB(scenario: Scenario, context: GenerationContext) {
        val processA = scenario.processes.firstOrNull { it.id == "Process_A" } ?: scenario.processes.first()
        publishFlowProcess(scenario, processA, context.copy(caseType = CaseType.HAPPY))

        scenario.legacy.firstOrNull()?.let { legacy ->
            publishLegacy(scenario, legacy, context.copy(caseType = CaseType.HAPPY))
        }
    }

    private fun executeDelayedLegacy(scenario: Scenario, context: GenerationContext) {
        val processA = scenario.processes.firstOrNull { it.id == "Process_A" } ?: scenario.processes.first()
        publishFlowProcess(scenario, processA, context.copy(caseType = CaseType.HAPPY))

        scenario.processes.filter { it.id == "Process_B" }.forEach { processB ->
            publishFlowProcess(scenario, processB, context.copy(caseType = CaseType.HAPPY))
        }

        scheduler.delay(
            scenario.timing.delayMsBetweenSnapshots * 5,
            scenario.timing.jitterMs,
            context.seed,
            context.caseIndex
        )

        scenario.legacy.firstOrNull()?.let { legacy ->
            publishLegacy(scenario, legacy, context.copy(caseType = CaseType.HAPPY))
        }
    }

    private fun executeDuplicateEvent(scenario: Scenario, context: GenerationContext) {
        val processA = scenario.processes.firstOrNull { it.id == "Process_A" } ?: scenario.processes.first()
        val snapshots = instanceGenerator.generateSnapshots(processA, context.copy(caseType = CaseType.HAPPY))
        val target = snapshots.first()
        val headers = headerFactory.flowInstance(context, snapshotIndex = 0)

        publishInstance(
            properties.kafka.topics.flowInstances,
            target,
            headers,
            ValidationContext()
        )
        publishInstance(
            properties.kafka.topics.flowInstances,
            target,
            headers,
            ValidationContext()
        )
    }

    private fun executeInvalidContract(context: GenerationContext) {
        val invalid = ProcessInstanceEvent(
            id = "",
            parentInstanceId = null,
            rootInstanceId = "",
            processId = "",
            processDefinitionId = "",
            resourceName = null,
            rootProcessId = null,
            processName = null,
            startDate = context.timestamp(),
            endDate = null,
            state = null,
            businessKey = null,
            version = 0,
            bamProjectId = null,
            extIds = null,
            error = null,
            moduleId = "",
            engineVersion = null,
            enginePodName = null,
            retryCount = -1,
            ownerRole = null,
            idempotencyKey = null,
            operation = null,
            nodeInstances = emptyList(),
            variables = emptyMap(),
            contextSize = null
        )

        val headers = headerFactory.flowInstance(context, snapshotIndex = 0)
        publisher.publish(
            properties.kafka.topics.flowInstances,
            "invalid-${context.caseIndex}",
            headers,
            invalid
        )
    }

    private fun publishFlowProcess(
        scenario: Scenario,
        definition: ProcessDefinition,
        context: GenerationContext
    ) {
        instanceGenerator.generateSnapshots(definition, context).forEachIndexed { snapshotIndex, event ->
            val headers = headerFactory.flowInstance(context, snapshotIndex)
            publishInstance(
                properties.kafka.topics.flowInstances,
                event,
                headers,
                ValidationContext()
            )
            scheduler.delay(
                scenario.timing.delayMsBetweenSnapshots,
                scenario.timing.jitterMs,
                context.seed,
                context.caseIndex
            )
        }
    }

    private fun publishLegacy(
        scenario: Scenario,
        definition: LegacyDefinition,
        context: GenerationContext
    ) {
        legacyGenerator.generateSnapshots(definition, context).forEachIndexed { snapshotIndex, event ->
            val headers = headerFactory.legacyInstance(context, snapshotIndex)
            publishInstance(
                properties.kafka.topics.legacyInstances,
                event,
                headers,
                ValidationContext()
            )
            scheduler.delay(
                scenario.timing.delayMsBetweenSnapshots,
                scenario.timing.jitterMs,
                context.seed,
                context.caseIndex
            )
        }
    }

    private fun publishInstance(
        topic: String,
        event: ProcessInstanceEvent,
        headers: CommonHeaders,
        validationContext: ValidationContext
    ) {
        validator.validate(headers, event, validationContext)
        publisher.publish(topic, event.id, headers, event)
    }

    private fun publishModels(scenario: Scenario, seed: Long) {
        scenario.processes.forEachIndexed { index, definition ->
            val context = GenerationContext(
                seed = seed,
                caseIndex = 0,
                caseType = CaseType.HAPPY,
                correlationValue = "MODEL"
            )
            val event = modelGenerator.generate(definition, context)
            val headers = headerFactory.flowModel(context, index)

            validator.validate(headers, event)
            publisher.publish(
                properties.kafka.topics.flowModels,
                event.processDefinitionId,
                headers,
                event
            )
        }
    }

    companion object {
        const val AMBIGUOUS_KEY = "AMBIGUOUS-SHARED-KEY"
    }
}
