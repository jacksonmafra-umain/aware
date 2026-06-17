package com.umain.aware.feature.tilt

/**
 * A marble rolling inside a unit box, driven by tilt. Position [x]/[y] are normalised to 0..1
 * (top-left origin). Each tick adds the tilt to the velocity, applies friction, integrates the
 * position, and bounces off the four walls with damping. Pure and platform-free (SRP) — the screen
 * maps the accelerometer onto screen-space tilt and feeds it here.
 *
 * @param accelScale how strongly tilt accelerates the marble.
 * @param friction velocity retained each tick (0..1); lower = more drag.
 * @param wallBounce fraction of speed kept when hitting a wall.
 */
class MarbleBox(
    private val accelScale: Float = 12f,
    private val friction: Float = 0.88f,
    private val wallBounce: Float = 0.5f,
) {
    var x: Float = 0.5f
        private set
    var y: Float = 0.5f
        private set

    private var vx = 0f
    private var vy = 0f

    /**
     * Advance the simulation. [tiltX]/[tiltY] are screen-space gravity components (right/down
     * positive); [dtSeconds] is the time since the previous sample (clamped to keep it stable).
     */
    fun onTilt(tiltX: Float, tiltY: Float, dtSeconds: Float) {
        val dt = dtSeconds.coerceIn(0f, 0.05f)

        vx = (vx + tiltX * accelScale * dt) * friction
        vy = (vy + tiltY * accelScale * dt) * friction

        x += vx * dt
        y += vy * dt

        if (x < 0f) { x = 0f; vx = -vx * wallBounce }
        else if (x > 1f) { x = 1f; vx = -vx * wallBounce }

        if (y < 0f) { y = 0f; vy = -vy * wallBounce }
        else if (y > 1f) { y = 1f; vy = -vy * wallBounce }
    }

    fun reset() {
        x = 0.5f
        y = 0.5f
        vx = 0f
        vy = 0f
    }
}
