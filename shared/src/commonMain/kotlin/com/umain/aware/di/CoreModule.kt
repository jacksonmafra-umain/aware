package com.umain.aware.di

import com.umain.aware.core.KSensorSource
import com.umain.aware.core.KStateSource
import com.umain.aware.core.SensorSource
import com.umain.aware.core.StateSource
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

/**
 * The single Koin module for Aware.
 *
 * Why Koin at all? `KSensor`/`KState` are global objects, so DI is not needed to *reach* them. It
 * earns its place by satisfying DIP: it is the one spot that binds the [SensorSource] /
 * [StateSource] abstractions to their concrete KSensor implementations. Screens and derivers inject
 * the abstractions and never name the concretes, and tests swap these two bindings for fakes. One
 * module is enough — we deliberately do not over-modularise.
 */
val coreModule: Module = module {
    single<SensorSource> { KSensorSource() }
    single<StateSource> { KStateSource() }
}

/** All modules that make up the app. Adding a feature never requires touching this (OCP). */
fun appModules(): List<Module> = listOf(coreModule)

/**
 * Starts Koin. Platform entry points call this exactly once before showing `App()`.
 * @param appDeclaration optional hook for platforms/tests to contribute extra configuration.
 */
fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()
        modules(appModules())
    }
}
