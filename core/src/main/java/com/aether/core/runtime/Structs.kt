package com.aether.core.runtime

import com.aether.core.runtime.AppContextManager.context
import com.aether.core.runtime.proxy.ProxyBinding
import org.jetbrains.kotlin.resolve.calls.model.FunctionExpression
import kotlin.reflect.KClass
import kotlin.reflect.KFunction


interface Present

interface AstDeclaration : Present {
    abstract val simpleName: String
    abstract val qualifiedName: String
    abstract val owner: AstDeclaration?
    abstract val isPrivate: Boolean
    abstract val programNode: AstRuntime.ProgramNode?
}

interface AstObject : Present {
    abstract fun invoke(
        memberName: String,
        positionalArguments: List<Any?>,
        namedArguments: Map<String, Any?>? = null
    ): Any?

    abstract fun invokeGetter(getterName: String): Any?

    abstract fun hasGetter(getterName: String): Boolean

    abstract fun invokeSetter(setterName: String, value: Any?): Any?

    abstract fun hasSetter(setterName: String): Boolean

    abstract fun hasRegularMethod(methodName: String): Boolean

//    override fun equals(other: Any?): Boolean {
//        if (hasGetter("==")) {
//            return invoke("==", listOf(other)) as? Boolean ?: false
//        }
//        return super.equals(other)
//    }
}

abstract class AstType : AstDeclaration

abstract class AstClass : AstType(), AstObject {
    abstract val superclass: AstClass?
    abstract val mixins: List<AstClass>?
    abstract val isAbstract: Boolean
    abstract val declarations: Map<String, AstDeclaration>
    abstract val instanceFields: Map<String, AstVariable>
    abstract val instanceGetters: Map<String, AstMethod>
    abstract val instanceSetters: Map<String, AstMethod>
    abstract val staticFields: Map<String, AstVariable>
    abstract val staticGetters: Map<String, AstMethod>
    abstract val staticSetters: Map<String, AstMethod>

    abstract fun newInstance(
        constructorName: String,
        positionalArguments: List<Any?>,
        namedArguments: Map<String, Any?>? = null
    ): Any?

    abstract fun isSubclassOf(other: AstClass): Boolean

    abstract fun hasConstructor(constructorName: String): Boolean

    companion object {
        fun fromClass(
            declaration: ClassDeclaration,
            programNode: AstRuntime.ProgramNode
        ): AstClass {
            // 实现逻辑
            return _ClassImpl.fromClass(declaration, programNode)
        }

        fun fromMirror(mirror: KClass<*>, programNode: AstRuntime.ProgramNode): AstClass {
            // 实现逻辑
            return _ClassMirror.fromMirror(mirror, programNode);
        }

        fun forName(className: String): AstClass? {
            val runtime = context.get<AstRuntime>(AstRuntime::class.java) ?: return null
            val clazz = runtime.getClass(className)
            if (clazz != null) return clazz
            //TODO zhangerwei 这里可以直接去加载反射的类。

            val importDirective = runtime.getReflectClass(className)
            var mirror: KClass<*>? = ProxyBinding.instance?.getProxyClassForName(importDirective)
//            if (mirror == null) {
//                mirror = ReflectionBinding.instance.reflectType(className)
//            }
            if (mirror != null) {
                return fromMirror(mirror, runtime._program)
            }
            return null
        }
    }
}

class _VariableImpl(
    name: String,
    typeName: String?,
    private val _initializer: Expression?,
    isPrivate: Boolean,
    isTopLevel: Boolean,
    isStatic: Boolean,
    isConst: Boolean,
    isFinal: Boolean
) : _VariableBase(
    name,
    typeName,
    isPrivate,
    isTopLevel,
    isStatic,
    isFinal,
    isConst
) {
    val initializer: Expression?
        get() = _initializer

    companion object {

        fun fromDeclarator(
            name: String,
            typeName: String?,
            _initializer: Expression?,
            isPrivate: Boolean = false,
            isTopLevel: Boolean = false,
            isStatic: Boolean = false,
            isConst: Boolean = false,
            isFinal: Boolean = false
        ): _VariableImpl {
            return _VariableImpl(
                name,
                typeName,
                _initializer,
                isPrivate,
                isTopLevel,
                isStatic,
                isConst,
                isFinal
            )
        }
    }
}

