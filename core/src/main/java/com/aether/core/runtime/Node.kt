package com.aether.core.runtime


fun parseError(unit: Any): Nothing {
    throw Exception("Parse error: ${unit.toString()}")
}

abstract class Node(
    private val unit: Map<String, Any>
) {
    val type: String =
        unit["type"] as? String ?: throw IllegalArgumentException("Type not found in unit")
    var parent: Node? = null

    init {
        if (unit["type"] == null) {
            throw IllegalArgumentException("Type not found in unit")
        }
    }

    protected fun <T : Node> becomeParentOf(child: T?): T? {
        child?.parent = this
        return child
    }

    fun toUnit(): Map<String, Any> = unit

    // Assuming Expression is a subclass of Node and has a specific property or behavior
    // If Expression has a special way of setting its parent, you might need to adjust this method.
    // For now, we'll assume that all Nodes have the same parent-setting logic.
}

class Expression(
    expression: Node,
    isIdentifier: Boolean = false,
    isConstructorDeclaration: Boolean = false,
    isMethodInvocation: Boolean = false,
    isCallExpression: Boolean = false,
    isNamedExpression: Boolean = false,
    isMethodDeclaration: Boolean = false,
    isBlockStatement: Boolean = false,
    isEmptyFunctionBody: Boolean = false,
    isInstanceCreationExpression: Boolean = false,
    isClassDeclaration: Boolean = false,
    isPropertyAccess: Boolean = false,
    isIntegerLiteral: Boolean = false,
    isStringLiteral: Boolean = false,
    isStringTemplateEntry: Boolean = false,
    isStringTemplateExpression: Boolean = false,
    isBooleanLiteral: Boolean = false,
    isBinaryExpression: Boolean = false,
    isReturnStatement: Boolean = false,
    isVariableDeclarationList: Boolean = false,
    isFieldDeclaration: Boolean = false,
    isReferenceExpression: Boolean = false,
    unit: Map<String, Any>
) : Node(unit) {
    var expression: Node? = null;
    var isIdentifier: Boolean = false
    var isConstructorDeclaration: Boolean = false
    var isMethodInvocation: Boolean = false
    var isMethodDeclaration: Boolean = false
    var isCallExpression: Boolean = false
    var isNamedExpression: Boolean = false
    var isBlockStatement: Boolean = false
    var isEmptyFunctionBody: Boolean = false
    var isInstanceCreationExpression: Boolean = false
    var isClassDeclaration: Boolean = false
    var isPropertyAccess: Boolean = false
    var isIntegerLiteral: Boolean = false
    var isStringLiteral: Boolean = false
    var isStringTemplateEntry: Boolean = false
    var isStringTemplateExpression: Boolean = false
    var isBooleanLiteral: Boolean = false
    var isBinaryExpression: Boolean = false
    var isReturnStatement: Boolean = false
    var isVariableDeclarationList: Boolean = false
    var isFieldDeclaration: Boolean = false
    var isReferenceExpression: Boolean = false

    init {
        this.expression = expression
        this.isIdentifier = isIdentifier
        this.isConstructorDeclaration = isConstructorDeclaration
        this.isMethodInvocation = isMethodInvocation
        this.isMethodDeclaration = isMethodDeclaration
        this.isCallExpression = isCallExpression
        this.isNamedExpression = isNamedExpression
        this.isBlockStatement = isBlockStatement
        this.isEmptyFunctionBody = isEmptyFunctionBody
        this.isInstanceCreationExpression = isInstanceCreationExpression
        this.isClassDeclaration = isClassDeclaration
        this.isPropertyAccess = isPropertyAccess
        this.isIntegerLiteral = isIntegerLiteral
        this.isStringLiteral = isStringLiteral
        this.isStringTemplateEntry = isStringTemplateEntry
        this.isStringTemplateExpression = isStringTemplateExpression
        this.isBooleanLiteral = isBooleanLiteral
        this.isBinaryExpression = isBinaryExpression
        this.isReturnStatement = isReturnStatement
        this.isVariableDeclarationList = isVariableDeclarationList
        this.isFieldDeclaration = isFieldDeclaration
        this.isReferenceExpression = isReferenceExpression
    }

    fun get(): Node? {
        return expression
    }

    val asConstructorDeclaration: ConstructorDeclaration
        get() = expression as ConstructorDeclaration

    val asBlockStatement: BlockStatement
        get() = expression as BlockStatement

    val asInstanceCreationExpression: InstanceCreationExpression
        get() = expression as InstanceCreationExpression

    val asClassDeclaration: ClassDeclaration
        get() = expression as ClassDeclaration

    val asVariableDeclarationList: PropertyStatement
        get() = expression as PropertyStatement

    val asFieldDeclaration: FieldDeclaration
        get() = expression as FieldDeclaration

    val asPropertyAccess: PropertyAccess
        get() = expression as PropertyAccess

    val asReturnStatement: ReturnStatement
        get() = expression as ReturnStatement
    val asBinaryExpression: BinaryExpression
        get() = expression as BinaryExpression

    val asIntegerLiteral: IntegerLiteral
        get() = expression as IntegerLiteral

    val asStringLiteral: StringTemplateExpression
        get() = expression as StringTemplateExpression

    val asStringTemplateEntry: StringTemplateEntry
        get() = expression as StringTemplateEntry


    val asBooleanLiteral: BooleanLiteral
        get() = expression as BooleanLiteral

    val asStringTemplateExpression: StringTemplateExpression
        get() = expression as StringTemplateExpression

    val asIdentifier: Identifier
        get() = expression as Identifier

    val asMethodInvocation: MethodInvocation
        get() = expression as MethodInvocation

    val asCallExpression: CallExpression
        get() = expression as CallExpression

    val asMethodExpression: MethodDeclaration
        get() = expression as MethodDeclaration

    companion object {
        fun fromUnit(unit: Map<String, Any>): Expression {
            val type = unit["type"]
            if (type == "ClassDeclaration") {
                return Expression(
                    ClassDeclaration.fromUnit(unit),
                    isClassDeclaration = true,
                    unit = unit
                )
            } else if (type == "MethodDeclaration") {
                return Expression(
                    MethodDeclaration.fromUnit(unit), isMethodDeclaration = true, unit = unit
                );
            } else if (type == "BlockStatement") {
                return Expression(
                    BlockStatement.fromUnit(unit), isBlockStatement = true, unit = unit
                );
            } else if (type == "EmptyFunctionBody") {
                return Expression(
                    EmptyFunctionBody.fromUnit(unit), isEmptyFunctionBody = true, unit = unit
                );
            } else if (type == "InstanceCreationExpression") {
                return Expression(
                    InstanceCreationExpression.fromUnit(unit),
                    isInstanceCreationExpression = true,
                    unit = unit
                );
            } else if (type == "ReturnStatement") {
                return Expression(
                    ReturnStatement.fromUnit(unit), isReturnStatement = true, unit = unit
                );
            } else if (type == "PropertyAccess") {
                return Expression(
                    PropertyAccess.fromUnit(unit),
                    isPropertyAccess = true,
                    unit = unit
                )
            } else if (type == "FieldDeclaration") {
                return Expression(
                    FieldDeclaration.fromUnit(unit),
                    isFieldDeclaration = true,
                    unit = unit
                )
            } else if (type == "PropertyStatement") {
                return Expression(
                    PropertyStatement.fromUnit(unit),
                    isVariableDeclarationList = true,
                    unit = unit
                )
            } else if (type == "ConstantExpression") {
                return Expression(
                    ConstantExpression.fromUnit(unit), unit = unit
                )
            } else if (type == "INTEGER_CONSTANT") {
                return Expression(
                    IntegerLiteral.fromUnit(unit), isIntegerLiteral = true, unit = unit
                );
            } else if (type == "StringTemplateExpression") {
                return Expression(
                    StringTemplateExpression.fromUnit(unit),
                    isStringTemplateExpression = true,
                    unit = unit
                )
            } else if (type == "StringLiteral") {
                return Expression(
                    StringTemplateExpression.fromUnit(unit), isStringLiteral = true, unit = unit
                )
            } else if (type == "BOOLEAN_CONSTANT") {
                return Expression(
                    BooleanLiteral.fromUnit(unit), isBooleanLiteral = true, unit = unit
                )
            } else if (type == "BinaryExpression") {
                return Expression(
                    BinaryExpression.fromUnit(unit), isBinaryExpression = true, unit = unit
                )
            } else if (type == "Identifier") {
                return Expression(
                    Identifier.fromUnit(unit),
                    isReferenceExpression = true,
                    isIdentifier = true,
                    unit = unit
                )
            } else if (type == "ImportDirective") {
                return Expression(
                    ImportDirective.fromUnit(unit), isIdentifier = false, unit = unit
                )
            } else if (type == "CallExpression") {
                return Expression(
                    CallExpression.fromUnit(unit),
                    isCallExpression = true,
                    isIdentifier = false,
                    unit = unit
                )
            } else if (type == "MethodInvocation") {
                return Expression(
                    MethodInvocation.fromUnit(unit),
                    isMethodInvocation = true,
                    isIdentifier = false,
                    unit = unit
                )
            } else if (type == "StringTemplateEntry") {
                return Expression(
                    StringTemplateEntry.fromUnit(unit),
                    isStringTemplateEntry = true,
                    isMethodInvocation = false,
                    isIdentifier = false,
                    unit = unit
                )
            } else if (type == "SecondaryConstructor") {
                return Expression(
                    ConstructorDeclaration.fromUnit(unit),
                    isConstructorDeclaration = true,
                    unit = unit
                )
            }
            return Expression(
                ClassDeclaration.fromUnit(unit), unit = mapOf(
                    "type" to "e"
                )
            )
        }
    }

}

