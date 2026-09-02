package ru.sber.bamn.e2egenerator

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import ru.sber.bamn.e2egenerator.generator.common.GenerationContext
import ru.sber.bamn.e2egenerator.generator.common.IdFactory
import ru.sber.bamn.e2egenerator.scenario.CaseType

class IdFactoryTest {

    private val factory = IdFactory()

    @Test
    fun `deterministic ids are reproducible`() {
        assertEquals(
            factory.deterministic("Process_A", "instance-1"),
            factory.deterministic("Process_A", "instance-1")
        )
    }

    @Test
    fun `event ids are seed deterministic`() {
        factory.configure(1001)
        val context = GenerationContext(
            seed = 1001,
            caseIndex = 1,
            caseType = CaseType.HAPPY,
            correlationValue = "APP-0001"
        )

        val first = factory.eventId(context, 0)
        val second = factory.eventId(context, 0)
        val otherSnapshot = factory.eventId(context, 1)

        assertEquals(first, second)
        assertNotEquals(first, otherSnapshot)
    }
}