class _ClassImpl(
    private val _name: String,
    override val superclass: AstClass?,
    override val mixins: List<AstClass>?,
    private val _staticFields: Map<String, _VariableImpl>,
    private val _staticGetters: Map<String, _MethodImpl>,
    private val _staticSetters: Map<String, _MethodImpl>,
    val _instanceFields: Map<String, _VariableImpl>,
    val _instanceGetters: Map<String, _MethodImpl>,
    val _instanceSetters: Map<String, _MethodImpl>,
    private val _constructors: Map<String, _ConstructorImpl>,
    private val _node: ClassDeclaration,
    override val programNode: AstRuntime.ProgramNode
) : AstClass() {

    public lateinit var _classScope: StackScope
    public lateinit var runtime: AstRuntime

    init {
        val programStack = context.get<ProgramStack>(ProgramStack::class.java)!!
        val classScope = StackScope("$simpleName").apply {
            set("node", _node)
        }

        programStack.push(scope = classScope)

        _staticFields.values.forEach { staticField ->
            programStack.putLazilyVariable(staticField.simpleName) {
                staticField.initializer?.let {
                    programStack.push(name = "Static field initializer")
                    val result = executeExpression(it)
                    programStack.pop()
                    result
                }
            }
        }

        _staticGetters.values.forEach { method ->
            programStack.put(method.simpleName, method)
        }

        _classScope = programStack.pop()!!
        runtime = context.get<AstRuntime>(AstRuntime::class.java)!!
    }

    override val simpleName: String get() = _name
    override val qualifiedName: String get() = _name
    override val owner: AstDeclaration?
        get() = TODO("Not yet implemented")
    override val declarations: Map<String, AstDeclaration>
        get() = throw NotImplementedError()

    override val instanceFields: Map<String, AstVariable>
        get() = _instanceFields

    private var _inheritedGetters: Map<String, AstMethod>? = null
    override val instanceGetters: Map<String, AstMethod>
        get() {
            _inheritedGetters?.let {
                return mapOf(*_instanceGetters.toList().toTypedArray(), *it.toList().toTypedArray())
            }

            val result = mutableMapOf<String, AstMethod>().apply {
                superclass?.instanceGetters?.let { putAll(it) }
            }
            _inheritedGetters = result.toMap()
            return _instanceGetters
        }

    private var _inheritedSetters: Map<String, AstMethod>? = null
    override val instanceSetters: Map<String, AstMethod>
        get() {
            _inheritedSetters?.let {
                return mapOf(*_instanceSetters.toList().toTypedArray(), *it.toList().toTypedArray())
            }

            val result = mutableMapOf<String, AstMethod>().apply {
                superclass?.instanceSetters?.let { putAll(it) }
            }
            _inheritedSetters = result.toMap()
            return _instanceSetters
        }

    override val staticFields: Map<String, AstVariable> get() = _staticFields
    override val staticGetters: Map<String, AstMethod> get() = _staticGetters
    override val staticSetters: Map<String, AstMethod> get() = _staticSetters

    override fun invoke(
        memberName: String,
        positionalArguments: List<Any?>,
        namedArguments: Map<String, Any?>?
    ): Any? {
        val method = _staticGetters[memberName] ?: throw NoSuchMethodError()
        return context.run(
            name = "Static invoke",
            overrides = mapOf(
                AstRuntime.ProgramNode::class.java to { programNode }
            ),
            body = {
                val programStack = context.get<ProgramStack>(ProgramStack::class.java)!!
                programStack.push(scope = _classScope)
                val result = AstMethod.apply2(method, positionalArguments, namedArguments)
                programStack.pop()
                (result as? Variable)?.value ?: result
            }
        )
    }

    override fun invokeGetter(getterName: String): Any? {
        val programStack = context.get<ProgramStack>(ProgramStack::class.java)!!
        return try {
            programStack.push(scope = _classScope)
            context.run(
                name = "Static invokeGetter",
                overrides = mapOf(AstRuntime.ProgramNode::class.java to { programNode }),
                body = {
                    _invokeGetter(getterName)
                }
            )
        } finally {
            programStack.pop()
        }
    }

    private fun _invokeGetter(getterName: String): Any? {
        val programStack = context.get<ProgramStack>(ProgramStack::class.java)!!
        return _staticGetters[getterName]?.let {
            AstMethod.apply2(it, emptyList(), mutableMapOf())
        } ?: _staticFields[getterName]?.let {
            programStack.getVariable(it.simpleName)?.value
        } ?: throw NoSuchMethodError("getterName=$getterName")
    }

    override fun invokeSetter(setterName: String, value: Any?): Any? {
        val programStack = context.get<ProgramStack>(ProgramStack::class.java)!!
        return try {
            programStack.push(scope = _classScope)
            context.run(
                name = "Static invokeSetter",
                overrides = mapOf(AstRuntime.ProgramNode::class.java to { programNode }),
                body = {
                    _invokeSetter(setterName, value)
                }
            )
        } finally {
            programStack.pop()
        }
    }

    override fun hasSetter(setterName: String): Boolean {
        return _staticSetters.containsKey(setterName) || _staticFields.containsKey(setterName)
    }

    private fun _invokeSetter(setterName: String, value: Any?): Any? {
        val programStack = context.get<ProgramStack>(ProgramStack::class.java)!!
        return _staticSetters[setterName]?.let {
            AstMethod.apply2(it, listOf(value), mapOf())
        } ?: _staticFields[setterName]?.let {
            val variable = programStack.getVariable(it.simpleName)
            if (variable != null) {
                variable?.value = value
                return null;
            }
        } ?: throw NoSuchMethodError("setterName=$setterName")
    }

    override val isAbstract: Boolean get() = throw NotImplementedError()
    override val isPrivate: Boolean get() = throw NotImplementedError()

    override fun isSubclassOf(other: AstClass): Boolean {
        throw NotImplementedError()
    }

    override fun newInstance(
        constructorName: String,
        positionalArguments: List<Any?>,
        namedArguments: Map<String, Any?>?
    ): Any? {

        return context.run(
            name = "Constructor scope",
            body = {
                val constructor = _constructors[constructorName] ?: throw NoSuchMethodError()
                val programStack = context.get<ProgramStack>(ProgramStack::class.java)!!

                programStack.push(scope = _classScope)
                programStack.push(name = "Instance scope")

                instanceFields.values.forEach { variable ->
                    val value =
                        (variable as? _VariableImpl)?.initializer?.let { executeExpression(it) }
                    programStack.putVariable(variable.simpleName, value)
                }

                instanceGetters.values.forEach { method ->
                    programStack.put(method.simpleName, method)
                }

                val instanceScope = programStack.pop()!!
                lateinit var instance: _InstanceImpl

                _MethodImpl.readyForScopedInvoke(
                    constructor.parameters, positionalArguments, namedArguments
                ) { args, namedArgs ->
                    programStack.push(scope = instanceScope)
                    constructor.parameters.forEachIndexed { i, param ->
                        if (param.isField) {
                            val field = _instanceFields[param.simpleName]!!
                            val value =
                                if (param.isNamed) namedArgs?.get(param.simpleName) else args[i]//param.simpleName 临时加的
                            programStack.putVariable(
                                param.simpleName,
                                value?.takeIf { field.typeName == "double" })
                        }
                    }
                    programStack.pop()

                    instance = _InstanceImpl.build(this@_ClassImpl, instanceScope)

                    superclass?.let {
                        // Superclass initialization logic
                    }

                    programStack.push(scope = instanceScope)
                    programStack.push(name = "Constructor body scope")

                    val result = context.run(
                        name = "Constructor body scope",
                        overrides = mapOf(ThisReference::class.java to { ThisReference(instance) }
                        ),
                        body = {
                            executeExpression(constructor.body)
                        }
                    )

                    programStack.pop()
                    programStack.pop()
                    result
                }

                instance
            },
            overrides = mapOf(
                ProgramStack::class.java to { runtime.programStack },
                AstRuntime::class.java to { runtime })
        )
    }

    override fun hasConstructor(constructorName: String): Boolean {
        return _constructors.containsKey(constructorName)
    }

    override fun hasGetter(getterName: String): Boolean {
        throw NotImplementedError()
    }

    override fun hasRegularMethod(methodName: String): Boolean {
        throw NotImplementedError()
    }

    companion object {
        fun fromClass(
            declaration: ClassDeclaration,
            programNode: AstRuntime.ProgramNode
        ): _ClassImpl {
            val staticFields = mutableMapOf<String, _VariableImpl>()
            val staticGetters = mutableMapOf<String, _MethodImpl>()
            val staticSetters = mutableMapOf<String, _MethodImpl>()
            val instanceFields = mutableMapOf<String, _VariableImpl>()
            val instanceGetters = mutableMapOf<String, _MethodImpl>()
            val instanceSetters = mutableMapOf<String, _MethodImpl>()
            val constructors = mutableMapOf<String, _ConstructorImpl>()
//                val superclass = declaration.extendsClause?.let {
//                    val superclassName = it.superclass
//                    AstClass.forName(superclassName) ?: throw IllegalStateException("Unable to determine superclass: $superclassName")
//                }
//
//                val mixins = mutableListOf<AstClass>().apply {
//                    declaration.withClause?.mixinTypes?.forEach { mixinType ->
//                        add(AstClass.forName(mixinType) ?: throw IllegalStateException("Unable to determine mixin: $mixinType"))
//                    }
//                }
            declaration.members?.forEach { member ->
                when {
                    member.isClassDeclaration -> {
                        member.asClassDeclaration.members?.forEach {
                            when {
                                it.isMethodDeclaration -> {
                                    val method = it.asMethodExpression
                                    if (method.isStatic) {
                                        if (method.isSetter) {
                                            staticSetters[method.name] =
                                                _MethodImpl.fromMethod(method);
                                        } else {
                                            staticGetters[method.name] =
                                                _MethodImpl.fromMethod(method);
                                        }
                                    }
                                    when {
//                                        method.isSetter -> setters[method.name] = _MethodImpl.fromMethod(method)
//                                        else -> getters[method.name] = _MethodImpl.fromMethod(method)
                                    }
                                }
                            }
                        }
                    }

                    member.isMethodDeclaration -> {
                        val method = member.asMethodExpression
                        if (method.isStatic) {
                            if (method.isSetter) {
                                staticSetters[method.name] =
                                    _MethodImpl.fromMethod(method);
                            } else {
                                staticGetters[method.name] =
                                    _MethodImpl.fromMethod(method);
                            }
                        } else {
                            if (method.isSetter) {
                                instanceSetters[method.name] =
                                    _MethodImpl.fromMethod(method);
                            } else {
                                instanceGetters[method.name] =
                                    _MethodImpl.fromMethod(method);
                            }
                        }
                    }

                    member.isConstructorDeclaration -> {
                        val constructor = member.asConstructorDeclaration
                        constructors[constructor.name] =
                            _ConstructorImpl.fromConstructor(constructor)
                    }

                    member.isFieldDeclaration -> {
                        val field = member.asFieldDeclaration
                        instanceFields[field.name] = _VariableImpl.fromDeclarator(
                            name = field.name,
                            typeName = field.name,
                            _initializer = field.target
                        )
//                        if (field)
//
//                        fields.fields.declarationList.forEach { declarator ->
//                            val targetMap =
//                                if (fields.isStatic) staticFields else instanceFields
//                            targetMap[declarator.name] =
//                                _VariableImpl.fromDeclarator(declarator)
//                        }
                    }
                }
//                it.asClassDeclaration.members[0]
            }

//            declaration.members[0].asClassDeclaration.members[0]
            declaration.members?.forEach { member ->
                when {
//                        member.isConstructorDeclaration -> {
//                            val constructor = member.asConstructorDeclaration
//                            constructors[constructor.name] = _ConstructorImpl.fromConstructor(constructor)
//                        }
//                        member.isFieldDeclaration -> {
//                            val fields = member.asFieldDeclaration
//                            fields.fields.declarationList.forEach { declarator ->
//                                val targetMap = if (fields.isStatic) staticFields else instanceFields
//                                targetMap[declarator.name] = _VariableImpl.fromDeclarator(declarator)
//                            }
//                        }
//                        member.isMethodDeclaration -> {
//                            val method = member.asMethodDeclaration
//                            val (getters, setters) = when (method.isStatic) {
//                                true -> staticGetters to staticSetters
//                                false -> instanceGetters to instanceSetters
//                            }
//
//                            when {
//                                method.isSetter -> setters[method.name] = _MethodImpl.fromMethod(method)
//                                else -> getters[method.name] = _MethodImpl.fromMethod(method)
//                            }
//                        }
                }
            }

            if (constructors.isEmpty()) {
                constructors[""] = _ConstructorImpl.defaultConstructor()
            }

            return _ClassImpl(
                _name = declaration.name,
//                    superclass = superclass,
//                    mixins = mixins,
                superclass = null,
                mixins = null,
                _staticFields = staticFields,
                _staticGetters = staticGetters,
                _staticSetters = staticSetters,
                _instanceFields = instanceFields,
                _instanceGetters = instanceGetters,
                _instanceSetters = instanceSetters,
                _constructors = constructors,
                _node = declaration,
                programNode = programNode,
            )
        }
    }
}

