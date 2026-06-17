package com.umain.aware.core

actual object AwareLog {
    actual fun d(tag: String, message: String) {
        println("D/$tag: $message")
    }

    actual fun e(tag: String, message: String, throwable: Throwable?) {
        val suffix = throwable?.let { "\n" + it.stackTraceToString() } ?: ""
        println("E/$tag: $message$suffix")
    }
}
