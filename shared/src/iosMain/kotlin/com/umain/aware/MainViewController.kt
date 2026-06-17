package com.umain.aware

import androidx.compose.ui.window.ComposeUIViewController
import com.umain.aware.di.initKoin
import org.koin.core.context.GlobalContext

/**
 * iOS entry point. Starts Koin once (guarded, since the view controller may be recreated) and then
 * hands off to the shared `App()`.
 */
fun MainViewController() = ComposeUIViewController {
    if (GlobalContext.getOrNull() == null) {
        initKoin()
    }
    App()
}