class _InstanceImpl(
    private val _type: _ClassImpl,
    private val _instanceScope: StackScope
) : AstObject, AstInstance() {

    var ancestor: AstInstance? = null
        private set

    private val _behaviors = mutableListOf<AstInstance>()

    init {
        // 初始化逻辑（如有需要）
    }

    override val type: _ClassImpl
        get() = _type

    override fun invoke(
        methodName: String,
        positionalArguments: List<Any?>,
        namedArguments: Map<String, Any?>?
    ): Any? {
        return context.run(
            name = "Instance scope",
            overrides = mapOf(
                ThisReference::class.java to { ThisReference(this) },
                SuperReference::class.java to { SuperReference(ancestor) },
                ProgramStack::class.java to { _type.runtime.programStack },
                AstRuntime::class.java to { _type.runtime },
                AstRuntime.ProgramNode::class.java to { _type.programNode }
            ),
            body = {
                val programStack = context.get<ProgramStack>(ProgramStack::class.java)
                val method = _type._instanceGetters[methodName]

                method?.let {
                    programStack?.push(scope = _type._classScope)
                    programStack?.push(scope = _instanceScope)

                    val result = _MethodImpl.readyForScopedInvoke(
                        it.parameters,
                        positionalArguments,
                        namedArguments
                    ) { args, namedArgs ->
                        val scope = StackScope("$methodName").apply {
                            it.body.expression?.parent?.let { parent -> set("node", parent) }
                        }
                        programStack?.push(scope = scope)
                        val executeResult = executeExpression(it.body)
                        programStack?.pop()
                        (executeResult as? Variable)?.value ?: executeResult
                    }

                    programStack?.pop()
                    programStack?.pop()
                    return@run result
                }

                ancestor?.takeIf { it.hasGetter(methodName) }?.let {
                    return@run it.invoke(methodName, positionalArguments, namedArguments)
                }

                throw NoSuchMethodError("NoSuchMethodError: $methodName")
            }
        )
    }

    override fun invokeGetter(getterName: String): Any? {
        return context.run(
            name = "Instance scope",
            overrides = mapOf(
                ThisReference::class.java to { ThisReference(this) },
                SuperReference::class.java to { SuperReference(ancestor) },
                ProgramStack::class.java to { _type.runtime.programStack },
                AstRuntime::class.java to { _type.runtime },
                AstRuntime.ProgramNode::class.java to { _type.programNode }
            ),
            body = {
                val programStack = context.get<ProgramStack>(ProgramStack::class.java)
                return@run try {
                    programStack?.push(scope = _type._classScope)
                    programStack?.push(scope = _instanceScope)
                    _invokeGetter(getterName).let {
                        (it as? Variable)?.value ?: it
                    }
                } finally {
                    programStack?.pop()
                    programStack?.pop()
                }
            }
        )
    }

    private fun _invokeGetter(getterName: String): Any? {
        val programStack = context.get<ProgramStack>(ProgramStack::class.java)!!

        _type._instanceGetters[getterName]?.takeIf { it.isGetter }?.let { method ->
            return _MethodImpl.readyForScopedInvoke(
                method.parameters,
                emptyList(),
                null
            ) { _, _ ->
                programStack.push(name = "Invoke getter $getterName")
                val result = executeExpression(method.body)
                programStack.pop()
                result
            }
        }

        _type._instanceFields[getterName]?.let { field ->
            return programStack.getVariable(field.simpleName)
        }

        ancestor?.takeIf { it.hasGetter(getterName) }?.let {
            return it.invokeGetter(getterName)
        }

        throw NoSuchMethodError("getterName=$getterName")
    }

    override fun invokeSetter(setterName: String, value: Any?): Any? {
        return context.run(
            name = "Instance scope",
            overrides = mapOf(
                ThisReference::class.java to { ThisReference(this) },
                SuperReference::class.java to { SuperReference(ancestor) },
                ProgramStack::class.java to { _type.runtime.programStack },
                AstRuntime::class.java to { _type.runtime },
                AstRuntime.ProgramNode::class.java to { _type.programNode }
            ),
            body = {
                val programStack = context.get<ProgramStack>(ProgramStack::class.java)!!
                return@run try {
                    programStack.push(scope = _type._classScope)
                    programStack.push(scope = _instanceScope)
                    _invokeSetter(setterName, value)
                } finally {
                    programStack.pop()
                    programStack.pop()
                }
            }
        )
    }

    override fun hasSetter(setterName: String): Boolean {
        return _type.instanceSetters.containsKey(setterName) ||
                _type.instanceFields.containsKey(setterName)
    }

    private fun _invokeSetter(setterName: String, value: Any?): Any? {
        val programStack = context.get<ProgramStack>(ProgramStack::class.java)!!

        _type._instanceSetters[setterName]?.takeIf { it.isSetter }?.let { method ->
            return _MethodImpl.readyForScopedInvoke(
                method.parameters,
                listOf(value),
                null
            ) { _, _ ->
                programStack.push(name = "Invoke setter $setterName")
                val result = executeExpression(method.body)
                programStack.pop()
                result
            }
        }

        _type._instanceFields[setterName]?.let { field ->
            programStack.getVariable(field.simpleName)?.apply {
                this.value = value
            }
            return null
        }

        ancestor?.takeIf { it.hasSetter(setterName) }?.let {
            return it.invokeSetter(setterName, value)
        }

        throw NoSuchMethodError("setterName=$setterName")
    }

    override fun hasGetter(getterName: String): Boolean {
        return _type.instanceGetters.containsKey(getterName) ||
                _type.instanceFields.containsKey(getterName)
    }

    override fun hasRegularMethod(methodName: String): Boolean {
        return _type.instanceGetters[methodName]?.isRegularMethod ?: false
    }

    companion object {
        fun build(type: _ClassImpl, scope: StackScope): _InstanceImpl {
            return _InstanceImpl(type, scope)
        }
    }
}

