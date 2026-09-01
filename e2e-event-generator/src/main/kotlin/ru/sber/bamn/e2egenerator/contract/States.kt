package ru.sber.bamn.e2egenerator.contract

enum class ProcessState(val code: Int) {
    RUNNING(1),
    COMPLETED(2),
    FAILED(3),
    SUSPENDED(4),
    INCIDENT(5)
}

enum class NodeState(val code: Int) {
    RUNNING(0),
    FAILED(2),
    INTERRUPTED(3),
    COMPLETED(4)
}
