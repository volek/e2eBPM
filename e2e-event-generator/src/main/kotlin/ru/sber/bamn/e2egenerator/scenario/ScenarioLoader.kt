package ru.sber.bamn.e2egenerator.scenario

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component

@Component
class ScenarioLoader {
    private val yamlMapper = ObjectMapper(YAMLFactory()).registerModule(KotlinModule())

    fun load(name: String): Scenario {
        val resource = ClassPathResource("scenarios/$name.yml")
        resource.inputStream.use {
            return yamlMapper.readValue(it, Scenario::class.java)
        }
    }
}