class _ConstructorImpl(
    name: String,
    body: Expression,
    parameters: List<_ParameterImpl>,
    isAbstract: Boolean,
    isSynthetic: Boolean,
    isGetter: Boolean,
    isSetter: Boolean,
    isStatic: Boolean,
    isPrivate: Boolean,
    isOperator: Boolean,
    isConstructor: Boolean,
    val isConstConstructor: Boolean,
    val isFactoryConstructor: Boolean,
    val isRedirectingConstructor: Boolean,
    val initializers: List<Expression>
) : _MethodBase(
    _name = name,
    _body = body,
    _isAbstract = isAbstract,
    _isSynthetic = isSynthetic,
//    _isExternal = false,
    _parameters = parameters,
//    _isGetter = isGetter,
//    _isSetter = isSetter,
    _isStatic = isStatic,
//    _isPrivate = isPrivate,
    _isOperator = isOperator,
    _isConstructor = isConstructor
) {

    companion object {
        fun defaultConstructor(): _ConstructorImpl {
            return _ConstructorImpl(
                name = "",
                body = Expression.fromUnit(mapOf("type" to "EmptyFunctionBody")),
                parameters = emptyList(),
                isAbstract = false,
                isSynthetic = true,
                isGetter = false,
                isSetter = false,
                isStatic = false,
                isPrivate = false,
                isOperator = false,
                isConstructor = true,
                isConstConstructor = false,
                isFactoryConstructor = false,
                isRedirectingConstructor = false,
                initializers = emptyList()
            )
        }

        fun fromConstructor(declaration: ConstructorDeclaration): _ConstructorImpl {
            val parameters = declaration.parameters.parameters.map {
                _ParameterImpl.fromParameter(it as FormalParameter)
            }.toList()

            return _ConstructorImpl(
                name = declaration.name,
                body = declaration.body,
                parameters = parameters,
                isAbstract = false,
                isSynthetic = false,
                isGetter = false,
                isSetter = false,
                isStatic = false,
                isPrivate = Identifier.isPrivateName(declaration.name),
                isOperator = false,
                isConstructor = true,
                isConstConstructor = declaration.isConstConstructor,
                isFactoryConstructor = declaration.isFactoryConstructor,
                isRedirectingConstructor = declaration.isRedirectingConstructor,
                initializers = declaration.initializers
            )
        }
    }
}

