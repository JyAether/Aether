package com.aether.core.runtime.reflectable

import androidx.compose.runtime.Composable

class MethodMirrorImpl(override val isStatic: Boolean,
                       val invoke: @Composable (args: List<Any?>?, namedArgs: Map<String, Any?>?) -> Any?,
                       override val simpleName: String,
                       override val qualifiedName: String
) :MethodMirror() {

    @Composable
    override fun invoke(
        args: List<Any?>?,
        namedArgs: Map<String, Any?>?
    ): Any? {
        return invoke.invoke(args, namedArgs)
    }

}