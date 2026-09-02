package ru.sber.bamn.e2egenerator.scenario

object CasePlanner {

    fun plan(cases: CaseDistribution): List<CaseType> {
        val entries = mutableListOf<CaseType>()
        repeat(cases.happy) { entries.add(CaseType.HAPPY) }
        repeat(cases.delayedLegacy) { entries.add(CaseType.DELAYED_LEGACY) }
        repeat(cases.missingLegacy) { entries.add(CaseType.MISSING_LEGACY) }
        repeat(cases.missingProcessB) { entries.add(CaseType.MISSING_PROCESS_B) }
        repeat(cases.retry) { entries.add(CaseType.RETRY) }
        repeat(cases.failedProcess) { entries.add(CaseType.FAILED_PROCESS) }
        repeat(cases.duplicateEvent) { entries.add(CaseType.DUPLICATE_EVENT) }
        repeat(cases.ambiguous) { entries.add(CaseType.AMBIGUOUS) }
        repeat(cases.invalidContract) { entries.add(CaseType.INVALID_CONTRACT) }
        return entries
    }

    fun totalInstances(cases: CaseDistribution): Int = plan(cases).size
}
