package com.example.config.di

import org.koin.dsl.module

/**
 * コアモジュール - 基本的な依存関係
 */
val coreModule =
    module {
        // ApplicationインスタンスをKoinで管理
        // Note: これはinstall(Koin)の後に設定される
    }

/**
 * メインアプリケーションモジュール
 * 全てのKoinモジュールを統合
 */
val applicationModules =
    listOf(
        coreModule,
        databaseModule,
        authModule,
        payableModule,
    )