abstract class AstInstance : AstObject {
    abstract val type: AstClass

    companion object {

        // todu 2.16 zhangerwei完成反射的开发
        fun forObject(obj: Any?, proxy: Boolean = true): AstInstance? {
            if (obj == null) return null
            if (obj is AstInstance) return obj
            //反射
            return null
//            if (proxy && obj is AstProxy) return obj.instance
//            val mirror = ReflectionBinding.instance.reflect(obj)
//            return mirror?.let { AstInstance.fromMirror(it) }
        }
    }
}

class ConstructorDeclaration(
    val name: String,
    val parameters: FormalParameterList,
    val initializers: List<Expression>,
    val body: Expression,
    val isConstConstructor: Boolean,
    val isFactoryConstructor: Boolean,
    val isRedirectingConstructor: Boolean,
    unit: Map<String, Any>
) : Node(unit) {

    init {
        parameters.parent = this
        body.parent = this
    }

    companion object {
        fun fromUnit(unit: Map<String, Any>): ConstructorDeclaration {
            if (unit["type"] != "SecondaryConstructor") {
                parseError(unit)
            }
            val name = unit["name"] ?: ""
//            val name = unit["name"]?.let {
//                Identifier.fromUnit(it as Map<String, Any>).name
//            } ?: ""

            val initializers = (unit["initializers"] as? List<Map<String, Any>>)?.map {
                Expression.fromUnit(it)
            }?.toList() ?: emptyList()
            val emptyUnit = mutableMapOf<String, Any>()
            emptyUnit["type"] = "EmptyFunctionBody"
            return ConstructorDeclaration(
                name = name as String,
                parameters = FormalParameterList.fromUnit(
                    unit["parameters"] as Map<String, Any>
                ),
                initializers = initializers,
                body = if (unit["body"] != null) Expression.fromUnit(unit["body"] as Map<String, Any>) else Expression(
                    EmptyFunctionBody.fromUnit(emptyUnit), isEmptyFunctionBody = true, unit = unit
                ),
                isConstConstructor = unit["isConstConstructor"] as? Boolean ?: false,
                isFactoryConstructor = unit["isFactoryConstructor"] as? Boolean ?: false,
                isRedirectingConstructor = unit["isRedirectingConstructor"] as? Boolean ?: false,
                unit = unit
            )
        }
    }
}

