package com.aether.core.runtime.reflectable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aether.core.runtime.Argument
import com.aether.core.runtime.Expression
import com.aether.core.runtime.executeExpression
import com.aether.core.runtime.reflectable.widgets.ColumnMirror
import com.aether.core.runtime.reflectable.widgets.MaterialThemeMirror
import com.aether.core.runtime.reflectable.widgets.TextMirror
import com.aether.core.runtime.reflectable.widgets.TextStyleMirror
import com.aether.core.runtime.reflectable.widgets.TypographyMirror

// 在你的代码中添加一个辅助类来映射Compose组件
/**
 * Compose组件工厂
 * 提供创建Compose组件的方法
 */
public object ComposeComponentFactory {
    private val componentFactories = mutableMapOf<String, @Composable (Map<String, Any>) -> Unit>()

    fun registerComponent(
        name: String, factory: @Composable (Map<String, Any>) -> Unit
    ) {
        componentFactories[name] = factory
    }

    @Composable
    fun createComponent2(
        name: String, arguments: Map<String, Any> = emptyMap()
    ) {
        val factory = componentFactories[name]
        if (factory != null) {
            factory(arguments)
        } else {
            // 处理未找到组件的情况
        }
    }

    // 创建动态Composable组件
    @Composable
    fun createComponent(
        componentPath: String?,
        positionalArguments: List<Any?>?,
        namedArguments: Map<String, Any?>?,
        children: List<ComposeComponentDescriptor>?
    ) {
        // 创建属性映射以存储解析后的参数
        val props = mutableMapOf<String, Any?>()
        when (componentPath) {
            "androidx.compose.material.Text" -> {
                // 设置默认值
                props["modifier"] = Modifier.padding(16.dp)
                ComposeMirror.getReflector(componentPath)?.invoke(namedArguments)
            }

            "androidx.compose.material.MaterialTheme" -> {
                val mergedArgs = namedArguments?.toMutableMap() ?: mutableMapOf()
                // 使用反射器渲染Column
                ComposeMirror.getReflector(componentPath)?.invoke(mergedArgs) ?: fallbackColumn(
                    mergedArgs,
                    children
                )
            }

            "androidx.compose.material.Typography" -> {
                val mergedArgs = namedArguments?.toMutableMap() ?: mutableMapOf()
                // 使用反射器渲染Column
                ComposeMirror.getReflector(componentPath)?.invoke(mergedArgs) ?: fallbackColumn(
                    mergedArgs,
                    children
                )
            }


            "androidx.compose.foundation.layout.Column" -> {
                // 为Column组件添加子组件信息
                val mergedArgs = namedArguments?.toMutableMap() ?: mutableMapOf()
                // 将子组件信息添加到参数中
                mergedArgs["children"] = children
                // 使用反射器渲染Column
                ComposeMirror.getReflector(componentPath)?.invoke(mergedArgs) ?: fallbackColumn(
                    mergedArgs,
                    children
                )
            }
            // 可以继续添加更多组件...
            else -> {
                Text("未实现的组件: $componentPath")
            }
        }
    }

    // 备用Column实现，当反射器不可用时使用
    @Composable
    private fun fallbackColumn(
        args: Map<String, Any?>, children: List<ComposeComponentDescriptor>?
    ) {
//        Column(
//            modifier = (args["modifier"] as? Modifier) ?: Modifier
//                .fillMaxSize()
//                .padding(16.dp),
//            verticalArrangement = (args["verticalArrangement"] as? Arrangement.Vertical)
//                ?: Arrangement.spacedBy(8.dp),
//            horizontalAlignment = (args["horizontalAlignment"] as? Alignment.Horizontal)
//                ?: Alignment.CenterHorizontally
//        ) {
//            // 渲染子组件
//            children?.forEach { childDescriptor ->
//                createComponent(
//                    childDescriptor.componentPath,
//                    childDescriptor.positionalArguments,
//                    childDescriptor.namedArguments,
//                    childDescriptor.children
//                )
//            }
//        }
    }

    // 注册生成的反射器的初始化函数
    fun initializeComposeMirrors() {
        // 这里会被生成的代码调用，注册所有反射器
        // 例如自动生成的TextMirror
        ComposeMirror.register(
            "androidx.compose.material.Text", TextMirror(
                simpleName = "Text",
                "androidx.compose.material.Text"
            )
        )
        ComposeMirror.register(
            "androidx.compose.foundation.layout.Column", ColumnMirror(
                simpleName = "Column",
                qualifiedName = "androidx.compose.foundation.layout.Column"
            )
        )

        ComposeMirror.register(
            "androidx.compose.material.MaterialTheme", MaterialThemeMirror(
                simpleName = "MaterialTheme",
                qualifiedName = "androidx.compose.material.MaterialTheme"
            )
        )

        ComposeMirror.register(
            "androidx.compose.material.Typography", TypographyMirror(
                simpleName = "Typography",
                qualifiedName = "androidx.compose.material.Typography"
            )
        )

        ComposeMirror.register(
            "androidx.compose.ui.text.TextStyle", TextStyleMirror(
                simpleName = "TextStyle",
                qualifiedName = "androidx.compose.ui.text.TextStyle"
            )
        )
    }

    private fun initReflect() {

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