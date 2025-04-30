package com.aether.core.runtime.proxy

import com.aether.core.runtime.ImportDirective
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
    fun getProxyClassForName(directive: ImportDirective?): KClass<*>? {
        // 假设 p.data 是一个存储类型映射的全局变量
        // 尝试通过反射加载类
        if (directive == null){
            return null
        }
        return try {
            Class.forName(directive.uri).kotlin
        } catch (e: ClassNotFoundException) {
            println("Class not found for type: ${directive.name}")
            null
        }
    }
}