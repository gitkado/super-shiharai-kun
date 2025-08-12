package com.example.infrastructure.database.schema.customtypes

import org.jetbrains.exposed.sql.*

/**
 * PostgreSQL citext 型のカスタムカラム型定義
 * 大文字小文字を区別しない文字列型
 */
class CitextColumnType : ColumnType<String>() {
    override fun sqlType(): String = "CITEXT"

    override fun valueFromDB(value: Any): String = value.toString()
}

/**
 * Table用のcitext拡張関数
 * PostgreSQLのcitext型を使用する列を定義
 */
fun Table.citext(name: String): Column<String> = registerColumn(name, CitextColumnType())
