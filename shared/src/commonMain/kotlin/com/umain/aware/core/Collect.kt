package com.umain.aware.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember

/**
 * Reusable Compose plumbing for consuming a [SensorSource] / [StateSource]. Written once here and
 * reused by every screen (DRY) — no screen re-implements register/collect/unregister.
 *
 * The lifecycle guarantee comes for free: each helper collects inside the calling composition's
 * scope, so when the screen leaves composition the collection is cancelled, which fires the
 * source's `onCompletion` and unregisters the sensors/states. Lists are compared structurally, so
 * passing a freshly-built `listOf(...)` on each recomposition does not restart the subscription.
 */

/** Latest [SensorUpdate] (or null before the first emission) for [types]. */
@Composable
fun rememberSensor(
    source: SensorSource,
    types: List<SensorType>,
    intervalMs: Long? = null,
): State<SensorUpdate?> =
    remember(source, types, intervalMs) { source.sensors(types, intervalMs) }
        .collectAsState(initial = null)

/** Latest [StateUpdate] (or null before the first emission) for [types]. */
@Composable
fun rememberState(
    source: StateSource,
    types: List<StateType>,
): State<StateUpdate?> =
    remember(source, types) { source.states(types) }
        .collectAsState(initial = null)

/**
 * Delivers every [SensorUpdate] to [onUpdate]. Use when a feature needs each event (e.g. shake
 * counts, signature points), not just the latest value.
 */
@Composable
fun CollectSensors(
    source: SensorSource,
    types: List<SensorType>,
    intervalMs: Long? = null,
    onUpdate: (SensorUpdate) -> Unit,
) {
    LaunchedEffect(source, types, intervalMs) {
        source.sensors(types, intervalMs).collect(onUpdate)
    }
}

/** Delivers every [StateUpdate] to [onUpdate]. */
@Composable
fun CollectStates(
    source: StateSource,
    types: List<StateType>,
    onUpdate: (StateUpdate) -> Unit,
) {
    LaunchedEffect(source, types) {
        source.states(types).collect(onUpdate)
    }
}