class BlockStatement(body: List<Expression>, unit: Map<String, Any>) : Node(unit) {
    var body: List<Expression>? = null

    init {
        this.body = body
    }

    companion object {
        fun fromUnit(unit: Map<String, Any>): BlockStatement {
            if (unit["type"] != "BlockStatement") {
                parseError(unit)
            }
            val body = mutableListOf<Expression>()
            if (unit["type"] == "BlockStatement") {
                if (unit["body"].isNotEmptyCollection()) {
                    val items = unit["body"] as List<Map<String, Any>>
                    for (item in items) {
                        body.add(Expression.fromUnit(item))
                    }
                }
            }
            return BlockStatement(body = body, unit)
        }
    }

}

class EmptyFunctionBody(unit: Map<String, Any>) : Node(unit) {

    companion object {
        fun fromUnit(unit: Map<String, Any>): EmptyFunctionBody {
            if (unit["type"] != "EmptyFunctionBody") {
                parseError(unit)
            }
            return EmptyFunctionBody(unit)
        }
    }

}

class PropertyAccess : Node {
    var name: String?
    var targetExpression: Expression?
    var selectorExpression: Expression?
    var isCascaded: Boolean
    var isNullAware: Boolean

    constructor(
        name: String?,
        targetExpression: Expression?,
        selectorExpression: Expression?,
        isCascaded: Boolean,
        isNullAware: Boolean,
        unit: Map<String, Any>
    ) : super(unit) {
        this.name = name
        this.targetExpression = targetExpression
        this.selectorExpression = selectorExpression
        this.isCascaded = isCascaded
        this.isNullAware = isNullAware
    }

