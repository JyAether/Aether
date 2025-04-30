package com.aether.core.runtime.deliver

import com.aether.core.runtime.AppContextManager.context
import com.aether.core.runtime.AstMethod
import com.aether.core.runtime.Expression
import com.aether.core.runtime.MethodDeclaration
import com.aether.core.runtime.executeExpression
import com.aether.core.runtime.reflectable.ComposeComponentDescriptor

// 实现 doRunBuild 方法
fun doRunBuild(func: MethodDeclaration) {
    val arguments = context.get<Arguments>(Arguments::class.java)
    val positionalArguments = mutableListOf<Any?>()
    val namedArguments = mutableMapOf<String, Any?>()

    if (arguments != null) {
        arguments.positionalArguments?.let { positionalArguments.addAll(it) }
        arguments.namedArguments?.let { namedArguments.putAll(it) }
    }

    val method = AstMethod.fromExpression(func)
    val holder = context.get<ComposableHolder>(ComposableHolder::class.java)!!

    // 处理返回的结果
    val result = AstMethod.apply2(method, positionalArguments, namedArguments)

    // 如果结果是 ComposeComponentDescriptor，直接设置
    if (result is ComposeComponentDescriptor) {
        holder.componentDescriptor = result
    } else {
        // 对于其他类型的结果，创建一个默认的 Text 组件描述符
        holder.componentDescriptor = ComposeComponentDescriptor(
            "androidx.compose.material.Text",
            null
        )
    }
}

// 实现 invokeRunMain 方法
fun invokeRunMain(expression: Any?) {
    val holder = context.get<ComposableHolder>(ComposableHolder::class.java)!!
//    val result = executeExpression(expression)

    // 处理返回的结果
    if (expression is ComposeComponentDescriptor) {
        holder.componentDescriptor = expression
    } else {
        // 默认情况
        holder.componentDescriptor = ComposeComponentDescriptor(
            "androidx.compose.material.Text",
            null
        )
    }
}


// 实现 invokeRunApp 方法
fun invokeRunApp(expression: Expression) {
    val holder = context.get<ComposableHolder>(ComposableHolder::class.java)!!
    val result = executeExpression(expression)

    // 处理返回的结果
    if (result is ComposeComponentDescriptor) {
        holder.componentDescriptor = result
    } else {
        // 默认情况
        holder.componentDescriptor = ComposeComponentDescriptor(
            "androidx.compose.material.Text",
            null
        )
    }
}
