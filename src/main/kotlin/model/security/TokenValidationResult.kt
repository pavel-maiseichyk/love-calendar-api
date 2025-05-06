package model.security

data class TokenValidationResult(
    val isValid: Boolean,
    val claims: Map<String, String> = mapOf()
)