    companion object {
        fun fromUnit(unit: Map<String, Any>): PropertyAccess {
            if (unit["type"] == "PropertyAccess") {
                val targetExpression =
                    unit["targetExpression"]?.let { Expression.fromUnit(it as Map<String, Any>) }
                val selectorExpression =
                    unit["selectorExpression"]?.let { Expression.fromUnit(it as Map<String, Any>) }
                return PropertyAccess(
                    ((unit["selectorExpression"] as Map<String, Any>)["target"] as Map<String, Any>)["name"] as String,
//                    unit["name"] as String?,
                    targetExpression,
                    selectorExpression,
                    unit["isCascaded"] as? Boolean ?: false,
                    unit["isNullAware"] as? Boolean ?: false,
                    unit
                )
            }
            parseError(unit)
        }
    }
}

class FieldDeclaration(
    name: String,
    isStatic: Boolean,
    target: Expression?,
    unit: Map<String, Any>
) : Node(unit) {
    var target: Expression? = null
    var name: String
    var isStatic: Boolean = false

    init {
        this.target = target
        this.name = name
        this.isStatic = isStatic
    }

    companion object {
        fun fromUnit(unit: Map<String, Any>): FieldDeclaration {
            if (unit["type"] != "FieldDeclaration") {
                return parseError(unit)
            }
            var target: Expression? = null
            if (unit["initializer"] != null) {
                target = Expression.fromUnit(unit["initializer"] as Map<String, Any>);
            }
            return FieldDeclaration(
                unit["name"] as String,
                false,
//                unit["isStatic"] as Boolean,
                target = target,
                unit = unit
            )
        }
    }
}

class PropertyStatement(
    name: String,
    isStatic: Boolean,
    target: Expression?,
    unit: Map<String, Any>
) : Node(unit) {
    var target: Expression? = null
    var name: String
    var isStatic: Boolean = false

    init {
        this.target = target
        this.name = name
        this.isStatic = isStatic
    }

    companion object {
        fun fromUnit(unit: Map<String, Any>): PropertyStatement {
            if (unit["type"] != "PropertyStatement") {
                parseError(unit)
            }
            var target: Expression? = null
            if (unit["type"] == "PropertyStatement") {
                if (unit["initializer"] != null) {
                    target = Expression.fromUnit(unit["initializer"] as Map<String, Any>);
                }
//                if (unit["parameters"].isNotEmptyMap()) {
//                    var argument: Expression? = null
//                    if (unit["argument"] != null) {
//                        argument = Expression.fromUnit(unit["parameters"] as Map<String, Any>);
//                    }
//                    return PropertyStatement(argument = argument, unit = unit)
//                }
            }
            return PropertyStatement(
                unit["name"] as String,
                false,
//                unit["isStatic"] as Boolean,
                target = target,
                unit = unit
            )
        }
    }
}

class StringLiteral(value: String?, unit: Map<String, Any>) : Node(unit) {
    var value: String? = null

    init {
        this.value = value
    }

    companion object {
        fun fromUnit(unit: Map<String, Any>): StringLiteral {
            if (unit["type"] != "StringLiteral") {
                parseError(unit)
            }
            if (unit["type"] == "StringLiteral") {
                return StringLiteral(value = (unit["value"].toString()), unit = unit)
            }
            return StringLiteral(value = null, unit = unit)
        }
    }
}

class StringTemplateEntry(value: String?, unit: Map<String, Any>) : Node(unit) {
    var value: String? = null

    init {
        this.value = value
    }

    companion object {
        fun fromUnit(unit: Map<String, Any>): StringTemplateEntry {
            if (unit["type"] != "StringTemplateEntry") {
                parseError(unit)
            }
            if (unit["type"] == "StringTemplateEntry") {
                return StringTemplateEntry(value = (unit["value"].toString()), unit = unit)
            }
            return StringTemplateEntry(value = null, unit = unit)
        }
    }
}

class ConstructorName(name: String, typeName: String?, unit: Map<String, Any>) : Node(unit) {
    val name: String = name
    val typeName: String? = typeName

    companion object {
        fun fromUnit(unit: Map<String, Any>): ConstructorName {
            return ConstructorName(
                name = unit["name"] as String,
                typeName = unit["typeName"] as String,
                unit = unit
            )
        }

        private fun parseError(unit: Map<String, Any?>): Nothing {
            throw IllegalArgumentException("Parse error on unit: $unit")
        }
    }
}

