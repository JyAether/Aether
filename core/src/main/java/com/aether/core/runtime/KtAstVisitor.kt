package com.aether.core.runtime

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.lexer.KtSingleValueToken
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject

// 自定义访问者类，继承 KtTreeVisitorVoid
class MyKtVisitorV2 : KtVisitor<Map<String, Any?>, Void?>() {

    private val result = mutableMapOf<String, Any>()

    fun getResult(): Map<String, Any> {
        return result.toMap()
    }

    private fun visitNode(node: KtElement?, data: Void?): Map<String, Any?>? {
        if (node != null) {
            return node.accept(this, data)
        }
        return null
    }

    private fun visitNodeList(nodes: List<KtElement>, data: Void?): List<Map<String, Any?>> {
        val maps = mutableListOf<Map<String, Any?>>()
        for (node in nodes) {
            val res = visitNode(node, data)
            if (res != null) {
                maps.add(res)
            }
        }
        return maps
    }

    override fun visitKtElement(element: KtElement, data: Void?): Map<String, Any>? {
//        println("Visiting element: ${element.text}")
        super.visitElement(element)
        return mutableMapOf()
    }

    override fun visitElement(element: PsiElement) {
        super.visitElement(element)
    }

//    override fun visitSimpleNameExpression(
//        node: KtSimpleNameExpression,
//        data: Void?
//    ): Map<String, Any?> {
//        super.visitSimpleNameExpression(node, data)
//        println("Visiting Identifier: ${node.name}")
//        return mapOf(
//            "type" to "Identifier",
//            "name" to node.name,
//        )
//    }

    override fun visitKtFile(file: KtFile, data: Void?): Map<String, Any>? {
        println("Visiting file: ${file.name}")
//        result["file"] = file.name

        // 处理包声明
//        val packageDirective = file.packageDirective
//        if (packageDirective != null) {
//            println("Package: ${packageDirective.fqName.asString()}")
//            result["package"] = packageDirective.fqName.asString()
//        }

        // 处理导入语句
//        for (import in file.importDirectives) {
//            println("Import: ${import.importPath?.toString()}")
//            result["directives"] =
//                result.getOrDefault("imports", mutableListOf<String>()) as MutableList<String>
//            (result["directives"] as MutableList<String>).add(
//                import.importPath?.toString() ?: "Unknown"
//            )
//        }

        val declarations = file.declarations
        if (declarations != null) {
            val directives = file.importDirectives
            println("Import: ${directives?.toString()}")
            result["directives"] = visitNodeList(directives, data)


            println("declarations: ${declarations.asSequence()}")
            result["declarations"] = visitNodeList(declarations, data)
            result["type"] = "CompilationUnit"
        }
        file.acceptChildren(this)
        return result
    }

    override fun visitBlockExpression(
        node: KtBlockExpression, data: Void?
    ): Map<String, Any?> {
        super.visitBlockExpression(node, data)
        println("Visiting class: ${node.name}")
        return mapOf(
            "type" to "BlockStatement",
            "body" to visitNodeList(node.statements, data),
        )
    }

    override fun visitObjectDeclaration(
        node: KtObjectDeclaration, data: Void?
    ): Map<String, Any?> {
//       1 val annotations = node.annotationEntries
//        return annotations.any { it.shortName()?.asString() == "JvmStatic" }
//       2 val containingClass = function.containingClassOrObject
//        return containingClass is KtObjectDeclaration && containingClass.isCompanion()
//       3 function.parent is KtFile
        println("Visiting visitObjectDeclaration: ${node.name}")
        return mapOf(
            "type" to "ClassDeclaration",
            "name" to node.name,
            "members" to visitNodeList(node.declarations, data),
            "body" to visitNode(node.body, data),
        )
    }

    // 重写 visitClass 方法，处理类节点
    override fun visitClass(klass: KtClass, data: Void?): Map<String, Any?>? {
        println("Visiting class: ${klass.name}")
        super.visitClass(klass, data)
        return mapOf(
            "type" to "ClassDeclaration",
            "name" to klass.name,
            "members" to visitNodeList(klass.declarations, data),
            "body" to visitNode(klass.body, data),
        )
    }

