package com.aether.core.runtime.entity

// 简单的成员表示，用于替代可能导致问题的KCallable
class SimpleMember(val name: String, val modifiers: Int) {
    val isStatic: Boolean
        get() = modifiers and java.lang.reflect.Modifier.STATIC != 0
}