class InstanceCreationExpression(
    constructorName: ConstructorName?,
    argumentList: ArgumentList?,
    unit: Map<String, Any>
) : Node(unit) {
    var constructorName: ConstructorName? = null
    var argumentList: ArgumentList? = null

    init {
        this.constructorName = constructorName
        this.argumentList = argumentList
    }

    companion object {
        fun fromUnit(unit: Map<String, Any>): InstanceCreationExpression {
            if (unit["type"] != "InstanceCreationExpression") {
                return parseError(unit)
            }
            val name = unit["constructorName"] as String
            return InstanceCreationExpression(
                ConstructorName(name, name, unit),
                ArgumentList.fromUnit(unit["argumentList"] as Map<String, Any>),
                unit = unit
            )
        }
    }
}

class StringTemplateExpression(target: Expression?, unit: Map<String, Any>) : Node(unit) {
    var target: Expression? = null

    init {
        this.target = target
    }

    companion object {
        fun fromUnit(unit: Map<String, Any>): StringTemplateExpression {
            if (unit["type"] != "StringTemplateExpression") {
                parseError(unit)
            }
            if (unit["type"] == "StringTemplateExpression") {
                var target: Expression? = null
                if (unit["body"] != null) {
                    target = Expression.fromUnit(unit["body"] as Map<String, Any>)
                    return StringTemplateExpression(
                        target = target,
                        unit = unit["body"] as Map<String, Any>
                    )
                }
            }
            return StringTemplateExpression(target = null, unit = unit)
        }
    }
}

class BinaryExpression(
    operator: String, left: Expression, right: Expression, unit: Map<String, Any>
) : Node(unit) {
    ///运算符
    /// * +
    /// * -
    /// * *
    /// * /
    /// * <
    /// * >
    /// * <=
    /// * >=
    /// * ==
    /// * &&
    /// * ||
    /// * %
    /// * <<
    /// * |
    /// * &
    /// * >>
    ///
    val operator: String = operator

    ///左操作表达式
    val left: Expression = left

    ///右操作表达式
    val right: Expression = right

    companion object {
        fun fromUnit(unit: Map<String, Any>): BinaryExpression {
            if (unit["type"] != "BinaryExpression") {
                parseError(unit)
            }
            if (unit["type"] == "BinaryExpression") {
//                return BinaryExpression(unit = unit)
            }
            return BinaryExpression(
                unit["operator"] as String,
                Expression.fromUnit(unit["left"] as Map<String, Any>),
                Expression.fromUnit(unit["right"] as Map<String, Any>),
                unit = unit
            )
        }
    }
}

class Identifier : Node {
    constructor(name: String, unit: Map<String, Any>) : super(unit) {
        this.name = name
    }

    var name: String

    companion object {
        fun fromUnit(unit: Map<String, Any>): Identifier {
            if (unit["type"] == "Identifier") {
                return Identifier(
                    name = (unit["name"].toString()), unit = unit
                )
            }
            parseError(unit)
        }

        fun isPrivateName(name: String): Boolean {
            return false
        }
    }
}

class BooleanLiteral(value: Boolean, unit: Map<String, Any>) : Node(unit) {
    var value: Boolean = false

    init {
        this.value = value
    }

    companion object {
        fun fromUnit(unit: Map<String, Any>): BooleanLiteral {
            if (unit["type"] != "BOOLEAN_CONSTANT") {
                parseError(unit)
            }
            if (unit["type"] == "BOOLEAN_CONSTANT") {
                return BooleanLiteral(value = (unit["value"].toString().toBoolean()), unit = unit)
            }
            return BooleanLiteral(value = false, unit = unit)
        }
    }
}

class IntegerLiteral(value: Int, unit: Map<String, Any>) : Node(unit) {
    var value: Int? = 0

    init {
        this.value = value
    }

    companion object {
        fun fromUnit(unit: Map<String, Any>): IntegerLiteral {
            if (unit["type"] != "INTEGER_CONSTANT") {
                parseError(unit)
            }
            if (unit["type"] == "INTEGER_CONSTANT") {
                return IntegerLiteral(value = (unit["value"].toString().toInt()), unit = unit)
            }
            return IntegerLiteral(value = 0, unit = unit)
        }
    }
}

class ConstantExpression(unit: Map<String, Any>) : Node(unit) {
    companion object {
        fun fromUnit(unit: Map<String, Any>): ConstantExpression {
            if (unit["type"] != "ConstantExpression") {
                parseError(unit)
            }
            if (unit["type"] == "ConstantExpression") {
//                if (unit["parameters"].isNotEmptyMap()) {
//                    var argument: Expression? = null
//                    if (unit["argument"] != null) {
//                        argument = Expression.fromUnit(unit["parameters"] as Map<String, Any>);
//                    }
//                    return PropertyStatement(argument = argument, unit = unit)
//                }
            }
            return ConstantExpression(unit = unit)
        }
    }
}

class ReturnStatement(val argument: Expression?, unit: Map<String, Any>) : Node(unit) {
    companion object {
        fun fromUnit(unit: Map<String, Any>): ReturnStatement {
            if (unit["type"] != "ReturnStatement") {
                parseError(unit)
            }
            if (unit["type"] == "ReturnStatement") {
                var argument: Expression? = null
                if (unit["argument"] != null) {
                    argument = Expression.fromUnit(unit["argument"] as Map<String, Any>);
                }
                return ReturnStatement(argument = argument, unit = unit)
            }
            return ReturnStatement(null, unit = unit)
        }
    }

}