    override fun visitCallableReferenceExpression(
        expression: KtCallableReferenceExpression,
        data: Void?
    ): Map<String, Any?> {
        super.visitCallableReferenceExpression(expression, data)
        return mapOf(
            "type" to "visitCallableReferenceExpression",
            "name" to expression.name,
            "targetExpression" to visitNode(expression.receiverExpression, data),//右边部分
        )
    }


    override fun visitLambdaExpression(
        expression: KtLambdaExpression,
        data: Void?
    ): Map<String, Any?> {
        super.visitLambdaExpression(expression, data)
        println("Visiting visitLambdaExpression: ${expression.name}")
        return mapOf(
            "type" to "LambdaExpression",
            "name" to expression.name,
            "parameters" to visitNodeList(expression.valueParameters, data),
            "body" to visitNode(expression.bodyExpression, data),
            "returnType" to visitNode(expression.functionLiteral, data),
        )
    }

    //    override fun visitFunctionDeclaration(node: KtNamedFunction, data: Void?): Map<String, Any?>? {
//        println("Visiting function: ${node.name}")
//        super.visitNamedFunction(node, data)
//        val parameters = visitNodeList(node.valueParameters, data)
//        return mapOf(
//            "type" to "FunctionDeclaration",
//            "name" to node.name,
//            "parameters" to parameters,
//            "typeParameters" to visitNodeList(node.typeParameters, data),
//            "body" to visitNode(node.bodyExpression, data),
//            "isStatic" to isStaticMethod(node),
//            "isGetter" to parameters.isEmpty(),
//            "isSetter" to (parameters.size == 1 && node.name!!.startsWith("set")),
//            "node" to node
//        )
//
//    }
    override fun visitNamedFunction(node: KtNamedFunction, data: Void?): Map<String, Any?>? {
        println("Visiting function: ${node.name}")
        super.visitNamedFunction(node, data)
        val parameters = visitNodeList(node.valueParameters, data)
        return mapOf(
            "type" to "MethodDeclaration",
            "name" to node.name,
            "parameters" to parameters,
            "typeParameters" to visitNodeList(node.typeParameters, data),
            "body" to visitNode(node.bodyExpression, data),
            "isStatic" to isStaticMethod(node),
            "isGetter" to parameters.isEmpty(),
            "isSetter" to (parameters.size == 1 && node.name!!.startsWith("set")),
//            "node" to node
        )

    }

    override fun visitValueArgumentList(node: KtValueArgumentList, data: Void?): Map<String, Any?> {
        println("visitValueArgumentList: ${node.name}")
        super.visitValueArgumentList(node, data)
        return mapOf(
            "type" to "ArgumentList",
            "arguments" to visitNodeList(node.arguments, data),
        )

    }

    override fun visitParameterList(node: KtParameterList, data: Void?): Map<String, Any?> {
        println("Visiting function: ${node.name}")
        super.visitParameterList(node, data)
        return mapOf(
            "type" to "ParameterList",
            "name" to node.name,
            "parameters" to visitNodeList(node.parameters, data),
        )
    }

    // 重写 visitProperty 方法，处理属性节点
    override fun visitProperty(property: KtProperty, data: Void?): Map<String, Any?>? {
        println("Visiting property: ${property.name}")
        super.visitProperty(property, data)
        return if (property.containingClassOrObject != null) {
            mapOf(
                "type" to "FieldDeclaration",
                "name" to property.name,
                "initializer" to visitNode(property.initializer, data),
            )
        } else {
            mapOf(
                "type" to "PropertyStatement",
                "name" to property.name,
                "initializer" to visitNode(property.initializer, data),
            )
        }
    }

    // 重写 visitBinaryExpression 方法，处理二元表达式
    override fun visitBinaryExpression(
        expression: KtBinaryExpression, data: Void?
    ): Map<String, Any?>? {
        println("Visiting binary expression: ${expression.text}")
        super.visitBinaryExpression(expression, data)
        return mapOf(
            "type" to "BinaryExpression",
            "name" to expression.name,
            "operator" to (expression.operationToken as KtSingleValueToken).value,
            "left" to visitNode(expression.left, data),
            "right" to visitNode(expression.right, data),
//            "node" to expression,
        )
    }

