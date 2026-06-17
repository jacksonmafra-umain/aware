package com.umain.aware.feature.privacy

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ShieldPolicyTest {

    @Test
    fun visible_unlocked_screen_on_shows_content() {
        assertFalse(ShieldPolicy.hidden(appVisible = true, locked = false, screenOn = true))
    }

    @Test
    fun any_unsafe_condition_hides_content() {
        assertTrue(ShieldPolicy.hidden(appVisible = false, locked = false, screenOn = true), "backgrounded")
        assertTrue(ShieldPolicy.hidden(appVisible = true, locked = true, screenOn = true), "locked")
        assertTrue(ShieldPolicy.hidden(appVisible = true, locked = false, screenOn = false), "screen off")
    }
}