class ClassDeclaration(members: List<Expression>, name: String, unit: Map<String, Any>) :
    Node(unit) {
    var name: String = ""
    var members: List<Expression>? = null

    init {
        this.members = members
        this.name = name
    }

    companion object {
        fun fromUnit(unit: Map<String, Any>): ClassDeclaration {
            if (unit["type"] != "ClassDeclaration") {
                parseError(unit)
            }
            val members = mutableListOf<Expression>()
            if (unit["type"] == "ClassDeclaration") {
                if (unit["members"] is List<*>) {
                    val list = unit["members"] as List<Map<String, Any>>
                    for (member in list) {
                        members.add(Expression.fromUnit(member))
                    }
                }
            }
            return ClassDeclaration(members = members, name = (unit["name"] ?: "") as String, unit)
        }
    }

}

class MethodDeclaration : Node {

    var parameters: FormalParameterList?
    var name: String
    var isStatic: Boolean
    var isSetter: Boolean
    var bodyExpression: Expression
    var isAsync: Boolean

    constructor(
        parameters: FormalParameterList?,
        name: String,
        isStatic: Boolean,
        isSetter: Boolean,
        bodyExpression: Expression,
        isAsync: Boolean,
        unit: Map<String, Any>
    ) : super(unit) {
        this.name = name
        this.isStatic = isStatic
        this.isSetter = isSetter
        this.parameters = parameters
        this.bodyExpression = bodyExpression
        this.isAsync = isAsync
    }

    companion object {
        fun fromUnit(unit: Map<String, Any>): Node {
            if (unit["type"] != "MethodDeclaration") {
                parseError(unit)
            }
            var parameters: FormalParameterList? = null;
            if (unit["parameters"].isNotEmptyCollection()) {
                val any = unit["parameters"]
                val map = mutableMapOf<String, Any>()
                map["type"] = "ParameterList"
                map["parameters"] = unit["parameters"] as Any
                parameters = FormalParameterList.fromUnit(map);
            }
            return MethodDeclaration(
                parameters = parameters,
                unit["name"] as String,
                unit["isStatic"] as Boolean,
                unit["isSetter"] as Boolean,
                bodyExpression = Expression.fromUnit(unit["body"] as Map<String, Any>),
                isAsync = false,
                unit = unit
            )
        }
    }
}

class VariableDeclarator : Node {

    var name: String
    var typeName: String?
    var init: Expression?
    var isStatic: Boolean
    var isFinal: Boolean
    var isConst: Boolean
    var isLate: Boolean
    var isTopLevel: Boolean

    constructor(
        name: String,
        typeName: String?,
        init: Expression?,
        isStatic: Boolean,
        isFinal: Boolean,
        isConst: Boolean,
        isLate: Boolean,
        isTopLevel: Boolean,
        unit: Map<String, Any>
    ) : super(unit) {
        this.name = name
        this.typeName = typeName
        this.init = init
        this.isStatic = isStatic
        this.isFinal = isFinal
        this.isConst = isConst
        this.isLate = isLate
        this.isTopLevel = isTopLevel
    }

    companion object {
        fun fromUnit(unit: Map<String, Any>): VariableDeclarator {
            if (unit["type"] == "VariableDeclarator") {
                val init = unit["init"]?.let { Expression.fromUnit(it as Map<String, Any>) }
                val isStatic = unit["isStatic"] as? Boolean ?: false
                val isTopLevel = unit["isTopLevel"] as? Boolean ?: false
                return VariableDeclarator(
                    Identifier.fromUnit(unit["id"] as Map<String, Any>).name,
                    unit["typeName"] as? String,
                    init,
                    isStatic,
                    unit["isFinal"] as? Boolean ?: false,
                    unit["isConst"] as? Boolean ?: false,
                    unit["isLate"] as? Boolean ?: false,
                    isTopLevel,
                    unit
                )
            }
            return parseError(unit)
        }
    }
}

//class FunctionDeclaration(
//    name: String,
//    unit: Map<String, Any>
//) : Node(unit) {
//    var name: String = ""
//
//    init {
//        this.name = name
//    }
//
//    companion object {
//        fun fromUnit(unit: Map<String, Any>): Node {
//            if (unit["type"] != "FunctionDeclaration") {
//                parseError(unit)
//            }
//            return FunctionDeclaration(
//                unit["name"] as String,
//                unit = unit
//            )
//        }
//    }
//}

class FormalParameterList(
    val parameters: List<FormalParameter>,
    unit: Map<String, Any>
) : Node(unit) {

    init {
        parameters.forEach { becomeParentOf(it) }
    }

    companion object {
        fun fromUnit(unit: Map<String, Any>): FormalParameterList {
            if (unit["type"] == "ParameterList") {
                return FormalParameterList(
                    (unit["parameters"] as List<Map<String, Any>>)
                        .map { FormalParameter.fromUnit(it) },
                    unit
                )
            }
            parseError(unit)
        }
    }
}

abstract class FormalParameter(unit: Map<String, Any>) : Node(unit) {

    abstract val name: String
    abstract val typeName: String?
    abstract val isFinal: Boolean
    abstract val isNamed: Boolean
    abstract val isPositional: Boolean
    abstract val isOptional: Boolean
    abstract val isOptionalNamed: Boolean
    abstract val isOptionalPositional: Boolean
    abstract val isRequired: Boolean
    abstract val isRequiredNamed: Boolean
    abstract val isRequiredPositional: Boolean

