package org.diego.stash

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform