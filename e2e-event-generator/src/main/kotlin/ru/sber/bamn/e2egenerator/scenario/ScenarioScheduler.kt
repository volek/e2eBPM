package ru.sber.bamn.e2egenerator.scenario

import org.springframework.stereotype.Component

@Component
class ScenarioScheduler {
    fun delay(milliseconds: Long) {
        if (milliseconds > 0) {
            Thread.sleep(milliseconds)
        }
    }
}
