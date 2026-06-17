package com.umain.aware.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion

/**
 * Drop-in [SensorSource] for tests (LSP). Records register/unregister calls and lets a test push
 * arbitrary updates, while honouring the same lifecycle contract as [KSensorSource]: the flow is
 * cold and increments [unregisterCount] on completion/cancellation.
 */
class FakeSensorSource : SensorSource {
    var lastRegistered: List<SensorType>? = null
        private set
    var registerCount = 0
        private set
    var unregisterCount = 0
        private set

    private val channel = MutableSharedFlow<SensorUpdate>(extraBufferCapacity = 64)

    /** Push an update to any active collector. */
    suspend fun emit(update: SensorUpdate) = channel.emit(update)

    override fun sensors(types: List<SensorType>, intervalMs: Long?): Flow<SensorUpdate> = flow {
        lastRegistered = types
        registerCount++
        emitAll(channel)
    }.onCompletion { unregisterCount++ }
}

/** Drop-in [StateSource] for tests (LSP). Mirrors [FakeSensorSource]. */
class FakeStateSource : StateSource {
    var lastRegistered: List<StateType>? = null
        private set
    var registerCount = 0
        private set
    var unregisterCount = 0
        private set

    private val channel = MutableSharedFlow<StateUpdate>(extraBufferCapacity = 64)

    suspend fun emit(update: StateUpdate) = channel.emit(update)

    override fun states(types: List<StateType>): Flow<StateUpdate> = flow {
        lastRegistered = types
        registerCount++
        emitAll(channel)
    }.onCompletion { unregisterCount++ }
}
