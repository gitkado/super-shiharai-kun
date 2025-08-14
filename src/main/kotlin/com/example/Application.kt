package com.example

import com.example.config.database.configureDatabases
import com.example.config.di.applicationModules
import com.example.config.http.configureHTTP
import com.example.config.monitoring.configureMonitoring
import com.example.config.security.configureSecurity
import com.example.config.serialization.configureSerialization
import com.example.presentation.routing.configureRouting
import io.ktor.server.application.*
import io.ktor.server.plugins.swagger.*
import org.koin.ktor.ext.getKoin
import org.koin.ktor.plugin.Koin
import org.koin.ktor.plugin.KoinApplicationStarted
import org.koin.ktor.plugin.KoinApplicationStopPreparing
import org.koin.ktor.plugin.KoinApplicationStopped
import org.koin.logger.slf4jLogger

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    // Koin DI設定 - 他の設定より先に初期化
    install(Koin) {
        slf4jLogger()
        modules(applicationModules)
    }

    // KoinにApplicationインスタンスを登録
    getKoin().declare(this)

    // KoinにApplicationConfigを登録
    getKoin().declare(environment.config)

    // Koinライフサイクルイベント監視
    monitor.subscribe(KoinApplicationStarted) {
        log.info("🚀 Koin DI container started successfully")
        log.info("📦 Registered modules: ${applicationModules.size} modules")
    }

    monitor.subscribe(KoinApplicationStopPreparing) {
        log.info("⏹️  Koin DI container stopping...")
    }

    monitor.subscribe(KoinApplicationStopped) {
        log.info("🛑 Koin DI container stopped successfully")
    }

    configureMonitoring()
    configureHTTP()
    configureSerialization()
    configureDatabases()
    configureSecurity()
    configureRouting()
}
