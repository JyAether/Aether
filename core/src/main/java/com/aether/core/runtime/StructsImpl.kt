package com.aether.core.runtime

class StructsImpl {
}

abstract class _VariableBase(
    val _name: String,
    val _typeName: String?,
    val _isPrivate: Boolean,
    val _isTopLevel: Boolean,
    val _isStatic: Boolean,
    val _isFinal: Boolean,
    val _isConst: Boolean,
    override val programNode: AstRuntime.ProgramNode? = null
) : AstVariable() {

    override val simpleName: String
        get() = _name

    override val qualifiedName: String
        get() = "${owner.qualifiedName}.$simpleName"

    override val isPrivate: Boolean
        get() = _isPrivate

    override val isTopLevel: Boolean
        get() = _isTopLevel

    override val isStatic: Boolean
        get() = _isStatic

    override val isFinal: Boolean
        get() = _isFinal

    override val isConst: Boolean
        get() = _isConst

    override val owner: AstDeclaration
        get() = throw UnimplementedError("owner error")

    override val type: AstType
        get() = throw UnimplementedError("type error:${type}")

    val typeName: String?
        get() = _typeName
}

class VariableImpl private constructor(
    name: String,
    typeName: String?,
    private val _initializer: Expression?,
    isPrivate: Boolean,
    isTopLevel: Boolean,
    isStatic: Boolean,
    isConst: Boolean,
    isFinal: Boolean,
    programNode: AstRuntime.ProgramNode? = null
) : _VariableBase(name, typeName, isPrivate, isTopLevel, isStatic, isFinal, isConst, programNode) {

    companion object {
        fun fromDeclarator(declarator: VariableDeclarator): VariableImpl {
            return VariableImpl(
                declarator.name,
                declarator.typeName,
                declarator.init,
                Identifier.isPrivateName(declarator.name),
                declarator.isTopLevel,
                declarator.isStatic,
                declarator.isConst,
                declarator.isFinal
            )
        }
    }

    override val owner: AstDeclaration
        get() = throw UnimplementedError("owner error")

    override val type: AstType
        get() = throw UnimplementedError("type error:${type}")

    val initializer: Expression?
        get() = _initializer
}

class _ParameterImpl private constructor(
    name: String,
    typeName: String?,
    isPrivate: Boolean,
    isTopLevel: Boolean,
    isStatic: Boolean,
    isConst: Boolean,
    isFinal: Boolean,
    private val _isNamed: Boolean,
    private val _isOptional: Boolean,
    private val _isField: Boolean,
    private val _defaultValue: Expression?
) : _VariableBase(name, typeName, isPrivate, isTopLevel, isStatic, isFinal, isConst){

    companion object {
        fun fromParameter(parameter: FormalParameter): _ParameterImpl {
            val defaultValue: Expression? = if (parameter is DefaultFormalParameter) {
                parameter.defaultValue
            } else {
                null
            }
            val isField = parameter is FieldFormalParameter
            return _ParameterImpl(
                parameter.name,
                parameter.typeName,
                Identifier.isPrivateName(parameter.name),
                false,
                false,
                false,
                parameter.isFinal,
                parameter.isNamed,
                parameter.isOptional,
                isField,
                defaultValue
            )
        }
    }

    val defaultValue: Expression?
        get() = _defaultValue

    val hasDefaultValue: Boolean
        get() = _defaultValue != null

    val isNamed: Boolean
        get() = _isNamed

    val isField: Boolean
        get() = _isField

    val isOptional: Boolean
        get() = _isOptional

    override val owner: AstDeclaration
        get() = throw UnimplementedError("owner error")

    override val type: AstType
        get() = throw UnimplementedError("type error:${type}")
}
