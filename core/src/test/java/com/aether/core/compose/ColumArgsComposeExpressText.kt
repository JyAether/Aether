package com.aether.core.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

class ColumArgsComposeExpressText {
    @Composable
    fun Main() {
        Column {
            Column(
            ) {
                Text(
                    text = "主标题 1",
                    style = MaterialTheme.typography.h4,
                )
                Text(
                    text = "副标题 1",
                    style = MaterialTheme.typography.h5,
                )
            }
            Column(
            ) {
                Text(
                    text = "主标题 2",
                    style = MaterialTheme.typography.h4,
                )
                Text(
                    text = "副标题 3",
                    style = MaterialTheme.typography.h5,
                )
            }
            Column(
            ) {
                Text(
                    text = "主标题 2",
                    style = MaterialTheme.typography.h4,
                )
                Text(
                    text = "副标题 3",
                    style = MaterialTheme.typography.h5,
                )
            }
        }
    }
}