abstract class AstVariable : AstDeclaration {
    abstract val type: AstType
    abstract override val isPrivate: Boolean
    abstract val isTopLevel: Boolean
    abstract val isStatic: Boolean
    abstract val isFinal: Boolean
    abstract val isConst: Boolean

    companion object {

        inline fun <reified T : AstVariable> fromDeclarator(declarator: VariableDeclarator): T {
            // 实现逻辑
            TODO("Not yet implemented")
        }

//        inline fun <reified T : AstVariable> fromMirror(mirror: VariableMirrorBase): T {
//            // 实现逻辑
//            TODO("Not yet implemented")
//        }
//
//        fun applyTopLevelVariable(variable: AstVariable) {
//            val programStack = context.get<ProgramStack>() ?: return
//            programStack.putLazilyVariable(variable.simpleName) {
//                if (variable is _VariableImpl && variable.initializer != null) {
//                    val result = executeExpression(variable.initializer!!)
//                    result
//                } else {
//                    null
//                }
//            }
//        }
    }
}

abstract class AstParameter : AstVariable() {
    abstract val isOptional: Boolean
    abstract val isNamed: Boolean
    abstract val hasDefaultValue: Boolean
    abstract val defaultValue: Any?

    companion object {
//        inline fun <reified T : AstParameter> fromParameter(parameter: FormalParameter): T {
//            // 实现逻辑
//            TODO("Not yet implemented")
//        }
//
//        inline fun <reified T : AstParameter> fromMirror(mirror: ParameterMirror): T {
//            // 实现逻辑
//            TODO("Not yet implemented")
//        }
    }
}

