package com.aether.composeapp.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aether.annotations.ReflectableFunction
import com.aether.core.runtime.reflectable.ComposeComponentFactory

/**
 * 一个可反射的文本组件
 */
@ReflectableFunction(
    name = "CustomText",
    description = "A custom text component with padding"
)
@Composable
fun CustomText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier.padding(16.dp)
    )
}

/**
 * 一个可反射的按钮组件
 */
@ReflectableFunction(
    name = "CustomButton",
    description = "A custom button component with text"
)
@Composable
fun CustomButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
    ) {
        Text(text)
    }
}

/**
 * 一个可反射的容器组件
 */
@ReflectableFunction(
    name = "CustomContainer",
    description = "A custom container component"
)
@Composable
fun CustomContainer(
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        content()
    }
}

/**
 * 一个可反射的复合组件
 */
//@ReflectableFunction(
//    name = "ComplexComponent",
//    description = "A complex component combining multiple elements"
//)
//@Composable
//fun ComplexComponent(
//    title: String,
//    buttonText: String,
//    onButtonClick: () -> Unit
//) {
//    CustomContainer {
//        CustomText(text = title)
//        CustomButton(
//            text = buttonText,
//            onClick = onButtonClick
//        )
//    }
//}

/**
 * 示例用法
 */
@Composable
fun ExampleUsage() {
    // 注册组件
}