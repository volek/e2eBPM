package ru.sber.bamn.e2egenerator.validation

enum class ValidationMode {
    NORMAL,
    NEGATIVE
}

data class ValidationContext(
    val mode: ValidationMode = ValidationMode.NORMAL,
    val allowInvalidContract: Boolean = false
)
