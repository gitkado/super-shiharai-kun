package com.example.config.di

import com.example.domain.payable.repository.InvoiceRepository
import com.example.infrastructure.database.repository.InvoiceRepositoryImpl
import org.jetbrains.exposed.sql.Database
import org.koin.dsl.module

/**
 * 支払いドメインモジュール - 支払い関連の依存関係
 */
val payableModule =
    module {
        single<InvoiceRepository> { InvoiceRepositoryImpl(get<Database>()) }
    }
