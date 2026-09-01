package ru.sber.bamn.e2egenerator.scenario

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import ru.sber.bamn.e2egenerator.config.GeneratorProperties
import ru.sber.bamn.e2egenerator.generator.common.HeaderFactory
import ru.sber.bamn.e2egenerator.generator.instance.FlowInstanceGenerator
import ru.sber.bamn.e2egenerator.generator.legacy.LegacyInstanceGenerator
import ru.sber.bamn.e2egenerator.generator.model.FlowModelGenerator
import ru.sber.bamn.e2egenerator.kafka.KafkaPublisher
import ru.sber.bamn.e2egenerator.validation.ContractValidator

@Component
class ScenarioEngine(
    private val properties: GeneratorProperties,
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
        publishModels(scenario)

        val maxInstances = listOf(
            scenario.processes.maxOfOrNull { it.instanceCount } ?: 0,
            scenario.legacy.maxOfOrNull { it.instanceCount } ?: 0
        ).maxOrNull() ?: 0

        for (index in 1..maxInstances) {
            val correlationValue = "APP-%04d".format(index)

            // Interleave sources by logical case instead of generating all A then all B.
            scenario.processes.forEach { definition ->
                if (index <= definition.instanceCount) {
                    instanceGenerator
                        .generateSnapshots(definition, index, correlationValue)
                        .forEach { event ->
                            val headers = headerFactory.flowInstance()
                            validator.validate(headers, event)
                            publisher.publish(
                                properties.kafka.topics.flowInstances,
                                event.id,
                                headers,
                                event
                            )
                            scheduler.delay(scenario.timing.delayMsBetweenSnapshots)
                        }
                }
            }

            scenario.legacy.forEach { definition ->
                if (index <= definition.instanceCount) {
                    legacyGenerator
                        .generateSnapshots(definition, index, correlationValue)
                        .forEach { event ->
                            val headers = headerFactory.legacyInstance()
                            validator.validate(headers, event)
                            publisher.publish(
                                properties.kafka.topics.legacyInstances,
                                event.id,
                                headers,
                                event
                            )
                            scheduler.delay(scenario.timing.delayMsBetweenSnapshots)
                        }
                }
            }
        }

        log.info("Scenario completed name={}", scenario.name)
    }

    private fun publishModels(scenario: Scenario) {
        scenario.processes.forEach { definition ->
            val event = modelGenerator.generate(definition)
            val headers = headerFactory.flowModel()

            validator.validate(headers, event)

            publisher.publish(
                properties.kafka.topics.flowModels,
                event.processDefinitionId,
                headers,
                event
            )
        }
    }
}
