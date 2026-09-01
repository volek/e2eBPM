package ru.sber.bamn.e2egenerator.application

import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import ru.sber.bamn.e2egenerator.config.GeneratorProperties
import ru.sber.bamn.e2egenerator.scenario.ScenarioEngine
import ru.sber.bamn.e2egenerator.scenario.ScenarioLoader

@Component
class GeneratorRunner(
    private val properties: GeneratorProperties,
    private val scenarioLoader: ScenarioLoader,
    private val scenarioEngine: ScenarioEngine
) : CommandLineRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun run(vararg args: String?) {
        when (properties.command.lowercase()) {
            "run" -> {
                val scenario = scenarioLoader.load(properties.scenario)
                log.info("Starting scenario name={} mode={} seed={}", scenario.name, properties.mode, scenario.seed)
                scenarioEngine.run(scenario)
            }
            else -> {
                error(
                    "Only command=run is implemented in starter scaffold. " +
                        "Add models/flow/legacy/validate/print according to docs/IMPLEMENTATION_PLAN.md"
                )
            }
        }
    }
}