    companion object {
        fun fromUnit(unit: Map<String, Any>): FormalParameter {
            return when (unit["type"]) {
                "SimpleFormalParameter" -> SimpleFormalParameter.fromUnit(unit)
                "DefaultFormalParameter" -> DefaultFormalParameter.fromUnit(unit)
//                "FieldFormalParameter" -> FieldFormalParameter.fromUnit(unit)
                else -> parseError(unit)
            }
        }
    }
}

class FieldFormalParameter(
    name: String,
    typeName: String?,
    isFinal: Boolean,
    isNamed: Boolean,
    isPositional: Boolean,
    isOptional: Boolean,
    isOptionalNamed: Boolean,
    isOptionalPositional: Boolean,
    isRequired: Boolean,
    isRequiredNamed: Boolean,
    isRequiredPositional: Boolean,
    val parameters: FormalParameterList?,
    unit: Map<String, Any>
) : SimpleFormalParameter(
    name,
    typeName,
    isFinal,
    isNamed,
    isPositional,
    isOptional,
    isOptionalNamed,
    isOptionalPositional,
    isRequired,
    isRequiredNamed,
    isRequiredPositional,
    unit
) {

    companion object {
        fun fromUnit(unit: Map<String, Any>): FieldFormalParameter {
            if (unit["type"] == "FieldFormalParameter") {
                val parameters =
                    unit["parameters"]?.let { FormalParameterList.fromUnit(it as Map<String, Any>) }
                val typeName =
                    unit["typeName"]?.let { TypeName.fromUnit(it as Map<String, Any>).name }
                return FieldFormalParameter(
                    unit["name"] as String,
                    typeName,
                    unit["isFinal"] as Boolean,
                    unit["isNamed"] as Boolean,
                    unit["isPositional"] as Boolean,
                    unit["isOptional"] as Boolean,
                    unit["isOptionalNamed"] as Boolean,
                    unit["isOptionalPositional"] as Boolean,
                    unit["isRequired"] as Boolean,
                    unit["isRequiredNamed"] as Boolean,
                    unit["isRequiredPositional"] as Boolean,
                    parameters,
                    unit
                )
            }
            parseError(unit)
        }
    }
}

abstract class NormalFormalParameter(unit: Map<String, Any>) : FormalParameter(unit)


open class SimpleFormalParameter(
    val _name: String,
    val _typeName: String?,
    val _isFinal: Boolean,
    val _isNamed: Boolean,
    val _isPositional: Boolean,
    val _isOptional: Boolean,
    val _isOptionalNamed: Boolean,
    val _isOptionalPositional: Boolean,
    val _isRequired: Boolean,
    val _isRequiredNamed: Boolean,
    val _isRequiredPositional: Boolean,
    unit: Map<String, Any>
) : NormalFormalParameter(unit) {

    companion object {
        fun fromUnit(unit: Map<String, Any>): SimpleFormalParameter {
            if (unit["type"] == "SimpleFormalParameter") {
                val typeName = unit["typeName"]
//                val typeName = unit["typeName"]?.let { TypeName.fromUnit(it as Map<String, Any>).name }
                return SimpleFormalParameter(
                    unit["name"] as String,
                    (typeName ?: "").toString(),
                    unit["isFinal"] as? Boolean ?: false,
                    unit["isNamed"] as? Boolean ?: false,
                    unit["isPositional"] as? Boolean ?: false,
                    unit["isOptional"] as? Boolean ?: false,
                    unit["isOptionalNamed"] as? Boolean ?: false,
                    unit["isOptionalPositional"] as? Boolean ?: false,
                    unit["isRequired"] as? Boolean ?: false,
                    unit["isRequiredNamed"] as? Boolean ?: false,
                    unit["isRequiredPositional"] as? Boolean ?: false,
                    unit
                )
            }
            parseError(unit)
        }
    }

    override val isFinal: Boolean
        get() = _isFinal

    override val isNamed: Boolean
        get() = _isNamed

    override val isOptional: Boolean
        get() = _isOptional

    override val isOptionalNamed: Boolean
        get() = _isOptionalNamed

    override val isOptionalPositional: Boolean
        get() = _isOptionalPositional

    override val isPositional: Boolean
        get() = _isPositional

    override val isRequired: Boolean
        get() = _isRequired

    override val isRequiredNamed: Boolean
        get() = _isRequiredNamed

    override val isRequiredPositional: Boolean
        get() = _isRequiredPositional

    override val name: String
        get() = _name

    override val typeName: String?
        get() = _typeName
}

class TypeName(
    name: String,
    typeArguments: TypeArgumentList?,
    unit: Map<String, Any>
) : NamedType(name, typeArguments, unit) {

    companion object {
        fun fromUnit(unit: Map<String, Any>): TypeName {
            if (unit["type"] == "TypeName") {
                val typeArguments =
                    unit["typeArguments"]?.let { TypeArgumentList.fromUnit(it as Map<String, Any>) }
                return TypeName(
                    unit["name"] as String,
                    typeArguments,
                    unit
                )
            }
            parseError(unit)
        }
    }
}

class TypeArgumentList : Node {

    val arguments: List<TypeAnnotation>

    constructor(arguments: List<TypeAnnotation>, unit: Map<String, Any>) : super(unit) {
        this.arguments = arguments
    }


