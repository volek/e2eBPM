package ru.sber.bamn.e2egenerator.scenario

enum class CaseType {
    HAPPY,
    DELAYED_LEGACY,
    MISSING_LEGACY,
    MISSING_PROCESS_B,
    RETRY,
    FAILED_PROCESS,
    DUPLICATE_EVENT,
    AMBIGUOUS,
    INVALID_CONTRACT
}