    override fun visitReferenceExpression(
        expression: KtReferenceExpression,
        data: Void?
    ): Map<String, Any?> {
        println("Visiting visitReferenceExpression: ${expression.text}")
        super.visitReferenceExpression(expression, data)//visitSimpleIdentifier
        return mapOf(
            "type" to "Identifier",
            "name" to expression.text,
        )
    }

    override fun visitSecondaryConstructor(
        constructor: KtSecondaryConstructor,
        data: Void?
    ): Map<String, Any?>? {
        super.visitSecondaryConstructor(constructor, null)
        println("Visiting visitSecondaryConstructor: ${constructor.name}")
        return mapOf(
            "type" to "SecondaryConstructor",
            "name" to constructor.name,
            "body" to visitNode(constructor.bodyExpression, data),
            "parameters" to visitNode(constructor.valueParameterList, data),
        )
    }

    override fun visitStringTemplateExpression(
        expression: KtStringTemplateExpression,
        data: Void?
    ): Map<String, Any?> {
        // 手动遍历子节点
        for (entry in expression.entries) {
            when (entry) {
                is KtLiteralStringTemplateEntry -> {
                    return mapOf(
                        "type" to "StringTemplateExpression",
                        "name" to entry.name,
                        "body" to visitNode(entry, data),
                    )
                }

                is KtSimpleNameStringTemplateEntry -> visitNode(entry, data)
//                is KtExpressionStringTemplateEntry -> visitExpressionStringTemplateEntry(entry)
                else -> entry.accept(this) // 对于未知类型，可以使用 accept 方法
            }
        }
        return super.visitStringTemplateExpression(expression, data)
    }

    override fun visitLiteralStringTemplateEntry(
        entry: KtLiteralStringTemplateEntry,
        data: Void?
    ): Map<String, Any?> {
        super.visitLiteralStringTemplateEntry(entry, data)
        return mapOf(
            "type" to "StringTemplateEntry",
            "body" to visitNode(entry.expression, data),
            "value" to entry.text
        )
    }

    override fun visitConstantExpression(
        expression: KtConstantExpression,
        data: Void?
    ): Map<String, Any?> {
        super.visitConstantExpression(expression, data)
        return mapOf(
            "type" to expression.elementType.debugName,
            "value" to expression.text,
            "name" to "ConstantExpression",
        )

    }

    override fun visitPrimaryConstructor(
        constructor: KtPrimaryConstructor,
        data: Void?
    ): Map<String, Any?>? {
        println("Visiting visitPrimaryConstructor: ${constructor.name}")
        super.visitPrimaryConstructor(constructor, null)
        return mapOf(
            "type" to "PrimaryConstructor",
            "name" to constructor.name,
            "body" to visitNode(constructor.bodyExpression, data),
            "parameters" to visitNode(constructor.valueParameterList, data),
        )
    }

    //
//    override fun visitTypeAlias(typeAlias: KtTypeAlias) {
//        println("Visiting visitTypeAlias: ${typeAlias.name}")
//        super.visitTypeAlias(typeAlias, null)
//    }
//
//    override fun visitDestructuringDeclaration(destructuringDeclaration: KtDestructuringDeclaration) {
//        println("Visiting visitDestructuringDeclaration: ${destructuringDeclaration.name}")
//        super.visitDestructuringDeclaration(destructuringDeclaration, null)
//    }
//
//    override fun visitDestructuringDeclarationEntry(multiDeclarationEntry: KtDestructuringDeclarationEntry) {
//        println("Visiting visitDestructuringDeclarationEntry: ${multiDeclarationEntry.name}")
//        super.visitDestructuringDeclarationEntry(multiDeclarationEntry, null)
//    }
//
//    override fun visitScript(script: KtScript) {
//        println("Visiting visitScript: ${script?.name}")
//        super.visitScript(script, null)
//    }
//
//    override fun visitImportAlias(importAlias: KtImportAlias) {
//        println("Visiting visitImportAlias: ${importAlias?.name}")
//        super.visitImportAlias(importAlias, null)
//    }
//
    override fun visitImportDirective(
        node: KtImportDirective,
        data: Void?
    ): Map<String, Any?>? {
        println("Visiting visitImportDirective: ${node?.name}")
        super.visitImportDirective(node, null)
//        node.importPath?.fqName?.asString()
        return mapOf(
            "type" to "ImportDirective",
            "importPath" to node.importPath?.pathStr,
            "alias" to node.alias?.text,
            "name" to node.importedFqName?.shortName()?.identifier,
        )
    }

