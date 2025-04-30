package com.aether.core.runtime.reflectable

import androidx.compose.runtime.Composable
import com.aether.core.runtime.Expression

// 定义一个描述Compose组件的类，不包含@Composable注解
class ComposeComponentDescriptor(
    val componentPath: String,
    val arguments: List<Expression>?
)

// 然后在@Composable函数中使用这个描述符
@Composable
fun RenderComponent(descriptor: ComposeComponentDescriptor) {
    ComposeComponentFactory.createComponent(
        descriptor.componentPath,
        descriptor.arguments
    )
}
