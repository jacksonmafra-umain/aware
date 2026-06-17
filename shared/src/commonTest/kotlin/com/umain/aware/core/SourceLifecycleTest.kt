package com.umain.aware.core

import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Verifies the lifecycle contract every source must honour: register on first collection, and
 * unregister exactly once when collection is cancelled (this is what guarantees no leaked listeners
 * when a screen leaves composition).
 */
class SourceLifecycleTest {

    @Test
    fun sensorSource_registers_on_collect_and_unregisters_on_cancel() = runTest {
        val source = FakeSensorSource()

        val job = launch { source.sensors(listOf(SensorType.ACCELEROMETER)).collect() }
        runCurrent()

        assertEquals(listOf(SensorType.ACCELEROMETER), source.lastRegistered)
        assertEquals(1, source.registerCount)
        assertEquals(0, source.unregisterCount, "must not unregister while still collecting")

        job.cancelAndJoin()
        assertEquals(1, source.unregisterCount, "must unregister exactly once on cancellation")
    }

    @Test
    fun stateSource_registers_on_collect_and_unregisters_on_cancel() = runTest {
        val source = FakeStateSource()

        val job = launch { source.states(listOf(StateType.BATTERY)).collect() }
        runCurrent()

        assertEquals(listOf(StateType.BATTERY), source.lastRegistered)
        assertEquals(1, source.registerCount)
        assertEquals(0, source.unregisterCount)

        job.cancelAndJoin()
        assertEquals(1, source.unregisterCount)
    }
}