    //    override fun visitImportList(importList: KtImportList, data: Void?): Map<String, Any?> {
//        super.visitImportList(importList, data)
//        return mapOf(
//            "type" to "ImportList",
//            "imports" to visitNodeList(importList.imports, data),
////            "prefix" to visitNode(node.pre,data),
////            "combinators" to visitNode(node.comb,data),
//        )
//
//    }
//
//    override fun visitModifierList(list: KtModifierList) {
//        println("Visiting visitModifierList: ${list?.name}")
//        super.visitModifierList(list, null)
//    }
//
//    override fun visitAnnotation(annotation: KtAnnotation) {
//        println("Visiting visitAnnotation: ${annotation?.name}")
//        super.visitAnnotation(annotation, null)
//    }
//
//    override fun visitAnnotationEntry(annotationEntry: KtAnnotationEntry) {
//        println("Visiting visitAnnotationEntry: ${annotationEntry?.name}")
//        super.visitAnnotationEntry(annotationEntry, null)
//    }
//
//    override fun visitConstructorCalleeExpression(constructorCalleeExpression: KtConstructorCalleeExpression) {
//        println("Visiting visitConstructorCalleeExpression: ${constructorCalleeExpression?.name}")
//        super.visitConstructorCalleeExpression(constructorCalleeExpression, null)
//    }
//
    override fun visitTypeParameterList(
        node: KtTypeParameterList, data: Void?
    ): Map<String, Any?>? {
        println("Visiting visitTypeParameterList: ${node?.name}")
        super.visitTypeParameterList(node, null)
        return mapOf(
            "type" to "TypeParameterList",
            "typeParameters" to visitNodeList(node.parameters, data),
        )
    }

    override fun visitTypeParameter(node: KtTypeParameter, data: Void?): Map<String, Any?>? {
        println("Visiting visitTypeParameter: ${node?.name}")
        super.visitTypeParameter(node, null)
        return mapOf(
            "type" to "TypeParameter",
            "name" to node.name,
            "bound" to visitNode(node.extendsBound, data),
        )
    }

    //
//    override fun visitEnumEntry(enumEntry: KtEnumEntry) {
//        println("Visiting visitEnumEntry: ${enumEntry?.name}")
//        super.visitEnumEntry(enumEntry, null)
//    }
//
    override fun visitParameter(parameter: KtParameter, data: Void?): Map<String, Any?>? {
        println("Visiting visitParameter: ${parameter?.name}")
        super.visitParameter(parameter, null)
        parameter
        return mapOf(
            "type" to "SimpleFormalParameter",
            "name" to parameter.name,
            "typeName" to parameter.typeReference?.text,
        )
    }

    //
//    override fun visitSuperTypeList(list: KtSuperTypeList) {
//        println("Visiting visitSuperTypeList: ${list?.name}")
//        super.visitSuperTypeList(list, null)
//    }
//
//    override fun visitSuperTypeListEntry(specifier: KtSuperTypeListEntry) {
//        println("Visiting visitSuperTypeList: ${specifier?.name}")
//        super.visitSuperTypeListEntry(specifier, null)
//    }
//
//    override fun visitDelegatedSuperTypeEntry(specifier: KtDelegatedSuperTypeEntry) {
//        println("Visiting visitDelegatedSuperTypeEntry: ${specifier?.name}")
//        super.visitDelegatedSuperTypeEntry(specifier, null)
//    }
//
//    override fun visitSuperTypeCallEntry(call: KtSuperTypeCallEntry) {
//        println("Visiting visitSuperTypeCallEntry: ${call?.name}")
//        super.visitSuperTypeCallEntry(call, null)
//    }
//
//    override fun visitSuperTypeEntry(specifier: KtSuperTypeEntry) {
//        super.visitSuperTypeEntry(specifier, null)
//    }
//
//    override fun visitContextReceiverList(contextReceiverList: KtContextReceiverList) {
//        super.visitContextReceiverList(contextReceiverList, null)
//    }
//
//    override fun visitConstructorDelegationCall(call: KtConstructorDelegationCall) {
//        super.visitConstructorDelegationCall(call, null)
//    }
//
//    override fun visitPropertyDelegate(delegate: KtPropertyDelegate) {
//        super.visitPropertyDelegate(delegate, null)
//    }
//
    override fun visitArgument(argument: KtValueArgument, data: Void?): Map<String, Any?>? {
        super.visitArgument(argument, null)
        return mapOf(
            "type" to "Argument",
            "body" to visitNode(argument.getArgumentExpression(), data),
        )
    }

