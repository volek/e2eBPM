package ru.sber.bamn.e2egenerator.validation

import org.springframework.stereotype.Component
import ru.sber.bamn.e2egenerator.contract.CommonHeaders
import ru.sber.bamn.e2egenerator.contract.ProcessInstanceEvent
import ru.sber.bamn.e2egenerator.contract.ProcessModelEvent
import ru.sber.bamn.e2egenerator.contract.ProcessState

@Component
class ContractValidator {

    fun validate(
        headers: CommonHeaders,
        event: ProcessModelEvent,
        context: ValidationContext = ValidationContext()
    ) {
        if (context.allowInvalidContract && context.mode == ValidationMode.NEGATIVE) {
            return
        }

        require(headers.ceId.isNotBlank()) { "ce_id must not be blank" }
        require(event.processId.isNotBlank()) { "processId must not be blank" }
        require(event.processDefinitionId.isNotBlank()) { "processDefinitionId must not be blank" }
        require(event.schema.isNotBlank()) { "schema must not be blank" }
        require(event.moduleId.isNotBlank()) { "moduleId must not be blank" }
    }

    fun validate(
        headers: CommonHeaders,
        event: ProcessInstanceEvent,
        context: ValidationContext = ValidationContext()
    ) {
        if (context.allowInvalidContract && context.mode == ValidationMode.NEGATIVE) {
            return
        }

        require(headers.ceId.isNotBlank()) { "ce_id must not be blank" }
        require(event.id.isNotBlank()) { "id must not be blank" }
        require(event.rootInstanceId.isNotBlank()) { "rootInstanceId must not be blank" }
        require(event.processId.isNotBlank()) { "processId must not be blank" }
        require(event.processDefinitionId.isNotBlank()) { "processDefinitionId must not be blank" }
        require(event.retryCount >= 0) { "retryCount must be >= 0" }
        require(event.variables.isNotEmpty()) { "variables must be present as object" }

        event.state?.let { state ->
            require(ProcessState.values().any { it.code == state }) {
                "process state code must be a known ProcessState value"
            }
        }
    }
}
