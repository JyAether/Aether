package com.aether.core.runtime.reflectable

import androidx.compose.runtime.Composable
import com.aether.core.runtime.Expression
import com.aether.core.runtime.executeExpression

// 定义一个描述Compose组件的类，不包含@Composable注解
class ComposeComponentDescriptor(
    val componentPath: String,
    val positionalArguments: List<Any?>?,
    val namedArguments: Map<String, Any?>?,
    val children: List<ComposeComponentDescriptor>?
)

class ComposeComponentDescriptor2(
    val methodMirror: MethodMirror?,
    val componentPath: String,
    val positionalArguments: List<Any?>?,
    val namedArguments: Map<String, Any?>?,
    val children: List<ComposeComponentDescriptor>? = null
)

// 然后在@Composable函数中使用这个描述符
@Composable
fun RenderComponent(descriptor: ComposeComponentDescriptor) {
    ComposeComponentFactory.createComponent(
        descriptor.componentPath,
        descriptor.positionalArguments,
        descriptor.namedArguments,
        descriptor.children
    )
}


// 然后在@Composable函数中使用这个描述符
@Composable
fun RenderComponent(
    componentPath: String,
    arguments: List<Expression>?
) {
    var mutableList = mutableListOf<String>()
    val expression = arguments?.get(0)
    var text = "Default Text"
    expression?.let {
        text = executeExpression(expression) as String
        mutableList.add(text)
    }

//    ComposeComponentFactory.createComponent2(
//        componentPath,
//        mutableList
//    )
}
