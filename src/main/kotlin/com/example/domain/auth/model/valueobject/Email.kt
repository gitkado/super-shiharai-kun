package com.example.domain.auth.model.valueobject

data class Email(val value: String) {
    init {
        require(value.isNotBlank()) { "Email address must not be blank" }
        require(isValidEmailFormat(value)) { "Invalid email format" }
    }

    override fun toString(): String = value

    companion object {
        private fun isValidEmailFormat(email: String): Boolean {
            val emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$".toRegex()
            return emailPattern.matches(email)
        }
    }
}
