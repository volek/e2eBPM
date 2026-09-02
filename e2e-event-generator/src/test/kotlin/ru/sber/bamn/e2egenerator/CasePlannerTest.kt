package ru.sber.bamn.e2egenerator

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ru.sber.bamn.e2egenerator.scenario.CaseDistribution
import ru.sber.bamn.e2egenerator.scenario.CasePlanner
import ru.sber.bamn.e2egenerator.scenario.CaseType

class CasePlannerTest {

    @Test
    fun `plan expands case distribution`() {
        val plan = CasePlanner.plan(
            CaseDistribution(
                happy = 2,
                delayedLegacy = 1,
                missingLegacy = 1,
                missingProcessB = 1,
                retry = 1,
                failedProcess = 1,
                duplicateEvent = 1,
                ambiguous = 1,
                invalidContract = 1
            )
        )

        assertEquals(10, plan.size)
        assertEquals(2, plan.count { it == CaseType.HAPPY })
        assertEquals(1, plan.count { it == CaseType.RETRY })
        assertEquals(1, plan.count { it == CaseType.INVALID_CONTRACT })
    }
}
