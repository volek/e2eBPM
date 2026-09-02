package ru.sber.bamn.e2egenerator

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import ru.sber.bamn.e2egenerator.contract.CommonHeaders
import ru.sber.bamn.e2egenerator.contract.ProcessInstanceEvent
import ru.sber.bamn.e2egenerator.validation.ContractValidator
import ru.sber.bamn.e2egenerator.validation.ValidationContext
import ru.sber.bamn.e2egenerator.validation.ValidationMode

class ContractValidatorTest {
    private val validator = ContractValidator()

    @Test
    fun `normal mode rejects invalid instance`() {
        val headers = CommonHeaders("t", "s", "1.0", "time", "id")
        val invalid = ProcessInstanceEvent(
            id = "",
            parentInstanceId = null,
            rootInstanceId = "",
            processId = "",
            processDefinitionId = "",
            resourceName = null,
            rootProcessId = null,
            processName = null,
            startDate = "2020-09-13T12:26:40Z",
            endDate = null,
            state = 1,
            businessKey = null,
            version = 1,
            bamProjectId = null,
            extIds = null,
            error = null,
            moduleId = "bpmx",
            engineVersion = null,
            enginePodName = null,
            retryCount = 0,
            ownerRole = null,
            idempotencyKey = null,
            operation = null,
            nodeInstances = emptyList(),
            variables = mapOf("applicationId" to com.fasterxml.jackson.databind.ObjectMapper().readTree("\"APP\"")),
            contextSize = null
        )

        assertThrows(IllegalArgumentException::class.java) {
            validator.validate(headers, invalid)
        }
    }

    @Test
    fun `negative mode allows invalid contract bypass`() {
        val headers = CommonHeaders("t", "s", "1.0", "time", "id")
        val invalid = ProcessInstanceEvent(
            id = "",
            parentInstanceId = null,
            rootInstanceId = "",
            processId = "",
            processDefinitionId = "",
            resourceName = null,
            rootProcessId = null,
            processName = null,
            startDate = "2020-09-13T12:26:40Z",
            endDate = null,
            state = 1,
            businessKey = null,
            version = 1,
            bamProjectId = null,
            extIds = null,
            error = null,
            moduleId = "bpmx",
            engineVersion = null,
            enginePodName = null,
            retryCount = 0,
            ownerRole = null,
            idempotencyKey = null,
            operation = null,
            nodeInstances = emptyList(),
            variables = emptyMap(),
            contextSize = null
        )

        assertDoesNotThrow {
            validator.validate(
                headers,
                invalid,
                ValidationContext(mode = ValidationMode.NEGATIVE, allowInvalidContract = true)
            )
        }
    }
}
