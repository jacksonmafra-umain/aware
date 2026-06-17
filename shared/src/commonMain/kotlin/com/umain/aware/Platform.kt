package com.umain.aware

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform