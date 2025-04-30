package com.aether.core.runtime.mirror

import android.util.Log
import androidx.compose.ui.Modifier
import java.lang.reflect.Field
import java.lang.reflect.Method
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.javaMethod

object MirrorHelper {

    /**
     * 安全获取类的成员，特别处理Compose UI组件
     * @param kClass 要反射的Kotlin类
     * @return 成员集合，包含方法和属性
     */
    fun safeGetMembers(kClass: KClass<*>): Collection<KCallableLike> {
        return try {
            if (kClass.qualifiedName?.startsWith("androidx.compose") == true) {
                // 对于Compose类，使用Java反射获取更安全的成员列表
                val javaClass = kClass.java
                val result = mutableListOf<KCallableLike>()

                // 添加方法
                javaClass.declaredMethods.forEach { method ->
                    result.add(JavaMethodAdapter(method))
                }

                // 添加字段
                javaClass.declaredFields.forEach { field ->
                    result.add(JavaFieldAdapter(field))
                }

                result
            } else {
                // 非Compose类使用正常的Kotlin反射
                kClass.members.map { KCallableAdapter(it) }
            }
        } catch (e: Exception) {
            Log.e("Reflection", "Error accessing members of ${kClass.qualifiedName}", e)
            // 返回空列表作为后备
            emptyList()
        }
    }

    /**
     * KCallable接口的统一表示
     */
    interface KCallableLike {
        val name: String
        val isStatic: Boolean
        fun call(vararg args: Any?): Any?
        fun hasAnnotation(annotationClass: KClass<out Annotation>): Boolean
    }

    /**
     * 包装KCallable的适配器
     */
    class KCallableAdapter(private val callable: KCallable<*>) : KCallableLike {
        override val name: String = callable.name

        override val isStatic: Boolean
            get() = callable.findAnnotation<JvmStatic>() != null ||
                    (callable is KFunction<*> &&
                            callable.javaMethod?.let {
                                java.lang.reflect.Modifier.isStatic(it.modifiers) // 明确使用完整类名
                            } == true)


        override fun call(vararg args: Any?): Any? {
            return callable.call(*args)
        }

        override fun hasAnnotation(annotationClass: KClass<out Annotation>): Boolean {
            return callable.annotations.any { it.annotationClass == annotationClass }
        }
    }

    /**
     * Java Method的适配器
     */
    class JavaMethodAdapter(private val method: Method) : KCallableLike {
        override val name: String = method.name

        override val isStatic: Boolean
            get() = java.lang.reflect.Modifier.isStatic(method.modifiers) // 明确使用完整类名

        override fun call(vararg args: Any?): Any? {
            val accessible = method.isAccessible
            try {
                method.isAccessible = true
                return method.invoke(null, *args)
            } finally {
                method.isAccessible = accessible
            }
        }

        override fun hasAnnotation(annotationClass: KClass<out Annotation>): Boolean {
            return method.isAnnotationPresent(annotationClass.java)
        }
    }

    /**
     * Java Field的适配器
     */
    class JavaFieldAdapter(private val field1: Field) : KCallableLike {
        override val name: String = field1.name


        override val isStatic: Boolean
            get() = java.lang.reflect.Modifier.isStatic(field1.modifiers) // 明确使用完整类名


        override fun call(vararg args: Any?): Any? {
            val accessible = field1.isAccessible
            try {
                field1.isAccessible = true
                return if (args.isEmpty()) {
                    field1.get(null)
                } else {
                    field1.set(null, args[0])
                    null
                }
            } finally {
                field1.isAccessible = accessible
            }
        }

        override fun hasAnnotation(annotationClass: KClass<out Annotation>): Boolean {
            return field1.isAnnotationPresent(annotationClass.java)
        }
    }
}