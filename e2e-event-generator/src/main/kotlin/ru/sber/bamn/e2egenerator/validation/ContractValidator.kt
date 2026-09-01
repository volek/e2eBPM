package ru.sber.bamn.e2egenerator.validation

import org.springframework.stereotype.Component
import ru.sber.bamn.e2egenerator.contract.CommonHeaders
import ru.sber.bamn.e2egenerator.contract.ProcessInstanceEvent
import ru.sber.bamn.e2egenerator.contract.ProcessModelEvent

@Component
class ContractValidator {

    fun validate(headers: CommonHeaders, event: ProcessModelEvent) {
        require(headers.ceId.isNotBlank()) { "ce_id must not be blank" }
        require(event.processId.isNotBlank()) { "processId must not be blank" }
        require(event.processDefinitionId.isNotBlank()) { "processDefinitionId must not be blank" }
        require(event.schema.isNotBlank()) { "schema must not be blank" }
        require(event.moduleId.isNotBlank()) { "moduleId must not be blank" }
    }

    fun validate(headers: CommonHeaders, event: ProcessInstanceEvent) {
        require(headers.ceId.isNotBlank()) { "ce_id must not be blank" }
        require(event.id.isNotBlank()) { "id must not be blank" }
        require(event.rootInstanceId.isNotBlank()) { "rootInstanceId must not be blank" }
        require(event.processId.isNotBlank()) { "processId must not be blank" }
        require(event.processDefinitionId.isNotBlank()) { "processDefinitionId must not be blank" }
        require(event.retryCount >= 0) { "retryCount must be >= 0" }
    }
}
