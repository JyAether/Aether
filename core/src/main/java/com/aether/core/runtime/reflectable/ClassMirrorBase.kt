package com.aether.core.runtime.reflectable

open class ClassMirrorBase(
    override val simpleName: String,
    override val qualifiedName: String,
) : ClassMirror(), ObjectMirror {
    override fun invoke(
        memberName: String,
        positionalArguments: List<Any?>,
        namedArguments: Map<String, Any?>?
    ): Any? {
        TODO("Not yet implemented")
    }

    override fun invokeGetter(getterName: String): Any? {
        TODO("Not yet implemented")
    }

    override fun invokeSetter(setterName: String, value: Any?): Any? {
        TODO("Not yet implemented")
    }

    override val declarations: Map<String, DeclarationMirror>
        get() = TODO("Not yet implemented")
    override val instanceMembers: Map<String, MethodMirror>
        get() = TODO("Not yet implemented")
    override val staticMembers: Map<String, MethodMirror>
        get() = TODO("Not yet implemented")

    override fun newInstance(
        constructorName: String,
        positionalArguments: List<Any?>,
        namedArguments: Map<String, Any?>?
    ): Any? {
        return null
    }
}