interface AstMethod : AstDeclaration {
    val returnType: AstType
    val parameters: List<_ParameterImpl>
    val isStatic: Boolean
    val isAbstract: Boolean
    val isSynthetic: Boolean
    val isRegularMethod: Boolean
    val isGetter: Boolean
    val isSetter: Boolean
    val isOperator: Boolean
    val isConstructor: Boolean
    val constructorName: String

    companion object {
//        fun defaultConstructor(): AstMethod = _ConstructorImpl.defaultConstructor()

//        fun fromConstructor(declaration: ConstructorDeclaration): AstMethod =
//            _ConstructorImpl.fromConstructor(declaration)

//        fun fromMethod(declaration: MethodDeclaration): AstMethod =
//            _MethodImpl.fromMethod(declaration)

        fun fromFunction(
            declaration: MethodDeclaration,
            programNode: AstRuntime.ProgramNode
        ): AstMethod =
            _MethodImpl.fromFunction(declaration, programNode)

        //
        fun fromExpression(expression: MethodDeclaration): AstMethod =
            _MethodImpl.fromExpression(expression)

        fun fromMirror(mirror: KFunction<*>): AstMethod = _MethodMirror.fromMirror(mirror)

//        fun forTopLevelFunction(name: String): AstMethod? {
//            val programNode = context.get<AstRuntime.ProgramNode>() ?: return null
//            var function = programNode.getFunction(name)
//            if (function != null) {
//                return function
//            }
//
//            val libraryMirror = ReflectionBinding.instance.reflectTopLevelInvoke(name)
//            if (libraryMirror != null) {
//                return fromMirror(libraryMirror.declarations[name] as MethodMirror)
//            }
//            return null
//        }

//        fun readyForScopedInvoke(
//            parameters: List<_ParameterImpl>?,
//            positionalArguments: List<Any?>,
//            namedArguments: Map<String, Any?>?,
//            invoke: (List<Any?>, Map<String, Any?>?) -> Any?
//        ): Any? =
//            _MethodImpl.readyForScopedInvoke(parameters, positionalArguments, namedArguments, invoke)

        fun apply2(
            method: AstMethod,
            positionalArguments: List<Any?>,
            namedArguments: Map<String, Any?>?
        ): Any? {
            if (method.isStatic) {
                if (method is _MethodImpl) {
                    return _MethodImpl.readyForScopedInvoke(
                        method.parameters,
                        positionalArguments,
                        namedArguments
                    ) { args, namedArgs ->
                        val programStack = context.get<ProgramStack>(ProgramStack::class.java)
                            ?: return@readyForScopedInvoke null
                        programStack.push(name = "Static function body")
                        val result = executeExpression(method.body)
                        programStack.pop()
                        result
                    }
                } else {
//                    processArguments(method.parameters, positionalArguments, namedArguments)
                }
            } else {
                val ref = context.get(ThisReference::class.java)
                    ?: throw IllegalStateException("Invalid ThisReference")
                if (ref.value is AstInstance) {
                    val instance = ref.value
                    return when {
                        method.isGetter -> instance.invokeGetter(method.simpleName)
                        method.isSetter -> instance.invokeSetter(
                            method.simpleName,
                            positionalArguments[0]
                        )

                        else -> instance.invoke(
                            method.simpleName,
                            positionalArguments,
                            namedArguments
                        )
                    }
                }
                throw IllegalStateException("Invalid ThisReference")
            }
            return null
        }
    }
}