    //
//    override fun visitLoopExpression(loopExpression: KtLoopExpression) {
//        super.visitLoopExpression(loopExpression, null)
//    }
//
    override fun visitLabeledExpression(
        node: KtLabeledExpression, data: Void?
    ): Map<String, Any?>? {
        super.visitLabeledExpression(node, null)
        return visitNode(node.labelQualifier, data)
    }

    //
//    override fun visitPrefixExpression(expression: KtPrefixExpression) {
//        super.visitPrefixExpression(expression, null)
//    }
//
//    override fun visitPostfixExpression(expression: KtPostfixExpression) {
//        super.visitPostfixExpression(expression, null)
//    }
//
//    override fun visitUnaryExpression(expression: KtUnaryExpression) {
//        super.visitUnaryExpression(expression, null)
//    }
//
    override fun visitReturnExpression(
        expression: KtReturnExpression, data: Void?
    ): Map<String, Any?>? {
        println("Visiting visitReturnExpression: ${expression?.name}")
        return mapOf(
            "type" to "ReturnStatement",
            "argument" to visitNode(expression.returnedExpression, null),
        )

    }

    //
//    override fun visitExpressionWithLabel(expression: KtExpressionWithLabel) {
//        super.visitExpressionWithLabel(expression, null)
//    }
//
    override fun visitThrowExpression(
        node: KtThrowExpression,
        data: Void?
    ): Map<String, Any?>? {
        super.visitThrowExpression(node, null)
        println("Visiting visitThrowExpression: ${node?.name}")
        return mapOf(
            "type" to "ThrowExpression",
            "expression" to visitNode(node.thrownExpression, data),
//            "node" to node,
        )
    }

    override fun visitBreakExpression(
        node: KtBreakExpression,
        data: Void?
    ): Map<String, Any?>? {
        super.visitBreakExpression(node, null)
        println("Visiting visitBreakExpression: ${node?.name}")
        return mapOf(
            "type" to "BreakStatement",
            "label" to visitNode(node.labelQualifier, data),
            "target" to visitNode(node.getTargetLabel(), data),
        )
    }

    //
//    override fun visitContinueExpression(expression: KtContinueExpression) {
//        super.visitContinueExpression(expression, null)
//    }
//
    override fun visitIfExpression(node: KtIfExpression, data: Void?): Map<String, Any?>? {
        super.visitIfExpression(node, null)
        println("Visiting visitIfExpression: ${node?.name}")
        return mapOf(
            "type" to "IfExpression",
            "condition" to visitNode(node.condition, data),
            "consequent" to visitNode(node.then, data),
            "alternate" to visitNode(node.`else`, data),
        )
    }


    override fun visitConstructorCalleeExpression(
        expression: KtConstructorCalleeExpression,
        data: Void?
    ): Map<String, Any?> {
        println("Visiting visitConstructorCalleeExpression: ${expression?.name}")
        return super.visitConstructorCalleeExpression(expression, data)
    }


