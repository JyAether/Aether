package com.aether.core.runtime

import androidx.compose.runtime.Composable
import com.aether.core.runtime.reflectable.ClassMirror
import com.aether.core.runtime.reflectable.ClassMirrorBase
import com.aether.core.runtime.reflectable.ComposeComponentDescriptor
import com.aether.core.runtime.reflectable.ComposeComponentDescriptor2
import com.aether.core.runtime.reflectable.ComposeReflector
import com.aether.core.runtime.reflectable.MethodMirror
import org.jetbrains.kotlin.builtins.StandardNames.FqNames.kProperty
import java.lang.reflect.Modifier
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaMethod


class _ClassMirror(
    private val classMirror: ClassMirrorBase,
    private val _name: String,
    override val superclass: AstClass?,
    private val _staticFields: Map<String, _VariableImpl>,
    private val _staticGetters: Map<String, AstMethod>,
    private val _staticSetters: Map<String, AstMethod>,
    val _instanceFields: Map<String, _VariableImpl>,
    val _instanceGetters: Map<String, _MethodImpl>,
    val _instanceSetters: Map<String, _MethodImpl>,
    private val _constructors: Map<String, _ConstructorImpl>,
//    private val _node: ClassDeclaration,
    override val programNode: AstRuntime.ProgramNode
) : AstObject, AstClass() {
    companion object {

        fun fromMirror(mirror: ClassMirrorBase, programNode: AstRuntime.ProgramNode): _ClassMirror {
            var staticFields: Map<String, _VariableImpl> = HashMap<String, _VariableImpl>()
            val staticGetters: HashMap<String, AstMethod> = HashMap<String, AstMethod>()
            val staticSetters: HashMap<String, AstMethod> = HashMap<String, AstMethod>()
            val instanceFields: Map<String, _VariableImpl> = mapOf<String, _VariableImpl>()
            val instanceGetters: Map<String, _MethodImpl> = mapOf<String, _MethodImpl>()
            val instanceSetters: Map<String, _MethodImpl> = mapOf<String, _MethodImpl>()
            val constructors: Map<String, _ConstructorImpl> = mapOf<String, _ConstructorImpl>()

            return _ClassMirror(
                mirror,
                _name = mirror.simpleName ?: "",
                superclass = null,
                _staticFields = staticFields,
                _staticGetters = staticGetters,
                _staticSetters = staticSetters,
                _instanceFields = instanceFields,
                _instanceGetters = instanceGetters,
                _instanceSetters = instanceSetters,
                _constructors = constructors,
                programNode = programNode,
            );
        }
    }

    private var _declarations: Map<String, AstDeclaration>? = null
    private var _staticMembers: Map<String, AstMethod>? = null
    private var _instanceMembers: Map<String, AstMethod>? = null
    override val isAbstract: Boolean
        get() = TODO("Not yet implemented")

    override val declarations: Map<String, AstDeclaration>
        get() = _declarations ?: run {
            val result = mutableMapOf<String, AstDeclaration>()
//            kClass.members.forEach { member ->
//                when (member) {
//                    is kotlin.reflect.KFunction -> result[member.name] =
//                        AstMethod.fromMirror(member)
//                    is kotlin.reflect.KProperty<*> -> result[member.name] =
//                        AstVariable.fromKCallable(member)
//                }
//            }
            result.toMap().also { _declarations = it }
        }
    override val instanceFields: Map<String, AstVariable>
        get() = TODO("Not yet implemented")
    override val instanceGetters: Map<String, AstMethod>
        get() = TODO("Not yet implemented")
    override val instanceSetters: Map<String, AstMethod>
        get() = TODO("Not yet implemented")
    override val staticFields: Map<String, AstVariable>
        get() = TODO("Not yet implemented")
    override val staticGetters: Map<String, AstMethod>
        get() = _staticMembers ?: run {
            val result = mutableMapOf<String, AstMethod>()
            classMirror.staticMembers.forEach { simpleName, member ->
                result[simpleName] = AstMethod.fromMirror(member)
            }
            result.toMap().also { _staticMembers = it }
        }
    override val staticSetters: Map<String, AstMethod>
        get() = TODO("Not yet implemented")

    override fun newInstance(
        constructorName: String,
        positionalArguments: List<Any?>,
        namedArguments: Map<String, Any?>?
    ): Any? {
        return classMirror.invoke(constructorName, positionalArguments, namedArguments)
    }

    override fun isSubclassOf(other: AstClass): Boolean {
        TODO("Not yet implemented")
    }

    override fun hasConstructor(constructorName: String): Boolean {
        return false
    }

    override val simpleName: String
        get() = _name
    override val qualifiedName: String
        get() = _name
    override val owner: AstDeclaration?
        get() = TODO("Not yet implemented")
    override val isPrivate: Boolean
        get() = throw NotImplementedError()

    override fun invoke(
        memberName: String,
        positionalArguments: List<Any?>,
        namedArguments: Map<String, Any?>?
    ): Any? {
        val method = staticGetters[memberName] ?: throw NoSuchMethodError("Member $memberName not found")
        return invokeStaticMethod(method, positionalArguments, namedArguments)
    }

    override fun invokeGetter(getterName: String): Any? {
        TODO("Not yet implemented")
    }

    override fun hasGetter(getterName: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun invokeSetter(setterName: String, value: Any?): Any? {
        TODO("Not yet implemented")
    }

    override fun hasSetter(setterName: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun hasRegularMethod(methodName: String): Boolean {
        TODO("Not yet implemented")
    }

    private fun invokeStaticMethod(
        name: AstMethod,
        positionalArguments: List<Any?>,
        namedArguments: Map<String, Any?>?
    ): Any? {
        val methodMirror = (name as _MethodMirror2)._methodMirror
        return ComposeComponentDescriptor2(
            methodMirror = methodMirror,
             "",
            positionalArguments = positionalArguments,
            namedArguments = namedArguments,
            children = null
        )
//        return methodMirror?.invoke(null,namedArguments)
    }

    private fun buildParameters(
        kFunction: KFunction<*>,
        positionalArgs: List<Any?>,
        namedArgs: Map<String, Any?>?
    ): Map<KParameter, Any?> {

        val parameters = kFunction.parameters
        val result = mutableMapOf<KParameter, Any?>()

        // 处理位置参数
        var positionalIndex = 0
        for (param in parameters) {
            if (positionalIndex < positionalArgs.size && !param.isVararg) {
                result[param] = positionalArgs[positionalIndex]
                positionalIndex++
            } else if (namedArgs?.containsKey(param.name) == true) {
                // 处理命名参数
                result[param] = namedArgs[param.name]
            } else if (param.isVararg) {
                // 如果是可变参数，则需要特殊处理
                val remainingPosArgs = positionalArgs.drop(positionalIndex)
                result[param] = if (remainingPosArgs.isNotEmpty())
                    remainingPosArgs.toTypedArray()
                else
                    null
            }
        }
        return result
    }

    private fun convertType(type: KType, value: Any?): Any? {
        return when (type.classifier) {
            Double::class -> value?.toString()?.toDoubleOrNull()
            else -> value
        }
    }
}

//class _ClassMirror(private val _classMirror: ClassMirror) : AstObject, AstClass() {
//    private var _declarations: Map<String, AstDeclaration>? = null
//    private var _staticMembers: Map<String, AstMethod>? = null
//    private var _instanceMembers: Map<String, AstMethod>? = null
//
//    override val declarations: Map<String, AstDeclaration>
//        get() = _declarations ?: run {
//            val result = mutableMapOf<String, AstDeclaration>()
//            _classMirror.declarations.forEach { (name, mirror) ->
//                when (mirror) {
//                    is MethodMirror -> result[name] = AstMethod.fromMirror(mirror)
//                    is VariableMirrorBase -> result[name] = AstVariable.fromMirror(mirror)
//                }
//            }
//            result.toMap().also { _declarations = it }
//        }
//
//    override val staticMembers: Map<String, AstMethod>
//        get() = _staticMembers ?: run {
//            val result = mutableMapOf<String, AstMethod>()
//            _classMirror.staticMembers.forEach { (name, mirror) ->
//                result[name] = AstMethod.fromMirror(mirror)
//            }
//            result.toMap().also { _staticMembers = it }
//        }
//
//    override fun hasConstructor(constructorName: String): Boolean {
//        val methodName = if (constructorName.isEmpty()) simpleName else "$simpleName.$constructorName"
//        return (_classMirror.declarations[methodName] as? MethodMirror)?.isConstructor ?: false
//    }
//
//    override fun invoke(memberName: String, positionalArguments: List<Any?>, namedArguments: MutableMap<String, Any?>?): Any? {
//        val method = staticMembers[memberName] ?: throw NoSuchMethodError("Member $memberName not found")
////        processArguments(method.parameters, positionalArguments, namedArguments)
//        processArguments(mutableListOf<AstParameter>(), positionalArguments, namedArguments)
//        return _classMirror.invoke(memberName, positionalArguments, namedArguments)
//    }
//
//    // 其他方法类似转换...
//}


class _MethodMirror2(methodMirror: MethodMirror) : AstMethod {

    var _methodMirror: MethodMirror? = null

    init {
        this._methodMirror = methodMirror
    }

    companion object {
        fun fromMirror(mirror: KFunction<*>): _MethodMirror {
            return _MethodMirror(mirror);
        }

        fun fromMirror(mirror: MethodMirror): _MethodMirror2 {
            return _MethodMirror2(mirror);
        }
    }

    fun fromKCallable(member: KFunction<*>): AstDeclaration {
        return _MethodMirror.fromMirror(member)
    }

    val isMutable: Boolean
        //        get() = kProperty is KMutableProperty<*>
        get() = true

    val setter: KCallable<*>?
        get() = if (isMutable) (kProperty as? KMutableProperty<*>)?.setter else null


    val isConstConstructor: Boolean
        get() = false // Kotlin does not have a direct equivalent of Dart's const constructors


    val isRedirectingConstructor: Boolean
        get() = false // Kotlin does not have redirecting constructors

    override val owner: AstDeclaration?
        get() = null // Unimplemented for simplicity
    override val isPrivate: Boolean
        get() = TODO("Not yet implemented")

    private var _parameters: List<AstParameter>? = null

    override val parameters: List<_ParameterImpl>
        get() {
            return mutableListOf()
        }
    override val isStatic: Boolean
        get() = TODO("Not yet implemented")
    override val isAbstract: Boolean
        get() = TODO("Not yet implemented")

    override val returnType: AstType
        get() = throw UnimplementedError("returnType error")

    override val simpleName: String
        get() = _methodMirror?.simpleName ?: ""

    override val qualifiedName: String
        get() = _methodMirror?.qualifiedName ?: ""

    override fun toString(): String = "_MethodMirror($qualifiedName)"


    override val isSynthetic: Boolean
        //        get() = kFunction.isSynthetic
        get() = true
    override val isRegularMethod: Boolean
        get() = TODO("Not yet implemented")
    override val isGetter: Boolean
        get() = TODO("Not yet implemented")
    override val isSetter: Boolean
        get() = TODO("Not yet implemented")
    override val isOperator: Boolean
        get() = TODO("Not yet implemented")
    override val isConstructor: Boolean
        get() = TODO("Not yet implemented")
    override val constructorName: String
        get() = TODO("Not yet implemented")

    override val programNode: AstRuntime.ProgramNode
        get() = throw UnsupportedOperationException()
}


class _MethodMirror(val kFunction: KFunction<*>) : AstMethod {

    var function: KFunction<*>? = null

    init {
        this.function = kFunction
    }

    companion object {
        fun fromMirror(mirror: KFunction<*>): _MethodMirror {
            return _MethodMirror(mirror);
        }

        fun fromMirror(mirror: MethodMirror): _MethodMirror2 {
            return _MethodMirror2(mirror);
        }
    }

    fun fromKCallable(member: KFunction<*>): AstDeclaration {
        return fromMirror(member)
    }

    val isBound: Boolean
        get() = kFunction.isInstanceBound()

    private fun KFunction<*>.isInstanceBound(): Boolean {
        return when {
            this is Function<*> -> {
                // 如果是 lambda 或局部函数，可能无法直接判断
                false
            }

            else -> {
                // 检查是否有接收者参数
//                parameters.firstOrNull()?.isInstanceParameter == true
                true
            }
        }
    }

    val isMutable: Boolean
        //        get() = kProperty is KMutableProperty<*>
        get() = true

    val setter: KCallable<*>?
        get() = if (isMutable) (kProperty as? KMutableProperty<*>)?.setter else null

    val declaringClass: KClass<*>?
        get() = kFunction.declaringClass()

    private fun KFunction<*>.declaringClass(): KClass<*>? {
        return when {
            javaMethod != null -> javaMethod!!.declaringClass.kotlin // 如果是 Java 方法
            else -> this.parameters.firstOrNull()?.type?.classifier as? KClass<*>
        }
    }

    override val constructorName: String
        get() = if (kFunction.name == "<init>") declaringClass?.simpleName ?: "" else ""

    override val isAbstract: Boolean
        get() = kFunction.isAbstract

    val isConstConstructor: Boolean
        get() = false // Kotlin does not have a direct equivalent of Dart's const constructors

    override val isConstructor: Boolean
        get() = kFunction.name == "<init>"

    val isFactoryConstructor: Boolean
        get() = false // Kotlin does not have factory constructors

    override val isGetter: Boolean
        get() = kFunction is KProperty<*> && !setter!!.isAccessible

    override val isOperator: Boolean
        get() = kFunction.name.startsWith("operator")

    override val isPrivate: Boolean
        get() = Modifier.isPrivate(kFunction.javaMethod?.modifiers ?: 0)

    val isRedirectingConstructor: Boolean
        get() = false // Kotlin does not have redirecting constructors

    override val isSetter: Boolean
        get() = kFunction is KMutableProperty<*>

    override val isStatic: Boolean
        get() = isBound


    override val owner: AstDeclaration?
        get() = null // Unimplemented for simplicity

    private var _parameters: List<AstParameter>? = null

    override val parameters: List<_ParameterImpl>
        get() {
//            if (_parameters == null) {
//                _parameters = kFunction.parameters.map { AstParameter.fromKParameter(it) }
//            }
//            return _parameters!!
            return mutableListOf()
        }

    override val returnType: AstType
        get() = throw UnimplementedError("returnType error")

    override val simpleName: String
        get() = kFunction.name

    override val qualifiedName: String
        get() = "${kFunction.name}"

    override fun toString(): String = "_MethodMirror($qualifiedName)"

    override val isRegularMethod: Boolean
        get() = kFunction.name != "<init>" && kFunction.name != "<clinit>"

    override val isSynthetic: Boolean
        //        get() = kFunction.isSynthetic
        get() = true

    override val programNode: AstRuntime.ProgramNode
        get() = throw UnsupportedOperationException()
}

private fun <T> processArguments(
    parameters: List<AstParameter>?,
    positionalArgs: List<Any?>,
    namedArgs: Map<String, Any?>?
) {
//    parameters.forEachIndexed { i, param ->
//        when {
//            param.isNamed && namedArgs?.containsKey(param.simpleName) == true -> {
//                namedArgs[param.simpleName] =
//                    convertType(param, namedArgs.getValue(param.simpleName))
//            }
//            i < positionalArgs.size -> {
//                positionalArgs[i] = convertType(param, positionalArgs[i])
//            }
//        }
//    }
}


// _InstanceMirror.kt
//class _InstanceMirror(private val _instanceMirror: InstanceMirror) : AstObject(), AstInstance {
//    override val type: AstClass get() = AstClass.fromMirror(_instanceMirror.type)
//
//    override fun invoke(memberName: String, positionalArguments: List<Any?>, namedArguments: Map<Symbol, Any?>?): Any? {
//        return context.run{
//            val method = (type as _ClassMirror).instanceMembers[memberName]
//                ?: throw NoSuchMethodError("Method $memberName not found")
//            processArguments(method.parameters, positionalArguments, namedArguments)
//            _instanceMirror.invoke(memberName, positionalArguments, namedArguments)
//        }
//    }
// 其他方法转换...
//}

//private fun convertType(param: AstParameter, value: Any?): Any? {
//    return when (param.reflectedType) {
//        Double::class -> value?.toString()?.toDoubleOrNull()
//        else -> value
//    }
//}

