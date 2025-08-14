package com.example.config.di

import com.example.domain.payable.repository.InvoiceRepository
import com.example.domain.payable.service.InvoiceService
import com.example.infrastructure.database.Tx
import com.example.infrastructure.database.repository.InvoiceRepositoryImpl
import com.example.presentation.controller.InvoiceController
import io.ktor.server.config.ApplicationConfig
import org.koin.dsl.module

/**
 * 支払いドメインモジュール - 支払い関連の依存関係
 */
val payableModule =
    module {
        single<InvoiceRepository> { InvoiceRepositoryImpl() }
        single { InvoiceService(get<InvoiceRepository>(), get<Tx>(), get<ApplicationConfig>()) }
        single { InvoiceController(get<InvoiceService>()) }
    }
