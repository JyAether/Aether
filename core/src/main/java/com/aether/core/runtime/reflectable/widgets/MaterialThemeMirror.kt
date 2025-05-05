package com.aether.core.runtime.reflectable.widgets

import android.util.Log
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import com.aether.core.runtime.reflectable.ComposeReflector
import com.aether.core.runtime.reflectable.MethodMirror
import com.aether.core.runtime.reflectable.MethodMirrorImpl

class MaterialThemeMirror(
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
            result["typography"] = MethodMirrorImpl(
                simpleName = "typography",
                qualifiedName = "androidx.compose.material.Typography",
                isStatic = true,
                invoke = { _, _ -> MaterialTheme.typography }
            )
            // Cache and return the immutable map
            result.toMap().also { _staticMembers = it }
        }

    @Composable
    override fun  invoke(args: Map<String, Any?>?): Int? {
        if (args == null) {
            Log.e("MaterialThemeMirror", "error: args :$args")
            return 0 as Int
        }
        val typography = args["typography"] as? Typography
        val content = args["content"] as? @Composable () -> Unit
        // 调用实际的Text组件
        MaterialTheme(
            content = content ?: {}, typography = typography ?: MaterialTheme.typography
        )
        return 1 as Int
    }
}