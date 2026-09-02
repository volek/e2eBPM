package ru.sber.bamn.e2egenerator.scenario

data class Scenario(
    val name: String,
    val seed: Long = 1001,
    val timing: Timing = Timing(),
    val processes: List<ProcessDefinition> = emptyList(),
    val legacy: List<LegacyDefinition> = emptyList(),
    val cases: CaseDistribution = CaseDistribution()
)

data class Timing(
    val delayMsBetweenSnapshots: Long = 300,
    val jitterMs: Long = 0
)

data class ProcessDefinition(
    val id: String,
    val definitionId: String,
    val name: String,
    val version: Int = 1,
    val bpmnResource: String,
    val moduleId: String = "bpmx",
    val instanceCount: Int = 10,
    val correlationVariable: String,
    val nodes: List<ScenarioNode>
)

data class LegacyDefinition(
    val id: String,
    val definitionId: String,
    val name: String,
    val moduleId: String,
    val instanceCount: Int = 10,
    val correlationVariable: String,
    val nodes: List<ScenarioNode>
)

data class ScenarioNode(
    val id: String,
    val name: String,
    val type: String
)

data class CaseDistribution(
    val happy: Int = 10,
    val delayedLegacy: Int = 0,
    val missingLegacy: Int = 0,
    val missingProcessB: Int = 0,
    val retry: Int = 0,
    val failedProcess: Int = 0,
    val duplicateEvent: Int = 0,
    val ambiguous: Int = 0,
    val invalidContract: Int = 0
)
