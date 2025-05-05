package com.aether.core.runtime.reflectable

abstract class DeclarationMirror : Mirror {

    /**
     * 返回此声明的简单名称。
     *
     * 简单名称通常是该实体的标识符名，例如：
     * - 方法名 "myMethod"
     * - 类名 "MyClass"
     * - 库名 "mylibrary"
     */
    abstract val simpleName: String

    /**
     * 返回此声明的完全限定名称。
     *
     * 例如：库 "lib" 中的类 "MyClass" 中的方法 "myMethod" 的完全限定名为：
     * "lib.MyClass.myMethod"
     */
    abstract val qualifiedName: String


    /**
     * 判断此声明是否为库私有（以 `_` 开头）。
     *
     * 对于库本身，始终返回 false。
     */
//    abstract val isPrivate: Boolean

    /**
     * 判断此声明是否是顶级声明（拥有者是库）。
     */
//    abstract val isTopLevel: Boolean
}