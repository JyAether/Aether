package com.aether.annotations

/**
 * 用于标记可反射的Compose组件类
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Reflectable(
    val name: String = "",
    val description: String = ""
)

/**
 * 用于标记可反射的Compose函数
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class ReflectableFunction(
    val name: String = "",
    val description: String = ""
)

// 标记要生成反射元数据的注解
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class GenerateMirror