abstract class _MethodBase(
    private val _name: String,
    private val _body: Expression,
    private val _parameters: List<_ParameterImpl>,
    private val _isAbstract: Boolean,
    private val _isSynthetic: Boolean,
//    private val _isRegularMethod: Boolean,
//    private val _isGetter: Boolean,
//    private val _isSetter: Boolean,
    private val _isStatic: Boolean,
//    private val _isPrivate: Boolean,
    private val _isOperator: Boolean,
    private val _isConstructor: Boolean,
    override val programNode: AstRuntime.ProgramNode? = null
) : AstMethod {

    val body: Expression
        get() = _body

    override val qualifiedName: String
        get() = "${owner?.qualifiedName}.$simpleName"

    override val simpleName: String
        get() = if (_isConstructor) {
            if (_name.isEmpty()) owner.simpleName else "${owner.simpleName}.$simpleName"
        } else {
            _name
        }

    override val constructorName: String
        get() = if (_isConstructor) _name else ""

    override val isAbstract: Boolean
        get() = _isAbstract

    override val isSynthetic: Boolean
        get() = _isSynthetic

    override val isRegularMethod: Boolean
        get() = false

    override val isConstructor: Boolean
        get() = _isConstructor

    override val isGetter: Boolean
        get() = false

    override val isPrivate: Boolean
        get() = false

    override val isSetter: Boolean
        get() = false

    override val isStatic: Boolean
        get() = _isStatic

    override val owner: AstDeclaration
        get() = throw UnimplementedError("operator ${_name} error")

    //
    override val parameters: List<_ParameterImpl>
        get() = _parameters

    override val isOperator: Boolean
        get() = _isOperator

    override val returnType: AstType
        get() = throw UnimplementedError("operator ${_name} error")
}

public class UnimplementedError(s: String) : Throwable() {

}

class _MethodImpl(
    name: String,
    body: Expression,
    parameters: List<_ParameterImpl>,
    isAbstract: Boolean,
    isSynthetic: Boolean,
//    isRegularMethod: Boolean,
//    isGetter: Boolean,
//    isSetter: Boolean,
    isStatic: Boolean,
//    isPrivate: Boolean,
    isOperator: Boolean,
    isConstructor: Boolean,
    programNode: AstRuntime.ProgramNode? = null
) : _MethodBase(
    name,
    body,
    parameters,
    isAbstract,
    isSynthetic,
//    isRegularMethod,
//    isGetter,
//    isSetter,
    isStatic,
//    isPrivate,
    isOperator,
    isConstructor,
    programNode
) {

    companion object {
        fun fromExpression(expression: MethodDeclaration): AstMethod {
            val parameters = mutableListOf<_ParameterImpl>()
            if (expression.parameters?.parameters?.isNotEmpty() == true) {
                for (parameter in expression.parameters!!.parameters) {
                    parameters.add(_ParameterImpl.fromParameter(parameter))
                }
            }

            return _MethodImpl(
                name = "",
                body = expression.bodyExpression,
                parameters = parameters,
                isStatic = false,
                isAbstract = false,
                isOperator = false,
                isConstructor = false,
                isSynthetic = false
            )
        }

        fun fromMethod(declaration: MethodDeclaration): _MethodImpl {
            val parameters = mutableListOf<_ParameterImpl>()
//            declaration.valueParameters.forEach { parameter ->
//                parameters.add(_ParameterImpl.fromParameter(parameter))
//            }
            return _MethodImpl(
                declaration.name ?: "",
                declaration.bodyExpression,
                parameters,
                false,
                false,
//                !declaration.isGetter && !declaration.isSetter,
//                declaration.isGetter,
//                declaration.isSetter,
                true,
//                declaration.isPrivate,
                false,
                false,
            )
        }

        fun fromFunction(
            declaration: MethodDeclaration,
            programNode: AstRuntime.ProgramNode
        ): _MethodImpl {
            val parameters = mutableListOf<_ParameterImpl>()
            declaration.parameters?.parameters?.forEach { parameter ->
                parameters.add(_ParameterImpl.fromParameter(parameter))
            }

            return _MethodImpl(
                declaration.name ?: "",
                declaration.bodyExpression,
                parameters,
                false,
                false,
//                !declaration.isGetter && !declaration.isSetter,
//                declaration.isGetter,
//                declaration.isSetter,
                true,
//                declaration.isPrivate,
                false,
                false,
                programNode
            )
        }
        fun readyForScopedInvoke(
            parameters: List<_ParameterImpl>?,
            positionalArguments: List<Any?>,
            namedArguments: Map<String, Any?>?,
            invoke: (List<Any?>, Map<String, Any?>?) -> Any?
        ): Any? {
            val programStack = context.get<ProgramStack>(ProgramStack::class.java) ?: return null
            programStack.push(name = "Formal parameters scope")

            parameters?.forEachIndexed { index, parameter ->
                if (parameter.isNamed) {
                    programStack.putVariable(
                        parameter.simpleName,
                        namedArguments?.get(parameter.simpleName)
                    )
                } else {
                    programStack.putVariable(
                        parameter.simpleName,
                        positionalArguments.getOrNull(index)
                    )
                }
            }

            val result = invoke(positionalArguments, namedArguments)

            programStack.pop()

            return result
        }
    }
}

interface Reference<T> {
    val value: T
}

class ThisReference<T>(override val value: T) : Reference<T>

class SuperReference<T>(override val value: T) : Reference<T>

class ProxyReference<T>(override val value: T) : Reference<T>

class MirrorReference<T>(override val value: T) : Reference<T>