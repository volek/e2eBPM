package ru.sber.bamn.e2egenerator.application

import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import ru.sber.bamn.e2egenerator.config.GeneratorProperties
import ru.sber.bamn.e2egenerator.scenario.ScenarioEngine
import ru.sber.bamn.e2egenerator.scenario.ScenarioLoader
import ru.sber.bamn.e2egenerator.scenario.ScenarioScheduler

@Component
class GeneratorRunner(
    private val properties: GeneratorProperties,
    private val scenarioLoader: ScenarioLoader,
    private val scenarioEngine: ScenarioEngine,
    private val scheduler: ScenarioScheduler
) : CommandLineRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun run(vararg args: String?) {
        when (properties.command.toLowerCase()) {
            "run" -> runScenarioLoop()
            else -> error("Unsupported command=${properties.command}. Supported: run")
        }
    }

    private fun runScenarioLoop() {
        var iteration = 0
        do {
            iteration++
            val scenario = scenarioLoader.load(properties.scenario)
            log.info(
                "Starting scenario name={} mode={} seed={} iteration={}",
                scenario.name,
                properties.mode,
                properties.seed,
                iteration
            )
            scenarioEngine.run(scenario)

            if (properties.mode.equals("CONTINUOUS", ignoreCase = true)) {
                scheduler.delay(properties.continuousDelayMs, 0, properties.seed, iteration)
            }
        } while (properties.mode.equals("CONTINUOUS", ignoreCase = true))
    }
}