    /**
     * visitCallExpression 方法在 Kotlin 的 AST（抽象语法树）访问器模式中用于处理函数或构造函数的调用。
     * 这意味着它不仅处理普通函数的调用，也包括对象实例化时构造函数的调用
     * KtConstructorCalleeExpression专门用于表示对构造函数的引用
     */
    override fun visitCallExpression(
        expression: KtCallExpression,
        data: Void?
    ): Map<String, Any?>? {
        super.visitCallExpression(expression, null)
        println("Visiting visitCallExpression: ${expression?.name}")
        val callee = expression.calleeExpression
        if (callee is KtConstructorCalleeExpression) {
            // 这是一个构造函数调用
            println("Detected constructor call: ${callee.text}")
        }
        //这里因为没有办法区分是构造方法还是普通方法，所以通过方法名称的大写暂时先这样判断，后续优化吧
        if (expression.calleeExpression!!.text[0].isUpperCase()) {
            return mapOf(
                "type" to "InstanceCreationExpression",
                "constructorName" to expression.calleeExpression?.text,
                "argumentList" to visitNode(expression.valueArgumentList, data),
                "valueArgument" to visitNodeList(expression.typeArguments, data),
            )
        } else {
            return mapOf(
                "type" to "CallExpression",
                "methodName" to visitNode(expression.calleeExpression, data),
                "target" to visitNode(expression.calleeExpression, data),
                "argumentList" to visitNode(expression.valueArgumentList, data),
                "valueArgument" to visitNodeList(expression.typeArguments, data),
            )
        }
    }

    override fun visitDotQualifiedExpression(
        expression: KtDotQualifiedExpression,
        data: Void?
    ): Map<String, Any?> {
        //对应visitPropertyAccess
        println("Visiting DotQualifiedExpression: ${expression.name}")
        super.visitDotQualifiedExpression(expression, data)
        return mapOf(
            "type" to "MethodInvocation",
            "name" to expression.name,
            "methodName" to visitNode(
                expression.selectorExpression,
                data
            ),//右边部分 ,要执行的方法test1，指令的部分比如比如DemoTest.test1.
            "target" to visitNode(expression.receiverExpression, data),//左边部分,指令的部分比如比如DemoTest
        )
//        return {
//            'type': 'PropertyAccess',
//            'id': _visitNode(node.propertyName),
//            'target': _visitNode(node.target),
//            'isCascaded': node.isCascaded,
//            'isNullAware': node.isNullAware,
//            'node': node,
//        };
    }


    override fun visitCatchSection(catchClause: KtCatchClause, data: Void?): Map<String, Any?>? {
        super.visitCatchSection(catchClause, null)
        println("Visiting visitCatchSection: ${catchClause?.name}")
        return mapOf(
            "type" to "CatchSection",
            "name" to catchClause.name,
        )
    }

    override fun visitTypeArgumentList(
        node: KtTypeArgumentList,
        data: Void?
    ): Map<String, Any?>? {
        super.visitTypeArgumentList(node, null)
        println("Visiting visitTypeArgumentList: ${node?.name}")
        return mapOf(
            "type" to "TypeArgumentList",
            "arguments" to visitNodeList(node.arguments, data),
        )
    }

    override fun visitThisExpression(
        node: KtThisExpression,
        data: Void?
    ): Map<String, Any?>? {
        super.visitThisExpression(node, null)
        println("Visiting visitThisExpression: ${node?.name}")
        return mapOf(
            "type" to "ThisExpression",
        )
    }

    override fun visitSuperExpression(
        node: KtSuperExpression,
        data: Void?
    ): Map<String, Any?>? {
        super.visitSuperExpression(node, null)
        println("Visiting visitSuperExpression: ${node?.name}")
        return mapOf(
            "type" to "SuperExpression",
        )
    }

    private fun isStaticMethod(function: KtNamedFunction): Boolean {
        // 1. 检查是否为顶层函数
        if (isTopLevelFunction(function)) {
            return true
        }

        if (isObjectMember(function)) {
            return true
        }

        // 2. 检查是否为伴生对象内的成员函数
        if (isCompanionObjectMember(function)) {
            return true
        }

        // 3. 检查是否使用了 @JvmStatic 注解
        if (hasJvmStaticAnnotation(function)) {
            return true
        }

        return false
    }

    private fun isTopLevelFunction(function: KtNamedFunction): Boolean {
        val parent = function.parent
        return parent is KtFile
    }

    private fun isObjectMember(function: KtNamedFunction): Boolean {
        val containingClass = function.containingClassOrObject
        return containingClass is KtObjectDeclaration
    }


    private fun isCompanionObjectMember(function: KtNamedFunction): Boolean {
        val containingClass = function.containingClassOrObject
        return containingClass is KtObjectDeclaration && containingClass.isCompanion()
    }

    private fun hasJvmStaticAnnotation(function: KtNamedFunction): Boolean {
        return function.annotationEntries.any { it.shortName?.asString() == "JvmStatic" }
    }
}