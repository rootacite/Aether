package com.acitelight.aether.helper

fun String.getFileNameFromUrl(): String? {
    return try {
        val url = java.net.URL(this)
        val path = url.path
        if (path.isNotEmpty()) {
            path.substringAfterLast('/').takeIf { it.isNotEmpty() }
        } else {
            null
        }
    } catch (e: Exception) {
        val path = this.substringAfter("://").substringAfter('/')
        if (path.contains('/')) {
            path.substringAfterLast('/').takeIf { it.isNotEmpty() }
        } else {
            path.takeIf { it.isNotEmpty() && it.contains('.') }
        }
    }
}