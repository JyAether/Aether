package com.aether.core.compose

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


class DemoCompose {
//    @Composable
//    fun Main() {
//        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
//            Button(onClick = { }) {
//                Text("Click me!")
//            }
//            Column(
//                Modifier.fillMaxWidth(),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                Text("这是一个KMP加载的动态化的View")
//            }
//        }
//    }
//}
//        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
//            Button(onClick = {}) {
//                Text("Click me!")
//            }
//            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
//                Box(
//                    modifier = Modifier
//                        .height(100.dp)
//                        .fillMaxWidth()
//                        .padding(16.dp) // 设置外部边缘（padding）
//                        .clip(RoundedCornerShape(10.dp)) // 设置圆角半径为10dp
//                        .background(Color(0xFFD1EAFB)), // 设置背景颜色
//                    contentAlignment = Alignment.Center // 将 Text 垂直和水平居中
//                ) {
//                    Text("一个标题")
//                }
//            }
//        }
//        Text("这个一个最小试验单位的动态化卡片，它的产物来自云端打包编译，可以根据产品诉求随意替换")
//    }

    @Composable
    fun Main() {
        Text(
            "这个一个最小试验单位的动态化卡片，它的产物来自云端打包编译，可以根据产品诉求随意替换",
            color = Color.Red
        )

    }
}