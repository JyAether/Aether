package com.aether.core.runtime.reflectable.widgets

import android.graphics.fonts.FontFamily
import android.os.Build
import android.text.TextUtils
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Text
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import com.aether.core.runtime.reflectable.ClassMirror
import com.aether.core.runtime.reflectable.ClassMirrorBase
import com.aether.core.runtime.reflectable.ComposeComponentDescriptor
import com.aether.core.runtime.reflectable.ComposeReflector
import com.aether.core.runtime.reflectable.DeclarationMirror
import com.aether.core.runtime.reflectable.MethodMirror
import com.aether.core.runtime.reflectable.MethodMirrorImpl
import com.aether.core.runtime.reflectable.ParamInfo
import com.aether.core.runtime.reflectable.RenderComponent
import java.time.format.TextStyle
import kotlin.collections.set

/**
 * 自动生成的Text组件反射包装类
 */
class TextMirror(
    simpleName: String, qualifiedName: String
) : ComposeReflector(
    simpleName, qualifiedName
) {

    override fun invoke(
        memberName: String, positionalArguments: List<Any?>, namedArguments: Map<String, Any?>?
    ): Any? {
        return super.invoke(memberName, positionalArguments, namedArguments)
    }

    @Composable
    override fun invoke(args: Map<String, Any?>?): Int {
        if (args == null) {
            Text("NO Text UI please chack args")
            return 0 as Int
        }
        // 从参数映射中提取必要参数
        val text = args["text"] as? String ?: ""

        // 从参数映射中提取可选参数
        val modifier = args["modifier"] as? Modifier
        val color = args["color"] as? Color
        val fontSize = args["fontSize"] as? TextUnit
        val fontFamily = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            args["fontFamily"] as? FontFamily
        } else {
            TODO("VERSION.SDK_INT < Q")
        }
        val textAlign = args["textAlign"] as? TextAlign
        val styleTemp = args["style"]
        val maxLines = args["maxLines"] as? Int
        val overflow = args["overflow"] as? androidx.compose.ui.text.style.TextOverflow
        val style = if (styleTemp is ComposeComponentDescriptor) {
            val attribute = styleTemp.namedArguments?.get("attribute") as String
            if (TextUtils.equals(attribute, "h6")) {
                typography.h6
            } else if (TextUtils.equals(attribute, "h5")) {
                typography.h5
            } else if (TextUtils.equals(attribute, "h4")) {
                typography.h4
            } else if (TextUtils.equals(attribute, "h3")) {
                typography.h3
            } else if (TextUtils.equals(attribute, "h2")) {
                typography.h2
            } else if (TextUtils.equals(attribute, "h1")) {
                typography.h1
            } else {
                Typography().h1
            }
        } else {
            Typography().h1
        }
        // 调用实际的Text组件
        Text(
            text = text,
//            modifier = modifier ?: Modifier,
            color = color ?: androidx.compose.ui.graphics.Color.Unspecified,
            fontSize = fontSize ?: TextUnit.Unspecified,
//            fontFamily = fontFamily,
            textAlign = textAlign,
            style = style,
//            maxLines = maxLines ?: Int.MAX_VALUE,
            overflow = overflow ?: TextOverflow.Clip
        )
        return 1 as Int
    }
}