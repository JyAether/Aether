package com.aether.core.runtime

typealias LazilyInitializer = () -> Any?

class Variable(
    private var _value: Any?,
    val name: String,
    val isFinal: Boolean = false,
    val isStatic: Boolean = false,
    val isConst: Boolean = false,
    val isLate: Boolean = false
) {
    private var isInitialized = if (isLate && _value == null) false else true

    var value: kotlin.Any?
        get() {
            if (isLate && !isInitialized) {
                _value = (_value as LazilyInitializer).invoke() as Any?
                isInitialized = true
            }
            return _value!!
        }
    set(data) {
        value = data
    }

//    constructor(name: String, initializer: LazilyInitializer, isLate: Boolean = true) : this(null, name, isLate = isLate) {
//        this._value = initializer
//    }

    operator fun plus(other: Any?): Any? {
        return when (other) {
            is Variable ->{
                val thisValue = value as? Comparable<*>
                val otherValue = other.value
                if (thisValue != null && otherValue != null) {
                    thisValue.compareTo(otherValue as Nothing)
                } else {
                    0
                }
            }
            is Number -> {
                val thisValue = value as? Number
                if (thisValue != null) {
                    thisValue.toDouble() + other.toDouble()
                } else {
                    null // or throw an exception if you prefer
                }
            }
            else -> null
        }
    }

    // Implement other operators similarly...

    override fun toString(): String {
        return "[Variable $value]"
    }

    override fun equals(other: kotlin.Any?): Boolean {
        if (this === other) return true
        if (other !is Variable) return false
        return value == other.value
    }

    override fun hashCode(): Int {
        return value?.hashCode() ?: 0
    }
}

class StackScope(val name: String? = null) {
    private val frame = mutableMapOf<String, Any?>()

    operator fun contains(key: String): Boolean = frame.containsKey(key)

    operator fun get(key: String): Any? = frame[key]

    operator fun set(key: String, value: Any?) {
        frame[key] = value
    }

    override fun toString(): String {
        return "[$name]: $frame"
    }
}

class ProgramStack(val name: String, oldStack: ProgramStack? = null) {
    val frames: MutableList<StackScope> = oldStack?.frames?.toMutableList() ?: mutableListOf()

    init {
        if (oldStack == null) {
            push(name = "root scope:$name")
        }
    }

    fun isRoot(): Boolean {
        return frames.size == 1 && frames.single().name == "root scope:$name"
    }

    fun length(): Int = frames.size

    fun push(scope: StackScope? = null, name: String? = null) {
        frames.add(scope ?: StackScope(name))
    }

    fun pop(): StackScope? = frames.removeLastOrNull()

    fun putVariable(name: String, value: Any?) {
        frames.last()[name] = Variable(value, name)
    }
    fun  getVariable(name: String): Variable? {
        return get<Variable>(name)
    }

    fun putLazilyVariable(name: String, initializer: LazilyInitializer) {
        frames.last()[name] = Variable(_value = initializer,name = name)//todo initializer
    }

    fun put(name: String, value: Any?) {
        frames.last()[name] = value
    }

    inline fun <reified T> get(name: String): T? {
        for (i in frames.indices.reversed()) {
            val curFrame = frames[i]
            if (curFrame.contains(name) && curFrame[name] is T) {
                @Suppress("UNCHECKED_CAST")
                return curFrame[name] as T
            }
        }
        return null
    }

    override fun toString(): String {
        return "Program Stack ($name)\n${frames.joinToString("\n")}"
    }
}