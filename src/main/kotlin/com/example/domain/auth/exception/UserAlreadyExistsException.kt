package com.example.domain.auth.exception

/**
 * ユーザーが既に存在する場合の例外
 */
class UserAlreadyExistsException(message: String) : Exception(message)
