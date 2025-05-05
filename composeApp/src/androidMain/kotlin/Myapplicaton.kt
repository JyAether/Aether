package org.example.project
import android.app.Application
import com.aether.core.runtime.reflectable.ComposeComponentFactory.initializeComposeMirrors

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // 初始化 Compose 镜像反射系统
        initializeComposeMirrors()

        // 其他初始化代码...
    }
}