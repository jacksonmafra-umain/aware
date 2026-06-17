package com.umain.aware.core

/**
 * Minimal multiplatform logger. Implemented per platform (`android.util.Log` on Android, `println`
 * to the Xcode console on iOS) so common code — chiefly the source layer — can log lifecycle and
 * errors the same way on both targets.
 */
expect object AwareLog {
    fun d(tag: String, message: String)
    fun e(tag: String, message: String, throwable: Throwable? = null)
}
