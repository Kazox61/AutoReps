package com.kazox.posedetection

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform