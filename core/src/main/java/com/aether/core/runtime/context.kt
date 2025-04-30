package com.aether.core.runtime

import java.util.Collections

typealias Generator = () -> Any?

class ContextDependencyCycleException(cycle: List<Class<*>>) :
    Exception("Dependency cycle detected: ${cycle.joinToString(" -> ")}")

const val contextKey = "context"

val context2: AppContext
    get() = AppContextManager.context

class AppContext(
    private var parent: AppContext?,
    val name: String?,
    private val overrides: Map<Class<*>, Generator>,
    private val fallbacks: Map<Class<*>, Generator>
) {
    private val values = mutableMapOf<Class<*>, Any?>()
    private val reentrantChecks = mutableListOf<Class<*>>()

    companion object {
        val root = AppContext(null, "ROOT", emptyMap(), emptyMap())
    }

    private fun boxNull(value: Any?): Any? = value ?: BoxedNull

    private fun unboxNull(value: Any?): Any? = if (value == BoxedNull) null else value

    private fun generateIfNecessary(type: Class<*>, generators: Map<Class<*>, Generator>): Any? {
        if (!generators.containsKey(type)) {
            return null
        }

        return values.getOrPut(type) {
            val index = reentrantChecks.indexOf(type)
            if (index >= 0) {
                throw ContextDependencyCycleException(reentrantChecks.subList(index, 0))
            }

            reentrantChecks.add(type)
            try {
                return  boxNull(generators[type]?.invoke())
            } finally {
                reentrantChecks.removeAt(reentrantChecks.lastIndex)
            }
        }
    }

    fun <T : Any> get(type: Class<T>): T? {
        var value = generateIfNecessary(type, overrides)
        if (value == null && parent != null) {
            value = parent!!.get(type)
        }
        return unboxNull(value ?: generateIfNecessary(type, fallbacks)) as T?
    }

    fun run(
        name: String? = null,
        body: () -> Any?,
        overrides: Map<Class<*>, Generator>? = null,
        fallbacks: Map<Class<*>, Generator>? = null
    ): Any? {
        val child = AppContext(
            this,
            name,
            Collections.unmodifiableMap(overrides ?: emptyMap()),
            Collections.unmodifiableMap(fallbacks ?: emptyMap())
        )
        val previousContext = AppContextManager.get()
        child.parent = previousContext
        AppContextManager.set(child)
        try {
            return body()
        } finally {
            AppContextManager.set(child)
        }
    }

//    suspend fun run(
//        body: suspend () -> Any?,
//        name: String? = null,
//        overrides: Map<Class<*>, Generator>? = null,
//        fallbacks: Map<Class<*>, Generator>? = null
//    ): Any? {
//        val child = AppContext(
//            this,
//            name,
//            overrides?.toMap() ?: emptyMap(),
//            fallbacks?.toMap() ?: emptyMap()
//        )
//        return withContext(currentCoroutineContext() + mapOf(contextKey to child)) {
//            try {
//                body()
//            } catch (e: Exception) {
//                val astRuntime = child.get(AstRuntime::class)
//                if (astRuntime != null && astRuntime.errorCallback != null) {
//                    astRuntime.errorCallback(e, e.stackTrace)
//                }
//                throw e
//            }
//        }
//    }

    override fun toString(): String {
        val buf = StringBuilder()
        var indent = ""
        var ctx: AppContext? = this
        while (ctx != null) {
            buf.append("AppContext")
            if (ctx.name != null) {
                buf.append("[${ctx.name}]")
            }
            if (ctx.overrides.isNotEmpty()) {
                buf.append("\n$indent  overrides: [${ctx.overrides.keys.joinToString(", ")}]")
            }
            if (ctx.fallbacks.isNotEmpty()) {
                buf.append("\n$indent  fallbacks: [${ctx.fallbacks.keys.joinToString(", ")}]")
            }
            if (ctx.parent != null) {
                buf.append("\n$indent  parent: ")
            }
            ctx = ctx.parent
            indent += "  "
        }
        return buf.toString()
    }
}

object BoxedNull
