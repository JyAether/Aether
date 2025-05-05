package com.aether.core.runtime.reflectable

import androidx.compose.runtime.Composable
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties

class ComposeReflectRuntime {
    private val reflectCache = mutableMapOf<String, Any>()

    fun <T : Any> createReflect(clazz: KClass<T>): T {
        val reflectClassName = "${clazz.simpleName}Reflect"
        val reflectClass = try {
            Class.forName("${clazz.qualifiedName}$reflectClassName")
        } catch (e: ClassNotFoundException) {
            throw IllegalStateException("Reflect class not found for ${clazz.qualifiedName}. Make sure the class is annotated with @ComposeReflect")
        }

        val instance = clazz.createInstance()
        val reflectInstance = reflectClass.getDeclaredConstructor(clazz.java)
            .newInstance(instance) as T

        reflectCache[clazz.qualifiedName!!] = reflectInstance
        return reflectInstance
    }

    fun invokeComposable(reflect: Any, functionName: String, vararg args: Any?): Any? {
        val clazz = reflect::class
        val reflectFunction = clazz.memberFunctions.find { it.name == "invoke$functionName" }
            ?: throw IllegalArgumentException("No reflectable function named $functionName found")

        return reflectFunction.call(reflect, *args)
    }

    fun getProperty(reflect: Any, propertyName: String): Any? {
        val clazz = reflect::class
        val property = clazz.memberProperties.find { it.name == propertyName }
            ?: throw IllegalArgumentException("No property named $propertyName found")

        return property.getter.call(reflect)
    }

    fun setProperty(reflect: Any, propertyName: String, value: Any?) {
        val clazz = reflect::class
        val property = clazz.memberProperties.find { it.name == propertyName }
            ?: throw IllegalArgumentException("No property named $propertyName found")

        if (property is kotlin.reflect.KMutableProperty<*>) {
            property.setter.call(reflect, value)
        } else {
            throw IllegalArgumentException("Property $propertyName is not mutable")
        }
    }
} 