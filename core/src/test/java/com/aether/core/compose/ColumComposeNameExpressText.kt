package com.aether.core.compose
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable


class ColumComposeNameExpressText {
    @Composable
    fun Main() {
        Column {
            Text(
                text = "1这个一个最小试验单位的动态化卡片，它的产物来自云端打包编译，可以根据产品诉求随意替换",
                minLines = 1,
            )
            Text(
                text = "2这个一个最小试验单位的动态化卡片，它的产物来自云端打包编译，可以根据产品诉求随意替换",
                minLines = 1,
            )
            Text(
                text = "3这个一个最小试验单位的动态化卡片，它的产物来自云端打包编译，可以根据产品诉求随意替换",
                minLines = 1,
            )
            Text(
                text = "4这个一个最小试验单位的动态化卡片，它的产物来自云端打包编译，可以根据产品诉求随意替换",
                minLines = 1,
            )
        }

    }
}