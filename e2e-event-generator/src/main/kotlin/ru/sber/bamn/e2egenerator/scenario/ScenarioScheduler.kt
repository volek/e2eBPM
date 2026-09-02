package ru.sber.bamn.e2egenerator.scenario

import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class ScenarioScheduler {
    fun delay(milliseconds: Long, jitterMs: Long = 0, seed: Long = 0L, caseIndex: Int = 0) {
        val jitter = if (jitterMs > 0) {
            Random(seed + caseIndex).nextLong(0, jitterMs + 1)
        } else {
            0L
        }
        val total = milliseconds + jitter
        if (total > 0) {
            Thread.sleep(total)
        }
    }
}
