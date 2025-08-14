package com.example.config.di

import com.example.application.port.TransactionRunner
import com.example.config.getInvoiceFeeRate
import com.example.config.getInvoiceTaxRate
import com.example.domain.payable.model.valueobject.Rate
import com.example.domain.payable.repository.InvoiceRepository
import com.example.domain.payable.service.InvoiceService
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

        single {
            val config = get<ApplicationConfig>()
            InvoiceService(
                get<InvoiceRepository>(),
                get<TransactionRunner>(),
                Rate.of(config.getInvoiceFeeRate()),
                Rate.of(config.getInvoiceTaxRate()),
            )
        }
        single { InvoiceController(get<InvoiceService>()) }
    }
