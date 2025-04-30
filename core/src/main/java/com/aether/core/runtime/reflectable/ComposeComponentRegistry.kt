package com.aether.core.runtime.reflectable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aether.core.runtime.Expression
import com.aether.core.runtime.executeExpression

// 在你的代码中添加一个辅助类来映射Compose组件
/**
 * Compose组件工厂
 * 提供创建Compose组件的方法
 */
object ComposeComponentFactory {
    // 创建动态Composable组件
    @Composable
    fun createComponent(
        componentPath: String,
        arguments: List<Expression>?
    ) {
        when (componentPath) {
            "androidx.compose.material.Text" -> {
                val expression = arguments?.get(0)
                var text = "Default Text"
                    expression?.let {
                        text = executeExpression(expression) as String
                }
//                val modifier = arguments["modifier"] as? androidx.compose.ui.Modifier
                androidx.compose.material.Text(
                    text = text,
//                    modifier = modifier ?: androidx.compose.ui.Modifier
                    modifier = Modifier
                        .padding(16.dp) // 设置外部边缘（padding）
                )
            }
//            "androidx.compose.material.Button" -> {
//                val onClick = arguments["onClick"] as? () -> Unit ?: {}
//                val content = arguments["content"] as? (@Composable () -> Unit)
//
//                androidx.compose.material.Button(
//                    onClick = onClick,
//                    modifier = arguments["modifier"] as? androidx.compose.ui.Modifier ?: androidx.compose.ui.Modifier
//                ) {
//                    if (content != null) {
//                        content()
//                    } else {
//                        androidx.compose.material.Text("Button")
//                    }
//                }
//            }
//            "androidx.compose.foundation.layout.Column" -> {
//                val modifier = arguments["modifier"] as? androidx.compose.ui.Modifier
//                val content = arguments["content"] as? (@Composable () -> Unit)
//
//                androidx.compose.foundation.layout.Column(
//                    modifier = modifier ?: androidx.compose.ui.Modifier
//                ) {
//                    if (content != null) {
//                        content()
//                    }
//                }
//            }
//            "androidx.compose.foundation.layout.Row" -> {
//                val modifier = arguments["modifier"] as? androidx.compose.ui.Modifier
//                val content = arguments["content"] as? (@Composable () -> Unit)
//
//                androidx.compose.foundation.layout.Row(
//                    modifier = modifier ?: androidx.compose.ui.Modifier
//                ) {
//                    if (content != null) {
//                        content()
//                    }
//                }
//            }
            // 可以继续添加更多组件...
            else -> {
                androidx.compose.material.Text("未实现的组件: $componentPath")
            }
        }
    }

    // 注册表，用于检查组件是否可用
    private val availableComponents = setOf(
        "androidx.compose.material.Text",
        "androidx.compose.material.Button",
        "androidx.compose.foundation.layout.Column",
        "androidx.compose.foundation.layout.Row",
        // 添加更多组件...
    )

    // 检查组件是否可用
    fun isComponentAvailable(path: String): Boolean {
        return availableComponents.contains(path)
    }
}