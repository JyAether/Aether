package com.aether.core.runtime.reflectable

import androidx.compose.runtime.Composable

// 参数信息
data class ParamInfo(
    val name: String,
    val type: String,
    val isOptional: Boolean
)

// 组件反射接口
abstract class ComposeReflector(
    simpleName: String,
    qualifiedName: String
) : ClassMirrorBase(
    simpleName, qualifiedName
) {
    @Composable
    abstract fun invoke(args: Map<String, Any?>?): Any?
}