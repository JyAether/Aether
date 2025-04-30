package com.aether.core.runtime

import com.aether.core.runtime.AppContext

object AppContextManager {
   private val contextHolder = ThreadLocal.withInitial { AppContext(null, "ROOT", emptyMap(), emptyMap()) }

    // 获取当前线程的 AppContext
    val context: AppContext
        get() = contextHolder.get()

    // 设置当前线程的 AppContext
    fun set(context: AppContext) {
        contextHolder.set(context)
    }

    fun get() :AppContext{
       return context
    }
    // 移除当前线程的 AppContext
    fun removeContext() {
        contextHolder.remove()
    }
}