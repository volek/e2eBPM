package ru.sber.bamn.e2egenerator

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ru.sber.bamn.e2egenerator.contract.NodeState
import ru.sber.bamn.e2egenerator.contract.ProcessState

class StateContractTest {

    @Test
    fun `process states match Flow contract`() {
        assertEquals(1, ProcessState.RUNNING.code)
        assertEquals(2, ProcessState.COMPLETED.code)
        assertEquals(3, ProcessState.FAILED.code)
        assertEquals(4, ProcessState.SUSPENDED.code)
        assertEquals(5, ProcessState.INCIDENT.code)
    }

    @Test
    fun `node states match Flow contract`() {
        assertEquals(0, NodeState.RUNNING.code)
        assertEquals(2, NodeState.FAILED.code)
        assertEquals(3, NodeState.INTERRUPTED.code)
        assertEquals(4, NodeState.COMPLETED.code)
    }
}
