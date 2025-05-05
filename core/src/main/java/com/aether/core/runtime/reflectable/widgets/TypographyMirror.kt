package com.aether.core.runtime.reflectable.widgets

import android.text.TextUtils
import android.util.Log
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.aether.core.runtime.reflectable.ComposeComponentDescriptor
import com.aether.core.runtime.reflectable.ComposeComponentDescriptor2
import com.aether.core.runtime.reflectable.ComposeMirror
import com.aether.core.runtime.reflectable.ComposeReflector
import com.aether.core.runtime.reflectable.MethodMirror
import com.aether.core.runtime.reflectable.MethodMirrorImpl

class TypographyMirror(
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

    @Composable
    override fun invoke(args: Map<String, Any?>?): Typography? {
        if (args == null) {
            Log.e("MaterialThemeMirror", "error: args :$args")
            return null
        }
        val typography = MaterialTheme.typography
        val attribute = args[simpleName] as String
        if (TextUtils.equals(attribute, "h6")) {
            typography.h6
        }else if (TextUtils.equals(attribute, "h5")) {
            typography.h5
        }
        return typography
    }

    override fun invoke(
        memberName: String,
        positionalArguments: List<Any?>,
        namedArguments: Map<String, Any?>?
    ): Any? {
        if (namedArguments == null) {
            Log.e("MaterialThemeMirror", "error: args :$namedArguments")
            return null
        }
        val reflector = ComposeMirror.getReflector(memberName)

        val typography = namedArguments["typography"] as? Typography
        val content = namedArguments["content"] as? @Composable () -> Unit
        // 调用实际的Text组件
//        return MaterialTheme.typography.h6
        return null
    }
}