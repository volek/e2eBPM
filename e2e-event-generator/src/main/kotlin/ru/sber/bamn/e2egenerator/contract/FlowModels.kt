package ru.sber.bamn.e2egenerator.contract

import com.fasterxml.jackson.databind.JsonNode

data class ProcessModelEvent(
    val processId: String,
    val processDefinitionId: String,
    val resourceName: String?,
    val processName: String?,
    val processVersion: String?,
    val businessFamily: String?,
    val ownerRole: String?,
    val contextVisible: Boolean,
    val processVersionInternal: String,
    val schema: String,
    val moduleId: String,
    val bamProjectId: String?,
    val deleteReason: String?,
    val instancesSuspended: Boolean,
    val suspended: Boolean,
    val retryPolicyModels: List<RetryPolicyModel>,
    val operation: Operation?,
    val created: String,
    val tags: Map<String, List<String>?>,
    val maskPatterns: List<MaskPatternItem>
)

data class ProcessInstanceEvent(
    val id: String,
    val parentInstanceId: String?,
    val rootInstanceId: String,
    val processId: String,
    val processDefinitionId: String,
    val resourceName: String?,
    val rootProcessId: String?,
    val processName: String?,
    val startDate: String,
    val endDate: String?,
    val state: Int?,
    val businessKey: String?,
    val version: Int,
    val bamProjectId: String?,
    val extIds: Map<String, JsonNode>?,
    val error: ErrorInfo?,
    val moduleId: String,
    val engineVersion: String?,
    val enginePodName: String?,
    val retryCount: Int,
    val ownerRole: String?,
    val idempotencyKey: String?,
    val operation: Operation?,
    val nodeInstances: List<NodeInstance>,
    val variables: Map<String, JsonNode>,
    val contextSize: Int?
)

data class NodeInstance(
    val id: String,
    val nodeId: String,
    val nodeDefinitionId: String,
    val nodeName: String?,
    val nodeType: String,
    val error: String?,
    val state: Int,
    val calledProcessInstanceIds: String?,
    val retries: List<RetryInstance>,
    val htmTaskId: String?,
    val triggerTime: String,
    val leaveTime: String?,
    val triggerNodeInstanceId: String?,
    val creationOrder: Int
)

data class ErrorInfo(
    val nodeId: String,
    val errorMessage: String?
)

data class RetryInstance(
    val id: String,
    val reason: String,
    val result: String?,
    val time: String,
    val strategy: String,
    val planedDate: String,
    val factStartDate: String?,
    val factEndDate: String?,
    val retryPolicyId: String?
)

data class RetryPolicyModel(
    val retryModelDefinitionId: String,
    val retryModelName: String,
    val description: String?,
    val policies: List<Policy>
)

data class Policy(
    val id: String,
    val description: String,
    val exceptions: List<String>?,
    val errorExpressions: Map<String, List<String>>?,
    val exceptionMessages: List<String>?,
    val errorCodes: List<String>?,
    val retryStrategy: String,
    val linearCount: Int?,
    val linearTimeout: String?,
    val intervals: List<String>?
)

data class Operation(
    val id: String,
    val type: Int,
    val userLogin: String,
    val moduleIds: String,
    val createdTime: String,
    val totalCount: Long
)

data class MaskPatternItem(
    val jsonPath: String,
    val options: MaskPatternOptions?
)

data class MaskPatternOptions(
    val regExp: String,
    val regExpMode: String
)