    companion object {
        fun fromUnit(unit: Map<String, Any>): TypeArgumentList {
            if (unit["type"] == "TypeArgumentList") {
                val arguments = unit["arguments"]?.let {
                    if (it is List<*>) {
                        it.mapNotNull { argument ->
                            if (argument is Map<*, *>) {
                                TypeAnnotation.fromUnit(argument as Map<String, Any>)
                            } else {
                                null
                            }
                        }
                    } else {
                        emptyList()
                    }
                } ?: emptyList()
                return TypeArgumentList(arguments, unit)
            }

            parseError(unit)
        }
    }
}

abstract class NamedType(
    val name: String,
    val typeArguments: TypeArgumentList?,
    unit: Map<String, Any>
) : TypeAnnotation(unit) {

    init {
        becomeParentOf(typeArguments)
    }
}

abstract class TypeAnnotation(unit: Map<String, Any>) : Node(unit) {

    companion object {
        fun fromUnit(unit: Map<String, Any>): TypeAnnotation {
            val type = unit["type"]
            return when (type) {
                "TypeName" -> TypeName.fromUnit(unit)
//                "GenericFunctionType" -> GenericFunctionType.fromUnit(unit)
                else -> parseError(unit)
            }
        }
    }
}


class DefaultFormalParameter(
    val parameter: FormalParameter,
    val defaultValue: Expression?,
    unit: Map<String, Any>
) : FormalParameter(unit) {

    init {
        becomeParentOf(parameter)
        becomeParentOf(defaultValue)
    }

    companion object {
        fun fromUnit(unit: Map<String, Any>): DefaultFormalParameter {
            if (unit["type"] == "DefaultFormalParameter") {
                val defaultValue =
                    unit["defaultValue"]?.let { Expression.fromUnit(it as Map<String, Any>) }
                return DefaultFormalParameter(
                    FormalParameter.fromUnit(unit["parameter"] as Map<String, Any>),
                    defaultValue,
                    unit
                )
            }
            parseError(unit)
        }
    }

    override val isFinal: Boolean
        get() = parameter.isFinal

    override val isNamed: Boolean
        get() = parameter.isNamed

    override val isOptional: Boolean
        get() = parameter.isOptional

    override val isOptionalNamed: Boolean
        get() = parameter.isOptionalNamed

    override val isOptionalPositional: Boolean
        get() = parameter.isOptionalPositional

    override val isPositional: Boolean
        get() = parameter.isPositional

    override val isRequired: Boolean
        get() = parameter.isRequired

    override val isRequiredNamed: Boolean
        get() = parameter.isRequiredNamed

    override val isRequiredPositional: Boolean
        get() = parameter.isRequiredPositional

    override val name: String
        get() = parameter.name

    override val typeName: String?
        get() = parameter.typeName
}

class CompilationUnit(unit: Map<String, Any>) : Node(unit) {
    var declarations: List<Expression>? = null
    var directives: List<Directive>? = null

    //
    constructor(
        declarations: List<Expression>, directives: List<Directive>, unit: Map<String, Any>
    ) : this(unit) {
        this.declarations = declarations
        this.directives = directives
    }

    // 从单元数据创建CompilationUnit实例的工厂方法
    companion object {
        fun fromUnit(unit: Map<String, Any>): CompilationUnit {
            if (unit["type"] != "CompilationUnit") {
                parseError(unit)
            }
            val declarations = unit["declarations"]?.let { it as? List<Map<String, Any>> }
                ?.map { Expression.fromUnit(it) } ?: emptyList()

            val directives = unit["directives"]?.let { it as? List<Map<String, Any>> }
                ?.map { Directive.fromUnit(it) } ?: emptyList()

            return CompilationUnit(declarations, directives, unit)
        }
    }
}

class ImportDirective : NamespaceDirective {
    var refix: String?

    var name: String

    private constructor(uri: String, prefix: String?, name: String, unit: Map<String, Any>) : super(
        uri,
        unit
    ) {
        this.refix = prefix;
        this.name = name
    }

    companion object {
        fun fromUnit(unit: Map<String, Any>): ImportDirective {
            if (unit["type"] == "ImportDirective") {
//                val combinators = (unit["combinators"] as List<Map<String, Any>>).map { Combinator.fromUnit(it as Map<String, Any>) }
                val prefix = unit["alias"]
                return ImportDirective(
                    unit["importPath"] as String,
                    prefix as String?,
                    unit["name"] as String,
                    unit
                )
            }
            parseError(unit)
        }
    }
}

class CallExpression : Node {
    val methodName: String
    val target: Expression?
    val argumentList: ArgumentList
    val isCascaded: Boolean
    val isNullAware: Boolean

    private constructor(
        methodName: String,
        target: Expression?,
        argumentList: ArgumentList,
        isCascaded: Boolean,
        isNullAware: Boolean,
        unit: Map<String, Any>
    ) : super(unit) {
        this.methodName = methodName
        this.target = target
        this.argumentList = argumentList
        this.isCascaded = isCascaded
        this.isNullAware = isNullAware
        becomeParentOf(target)
        becomeParentOf(argumentList)
    }

    companion object {
        fun fromUnit(unit: Map<String, Any>): CallExpression {
            if (unit["type"] == "CallExpression") {
                val target = unit["target"]?.let { Expression.fromUnit(it as Map<String, Any>) }
                return CallExpression(
                    Identifier.fromUnit(unit["methodName"] as Map<String, Any>).name,
                    target,
                    ArgumentList.fromUnit(unit["argumentList"] as Map<String, Any>),
                    unit["isCascaded"] as? Boolean ?: false,
                    unit["isNullAware"] as? Boolean ?: false,
                    unit
                )
            }
            parseError(unit)
        }
    }
}


