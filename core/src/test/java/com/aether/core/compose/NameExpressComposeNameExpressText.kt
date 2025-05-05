package com.aether.core.compose

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


class NameExpressComposeNameExpressText {
    @Composable
    fun Main() {
        Text(
            text = "text=这个一个最小试验单位的动态化卡片，它的产物来自云端打包编译，可以根据产品诉求随意替换",
            minLines = 1,
        )
    }
}