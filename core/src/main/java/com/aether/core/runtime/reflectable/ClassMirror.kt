package com.aether.core.runtime.reflectable

import androidx.compose.runtime.Composable
import kotlin.reflect.KClass

abstract class ClassMirror : TypeMirror() {

//    /**
//     * 当前类所实现的所有接口的镜像列表。
//     */
//    abstract val superinterfaces: List<ClassMirror>


    /**
     * 返回当前类中显式声明的所有成员的不可变映射。
     *
     * 包括方法、getter、setter、字段、构造函数等。
     * 不包括继承来的成员。
     *
     * 映射以简单名称为键，[DeclarationMirror] 为值。
     *
     * 所需能力: [DeclarationsCapability]
     */
    abstract val declarations: Map<String, DeclarationMirror>

    /**
     * 返回实例可以访问的所有成员（包括继承的）。
     *
     * 包括方法、getter、setter。
     * 字段本身不包含，但其 getter/setter 会被包含。
     *
     * 映射以简单名称为键，[MethodMirror] 为值。
     *
     * 所需能力: [DeclarationsCapability]
     */
    abstract val instanceMembers: Map<String, MethodMirror>

    /**
     * 返回类的静态方法、getter 和 setter。
     *
     * 映射以简单名称为键，[MethodMirror] 为值。
     *
     * 所需能力: [DeclarationsCapability]
     */
    abstract val staticMembers: Map<String, MethodMirror>


    /**
     * 调用指定名称的构造函数并返回结果。
     *
     * @param constructorName 构造函数名（空字符串表示默认构造函数）
     * @param positionalArguments 位置参数列表
     * @param namedArguments 命名参数映射
     */
    abstract fun newInstance(
        constructorName: String,
        positionalArguments: List<Any?>,
        namedArguments: Map<String, Any?>? = null
    ): Any?
}

/**
 * 表示类型相关的镜像信息。
 */
abstract class TypeMirror : DeclarationMirror() {

}


/**
 * 代表方法或访问器的镜像。
 */
abstract class MethodMirror : DeclarationMirror() {
    abstract val isStatic: Boolean

    @Composable
    abstract fun invoke(args: List<Any?>?, namedArgs: Map<String, Any?>?): Any?
}

data class Symbol(val name: String)

typealias Type = KClass<*>