class MethodInvocation : Node {
    val methodName: String
    var isStatic: Boolean = true
    var isSetter: Boolean = false
    val target: Expression?
    val argumentList: ArgumentList?
    val isCascaded: Boolean
    val isNullAware: Boolean

    private constructor(
        methodName: String,
        target: Expression?,
        argumentList: ArgumentList?,
        isCascaded: Boolean,
        isNullAware: Boolean,
        unit: Map<String, Any>
    ) : super(unit) {
        this.methodName = methodName
        this.target = target
        this.argumentList = argumentList
        this.isCascaded = isCascaded
        this.isNullAware = isNullAware
        becomeParentOf(target)
        becomeParentOf(argumentList)
    }

    companion object {
        fun fromUnit(unit: Map<String, Any>): MethodInvocation {
            if (unit["type"] == "MethodInvocation") {
                val target = unit["target"]?.let { Expression.fromUnit(it as Map<String, Any>) }
                return MethodInvocation(
                    if ((unit["methodName"] as Map<String, Any>)["target"] == null) "" else ((unit["methodName"] as Map<String, Any>)["target"] as Map<String, Any>)["name"] as String,
                    target,
                    if ((unit["methodName"] as Map<String, Any>)["argumentList"] ==null) null else ArgumentList.fromUnit((unit["methodName"] as Map<String, Any>)["argumentList"] as Map<String, Any>),
                    unit["isCascaded"] as? Boolean ?: false,
                    unit["isNullAware"] as? Boolean ?: false,
                    unit
                )
            }
            parseError(unit)
        }
    }
}

class ArgumentList : Node {
    val arguments: List<Expression>?

    private constructor(arguments: List<Expression>?, unit: Map<String, Any>) : super(unit) {
        this.arguments = arguments
        arguments?.forEach { becomeParentOf(it) }
    }

    companion object {
        fun fromUnit(unit: Map<String, Any>): ArgumentList {
            if (unit["type"] == "ArgumentList") {
                val arguments =
                    (unit["arguments"] as List<Map<String, Any>>).map { Expression.fromUnit(it.get("body") as Map<String, Any>) }
                return ArgumentList(arguments, unit)
            }
            parseError(unit)
        }
    }
}


abstract class Combinator(unit: Map<String, Any>) : Node(unit) {

    companion object {
        fun fromUnit(unit: Map<String, Any>): Combinator {
            val type = unit["type"]
            return when (type) {
                "HideCombinator" -> HideCombinator.fromUnit(unit)
                "ShowCombinator" -> ShowCombinator.fromUnit(unit)
                else -> parseError(unit)
            }
        }
    }
}

class ShowCombinator : Combinator {
    var shownNames: List<Identifier>

    private constructor(shownNames: List<Identifier>, unit: Map<String, Any>) : super(unit) {
        this.shownNames = shownNames
        shownNames.forEach { becomeParentOf(it) }
    }

    companion object {
        fun fromUnit(unit: Map<String, Any>): ShowCombinator {
            if (unit["type"] == "ShowCombinator") {
                val shownNames =
                    (unit["shownNames"] as List<Map<String, Any>>).map { Identifier.fromUnit(it as Map<String, Any>) }
                return ShowCombinator(shownNames, unit)
            }
            parseError(unit)
        }
    }
}

class HideCombinator : Combinator {
    var hiddenNames: List<Identifier>

    constructor(hiddenNames: List<Identifier>, unit: Map<String, Any>) : super(unit) {
        this.hiddenNames = hiddenNames
        hiddenNames.forEach { becomeParentOf(it) }
    }

    companion object {
        fun fromUnit(unit: Map<String, Any>): HideCombinator {
            if (unit["type"] == "HideCombinator") {
                val hiddenNames =
                    (unit["hiddenNames"] as List<Map<String, Any>>).map { Identifier.fromUnit(it as Map<String, Any>) }
                return HideCombinator(hiddenNames, unit)
            }
            parseError(unit)
        }
    }
}


abstract class NamespaceDirective : UriBasedDirective {
    constructor(
        uri: String,
        unit: Map<String, Any>
    ) : super(uri, unit) {
        this.uri = uri
    }
}

abstract class UriBasedDirective : Directive {
    var uri: String

    constructor(
        uri: String,
        unit: Map<String, Any>
    ) : super(unit) {
        this.uri = uri
    }
}

abstract class Directive(unit: Map<String, Any>) : Node(unit) {

    companion object {
        fun fromUnit(unit: Map<String, Any>): Directive {
            val type = unit["type"]
            return when (type) {
                "ImportDirective" -> ImportDirective.fromUnit(unit)
//                "PartDirective" -> PartDirective.fromUnit(unit)
//                "PartOfDirective" -> PartOfDirective.fromUnit(unit)
//                "ExportDirective" -> ExportDirective.fromUnit(unit)
                else -> parseError(unit)
            }
        }
    }
}


fun Any?.isNotEmptyCollection(): Boolean =
    this != null && this is Collection<*> && this.isNotEmpty()

fun Any?.isNotEmptyMap(): Boolean = this != null && this is Map<*, *> && this.isNotEmpty()