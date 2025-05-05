package com.aether.core.runtime.proxy

import com.aether.core.runtime.ImportDirective
import com.aether.core.runtime.reflectable.ComposeMirror
import com.aether.core.runtime.reflectable.ComposeReflector
import java.lang.reflect.Modifier
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.jvm.javaMethod

// 假设 BindingBase 是一个基础类
open class BindingBase {
    open fun initInstances() {
        println("BindingBase initInstances called")
    }
}

// ProxyBinding 类，使用继承代替 mixin
class ProxyBinding : BindingBase() {

    companion object {
        private var _instance: ProxyBinding? = null

        // 单例访问方法
        val instance: ProxyBinding?
            get() = _instance
    }

    override fun initInstances() {
        super.initInstances()
        _instance = this
        initializeReflectable()
        initializeProxy()
    }

    // 初始化反射相关的逻辑
    private fun initializeReflectable() {
        println("Initializing reflectable...")
    }

    // 初始化代理相关的逻辑
    private fun initializeProxy() {
        println("Initializing proxy...")
    }

    // 根据类型名称获取类镜像
    fun getProxyClassForName2(typeName: String): ComposeReflector? {
        return ComposeMirror.getReflector(typeName)
    }


    // 根据类型名称获取类镜像
    fun getProxyClassForName(directive: ImportDirective?): KClass<*>? {
        // 假设 p.data 是一个存储类型映射的全局变量
        // 尝试通过反射加载类
        if (directive == null) {
            return null
        }
        return try {
            //parseKtClass(directive.uri, "", ArrayList<String>())
            Class.forName(directive.uri).kotlin
        } catch (e: ClassNotFoundException) {
            println("Class not found for type: ${directive.name}")
            null
        }
    }

    fun parseKtClass(className: String, methodName: String, methodArgs: List<*>) {
        // 假设这是从 PSI 解析生成的 Map<String, Any>
        val parsedMap: Map<String, Any> = mapOf(
            "className" to "org.example.MyClass",
            "methodName" to "myMethod",
            "methodArgs" to listOf("arg1", "arg2")
        )

//        // 动态加载类并调用方法
//        val className = parsedMap["className"] as String
//        val methodName = parsedMap["methodName"] as String
//        val methodArgs = parsedMap["methodArgs"] as List<*>

        try {
            // 1. 动态加载类
            val clazz: Class<*> = Class.forName(className)
            val kClass: KClass<*> = clazz.kotlin

            // 2. 获取目标方法
            val memberFunction = kClass.members.find { it.name == methodName }
                ?: throw NoSuchMethodException("No such method: $methodName")

            // 3. 调用方法
            if (memberFunction is kotlin.reflect.KFunction<*>) {
                // 创建实例（如果需要）
                val instance = if (!clazz.isInterface && !Modifier.isStatic(
                        memberFunction.javaMethod?.modifiers ?: 0
                    )
                ) {
                    clazz.getDeclaredConstructor().newInstance()
                } else {
                    null
                }

                // 调用方法
                val result = memberFunction.call(instance, *methodArgs.toTypedArray())
                println("Result of $methodName: $result")
            } else {
                throw IllegalArgumentException("$methodName is not a callable function")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}


// 假设的全局数据源 p.data
object p {
    val data: Map<String, Class<*>> = mapOf(
        "ExampleType" to ExampleType::class.java
    )
}

// 示例类
class ExampleType

fun mainTest() {
//    // 创建并初始化 ProxyBinding 实例
//    val proxyBinding = ProxyBinding()
//    proxyBinding.initInstances()
//
//    // 获取当前实例
//    val currentInstance = ProxyBinding.instance
//    println("Current ProxyBinding instance: $currentInstance")
//
//    // 测试 getProxyClassForName 方法
//    val proxyClass = currentInstance?.getProxyClassForName("ExampleType")
//    println("Proxy class for 'ExampleType': $proxyClass")
//
//    val invalidClass = currentInstance?.getProxyClassForName("InvalidType")
//    println("Proxy class for 'InvalidType': $invalidClass")
}