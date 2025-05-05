package com.aether.core.runtime.reflectable.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.aether.core.runtime.reflectable.ComposeComponentDescriptor
import com.aether.core.runtime.reflectable.ComposeComponentFactory.createComponent
import com.aether.core.runtime.reflectable.ComposeReflector
import com.aether.core.runtime.reflectable.DeclarationMirror
import com.aether.core.runtime.reflectable.MethodMirror
import com.aether.core.runtime.reflectable.ParamInfo

class ColumnMirror(
    simpleName: String,
    qualifiedName: String
) : ComposeReflector(simpleName, qualifiedName) {
    override val simpleName: String = "Column"

    @Composable
    override fun  invoke(args: Map<String, Any?>?): Int  {
        if (args == null) {
            Column {
                Text("No Column UI, please check args")
            }
            return 0 as Int
        }

        // 从参数映射中提取可选参数
        val modifier = args["modifier"] as? Modifier ?: Modifier
        val verticalArrangement = args["verticalArrangement"] as? Arrangement.Vertical ?: Arrangement.Top
        val horizontalAlignment = args["horizontalAlignment"] as? Alignment.Horizontal ?: Alignment.Start

        // 获取内容函数
        val children = args["children"] as? List<ComposeComponentDescriptor>

        // 调用实际的Column组件
        Column(
            modifier = modifier,
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment
        ) {
            // 如果提供了子组件列表，则渲染它们
            children?.forEach { childDescriptor ->
                createComponent(
                    childDescriptor.componentPath,
                    childDescriptor.positionalArguments,
                    childDescriptor.namedArguments,
                    childDescriptor.children
                )
            }
        }
        return 1 as Int
    }

//    override val params: List<ParamInfo> = listOf(
//        ParamInfo(
//            name = "modifier", type = "androidx.compose.ui.Modifier", isOptional = true
//        ), ParamInfo(
//            name = "verticalArrangement",
//            type = "androidx.compose.foundation.layout.Arrangement.Vertical",
//            isOptional = true
//        ), ParamInfo(
//            name = "horizontalAlignment",
//            type = "androidx.compose.ui.Alignment.Horizontal",
//            isOptional = true
//        ), ParamInfo(
//            name = "content", type = "androidx.compose.runtime.Composable", isOptional = false
//        )
//    )

//    @Composable
//    override fun invoke(args: Map<String, Any?>?) {
//        if (args == null) {
//            Column {
//                Text("No Column UI, please check args")
//            }
//            return
//        }
//
//        // 从参数映射中提取可选参数
//        val modifier = args["modifier"] as? Modifier ?: Modifier
//        val verticalArrangement = args["verticalArrangement"] as? Arrangement.Vertical ?: Arrangement.Top
//        val horizontalAlignment = args["horizontalAlignment"] as? Alignment.Horizontal ?: Alignment.Start
//
//        // 获取内容函数
//        val children = args["children"] as? List<ComposeComponentDescriptor>
//
//        // 调用实际的Column组件
//        Column(
//            modifier = modifier,
//            verticalArrangement = verticalArrangement,
//            horizontalAlignment = horizontalAlignment
//        ) {
//            // 如果提供了子组件列表，则渲染它们
//            children?.forEach { childDescriptor ->
//                createComponent(
//                    childDescriptor.componentPath,
//                    childDescriptor.positionalArguments,
//                    childDescriptor.namedArguments,
//                    childDescriptor.children
//                )
//            }
//        }
//    }
}