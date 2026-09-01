package ru.sber.bamn.e2egenerator

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ru.sber.bamn.e2egenerator.generator.common.IdFactory

class IdFactoryTest {

    @Test
    fun `deterministic ids are reproducible`() {
        val factory = IdFactory()

        assertEquals(
            factory.deterministic("Process_A", "instance-1"),
            factory.deterministic("Process_A", "instance-1")
        )
    }
}
