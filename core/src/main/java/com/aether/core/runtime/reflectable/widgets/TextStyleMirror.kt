package com.aether.core.runtime.reflectable.widgets

import android.util.Log
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.aether.core.runtime.reflectable.ComposeReflector
import com.aether.core.runtime.reflectable.MethodMirror
import com.aether.core.runtime.reflectable.MethodMirrorImpl

class TextStyleMirror(
    simpleName: String,
    qualifiedName: String
) : ComposeReflector(
    simpleName, qualifiedName
) {
    private var _staticMembers: Map<String, MethodMirror>? = null

    override val staticMembers: Map<String, MethodMirror>
        get() = _staticMembers ?: run {
            // Create a map to store MaterialTheme's static members
            val result = mutableMapOf<String, MethodMirror>()
            // Add Typography property
            result["h6"] = MethodMirrorImpl(
                simpleName = "h6",
                qualifiedName = "androidx.compose.ui.text.TextStyle",
                isStatic = true,
                invoke = { _, _ -> }
            )
            // Cache and return the immutable map
            result.toMap().also { _staticMembers = it }
        }

    override fun invoke(
        memberName: String,
        positionalArguments: List<Any?>,
        namedArguments: Map<String, Any?>?
    ): Any? {

        return TextStyle(

        )

    }

    @Composable
    override fun invoke(args: Map<String, Any?>?): Int? {
        if (args == null) {
            Log.e("TextStyleMirror", "error: args :$args")
            return 0 as Int
        }
        // 调用实际的Text组件
        TextStyle(

        )
        return 1 as Int
    }
}