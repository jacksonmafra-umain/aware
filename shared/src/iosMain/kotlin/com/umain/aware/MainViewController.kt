package com.umain.aware

import androidx.compose.ui.window.ComposeUIViewController
import com.umain.aware.di.initKoin

private var koinStarted = false

/**
 * iOS entry point. Starts Koin exactly once (the view controller may be created more than once)
 * and then hands off to the shared `App()`. A plain flag is used instead of Koin's GlobalContext,
 * which isn't available on the Kotlin/Native target.
 */
fun MainViewController() = ComposeUIViewController {
    if (!koinStarted) {
        initKoin()
        koinStarted = true
    }
    App()
}
