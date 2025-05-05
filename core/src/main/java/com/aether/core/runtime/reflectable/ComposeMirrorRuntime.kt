package com.aether.core.runtime.reflectable

import androidx.compose.runtime.Composable
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties

class ComposeMirrorRuntime {
    private val mirrorCache = mutableMapOf<String, Any>()

    fun <T : Any> createMirror(clazz: KClass<T>, params: List<Any?> = emptyList()): T {
        val mirrorClassName = "${clazz.simpleName}Mirror"
        val mirrorClass = try {
            Class.forName("${clazz.qualifiedName}$mirrorClassName")
        } catch (e: ClassNotFoundException) {
            throw IllegalStateException("Mirror class not found for ${clazz.qualifiedName}. Make sure the class is annotated with @ComposeMirror")
        }

        val mirrorInstance = mirrorClass.getDeclaredMethod("invoke", List::class.java)
            .invoke(null, params) as T

        mirrorCache[clazz.qualifiedName!!] = mirrorInstance
        return mirrorInstance
    }

    fun invokeComposable(mirror: Any, functionName: String, vararg args: Any?): Any? {
        val clazz = mirror::class
        val composableFunction = clazz.memberFunctions.find { it.name == functionName }
            ?: throw IllegalArgumentException("No composable function named $functionName found")

        if (!composableFunction.annotations.any { it is Composable }) {
            throw IllegalArgumentException("Function $functionName is not a @Composable function")
        }

        return composableFunction.call(mirror, *args)
    }

    fun getProperty(mirror: Any, propertyName: String): Any? {
        val clazz = mirror::class
        val property = clazz.memberProperties.find { it.name == propertyName }
            ?: throw IllegalArgumentException("No property named $propertyName found")

        return property.getter.call(mirror)
    }

    fun setProperty(mirror: Any, propertyName: String, value: Any?) {
        val clazz = mirror::class
        val property = clazz.memberProperties.find { it.name == propertyName }
            ?: throw IllegalArgumentException("No property named $propertyName found")

        if (property is kotlin.reflect.KMutableProperty<*>) {
            property.setter.call(mirror, value)
        } else {
            throw IllegalArgumentException("Property $propertyName is not mutable")
        }
    }
} 