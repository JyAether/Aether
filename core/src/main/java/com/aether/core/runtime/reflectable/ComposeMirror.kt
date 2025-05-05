package com.aether.core.runtime.reflectable

import androidx.compose.runtime.Composable

object ComposeMirror {
    private val reflectors = mutableMapOf<String, ComposeReflector>()

    fun register(name: String, reflector: ComposeReflector) {
        reflectors[name] = reflector
    }

    fun getReflector(name: String): ComposeReflector? {
        return reflectors[name]
    }

    @Composable
    fun render(name: String, args: Map<String, Any?>?) {
        reflectors[name]?.invoke(args) ?: error("No reflector found for $name")
    }
}