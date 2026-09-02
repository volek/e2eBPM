package ru.sber.bamn.e2egenerator

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.sber.bamn.e2egenerator.config.GeneratorProperties
import ru.sber.bamn.e2egenerator.contract.CommonHeaders
import ru.sber.bamn.e2egenerator.contract.ProcessInstanceEvent
import ru.sber.bamn.e2egenerator.contract.ProcessModelEvent
import ru.sber.bamn.e2egenerator.generator.common.HeaderFactory
import ru.sber.bamn.e2egenerator.generator.common.IdFactory
import ru.sber.bamn.e2egenerator.generator.instance.FlowInstanceGenerator
import ru.sber.bamn.e2egenerator.generator.legacy.LegacyInstanceGenerator
import ru.sber.bamn.e2egenerator.generator.model.FlowModelGenerator
import ru.sber.bamn.e2egenerator.kafka.KafkaPublisher
import ru.sber.bamn.e2egenerator.scenario.CaseDistribution
import ru.sber.bamn.e2egenerator.scenario.Scenario
import ru.sber.bamn.e2egenerator.scenario.ScenarioEngine
import ru.sber.bamn.e2egenerator.scenario.ScenarioLoader
import ru.sber.bamn.e2egenerator.scenario.ScenarioScheduler
import ru.sber.bamn.e2egenerator.scenario.Timing
import ru.sber.bamn.e2egenerator.validation.ContractValidator

class ScenarioEngineTest {
    private lateinit var properties: GeneratorProperties
    private lateinit var publisher: KafkaPublisher
    private lateinit var engine: ScenarioEngine

    @BeforeEach
    fun setUp() {
        properties = GeneratorProperties().apply {
            seed = 1001
            kafka.topics.flowModels = "model-update-event"
            kafka.topics.flowInstances = "instance-update-event"
            kafka.topics.legacyInstances = "legacy-events"
            headers.flowModel.ceType = "model.event"
            headers.flowInstance.ceType = "instance.event"
            headers.legacyInstance.ceType = "legacy.event"
            headers.ceSpecversion = "1.0"
        }

        publisher = mockk(relaxed = true)
        every { publisher.publish(any(), any(), any(), any()) } returns Unit

        val idFactory = IdFactory()
        val headerFactory = HeaderFactory(properties, idFactory)
        val scheduler = mockk<ScenarioScheduler>(relaxed = true)

        engine = ScenarioEngine(
            properties = properties,
            idFactory = idFactory,
            modelGenerator = FlowModelGenerator(),
            instanceGenerator = FlowInstanceGenerator(ObjectMapper(), idFactory),
            legacyGenerator = LegacyInstanceGenerator(ObjectMapper(), idFactory),
            headerFactory = headerFactory,
            validator = ContractValidator(),
            publisher = publisher,
            scheduler = scheduler
        )
    }

    @Test
    fun `model events go only to model topic`() {
        val scenario = ScenarioLoader().load("happy-path")

        engine.run(scenario)

        verify(atLeast = 1) {
            publisher.publish("model-update-event", any(), any(), match<ProcessModelEvent> { true })
        }
        verify(exactly = 0) {
            publisher.publish("instance-update-event", any(), any(), match<ProcessModelEvent> { true })
        }
    }

    @Test
    fun `missing legacy case skips legacy topic`() {
        val happyPath = ScenarioLoader().load("happy-path")
        val scenario = Scenario(
            name = "missing-legacy-test",
            seed = 1001,
            timing = Timing(delayMsBetweenSnapshots = 0),
            processes = happyPath.processes.filter { it.id == "Process_A" },
            legacy = happyPath.legacy,
            cases = CaseDistribution(happy = 0, missingLegacy = 1)
        )

        engine.run(scenario)

        verify(exactly = 0) {
            publisher.publish("legacy-events", any(), any(), match<ProcessInstanceEvent> { true })
        }
    }

    @Test
    fun `duplicate event case publishes same ce id twice`() {
        val capturedHeaders = mutableListOf<CommonHeaders>()
        every { publisher.publish(any(), any(), capture(capturedHeaders), any()) } returns Unit

        val happyPath = ScenarioLoader().load("happy-path")
        val scenario = Scenario(
            name = "duplicate-test",
            seed = 1001,
            timing = Timing(delayMsBetweenSnapshots = 0),
            processes = happyPath.processes.filter { it.id == "Process_A" },
            legacy = emptyList(),
            cases = CaseDistribution(happy = 0, duplicateEvent = 1)
        )

        engine.run(scenario)

        val instanceHeaders = capturedHeaders.filter { it.ceType == "instance.event" }
        assertEquals(2, instanceHeaders.size)
        assertEquals(instanceHeaders[0].ceId, instanceHeaders[1].ceId)
    }
}
