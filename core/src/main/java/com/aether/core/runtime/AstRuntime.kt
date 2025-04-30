package com.aether.core.runtime

import android.text.TextUtils
import com.aether.core.runtime.AppContextManager.context
import com.aether.core.runtime.deliver.ComposableHolder
import com.aether.core.runtime.deliver.invokeRunMain
import com.aether.core.runtime.proxy.ProxyBinding
import com.aether.core.runtime.reflectable.ComposeComponentDescriptor
import com.aether.core.runtime.reflectable.ComposeComponentFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.psi.PsiFileFactory
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFile
import java.io.File
import java.lang.reflect.Modifier
import java.util.Collections
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.jvm.javaMethod


class Import(
    val uri: String,
    val prefix: String?,
    val combinators: List<Combinator>?
) {
    override fun toString(): String {
        return "Import(uri: $uri, prefix: $prefix, combinators: $combinators)"
    }
}

fun _exeAndRecMethodInvocation(methodInvocation: MethodInvocation): Any? {
    val programStack = context.get<ProgramStack>(ProgramStack::class.java)
    val scope = StackScope("Call method: ") // 创建 StackScope 实例
    scope["node"] = methodInvocation // 将 methodInvocation 存入 scope
    programStack?.push(scope = scope) // 将 scope 压入程序栈
    val result = _executeMethodInvocation2(methodInvocation) // 执行方法调用逻辑
    programStack?.pop() // 弹出栈顶
    return result // 返回结果
}

fun _executeMethodInvocation2(methodInvocation: MethodInvocation): Any? {
    val runtime = context.get<AstRuntime>(AstRuntime::class.java)!! // 获取 AstRuntime，使用 !! 表示非空断言
    val programStack = context.get<ProgramStack>(ProgramStack::class.java)!! // 获取 ProgramStack
    val methodName = methodInvocation.methodName

    val positionalArguments = mutableListOf<Any?>() // 位置参数列表
    val namedArguments = mutableMapOf<String, Any?>() // 命名参数 Map

    // 处理方法的参数
    if (!(methodInvocation.argumentList?.arguments == null || methodInvocation.argumentList.arguments.isEmpty())) {
        for (argument in methodInvocation.argumentList!!.arguments!!) {
            if (argument.isNamedExpression) {
                namedArguments.putAll(executeExpression(argument) as Map<String, Any?>)
            } else {
                val arg = executeExpression(argument)
                if (arg != null) {
                    positionalArguments.add(
                        arg
                    )
                }
            }
        }
    }
    // 检查是否有 ComposableHolder 上下文
    val holder = context.get(ComposableHolder::class.java)
    if (holder != null) {
        if (methodName == "build" && methodInvocation.argumentList?.arguments?.isNotEmpty() == true) {
            // 确保有参数
//            val arg = methodInvocation.argumentList?.arguments?.get(0)
//            if (arg != null && arg.isFunctionExpression) {
//                return doRunBuild(arg.asFunctionExpression())
//            }
        } else if (methodName == "runApp" && methodInvocation.argumentList?.arguments?.isNotEmpty() == true) {
//            val arg = methodInvocation.argumentList?.arguments?.get(0)
//            if (arg != null) {
//                return invokeRunApp(arg)
//            }
        }
    }

    var target: Any? = null
    if (methodInvocation.isCascaded) {
//        target = findCascadeAncestorTargetValue(methodInvocation)
    } else if (methodInvocation.target != null) {
        target = executeExpression(methodInvocation.target)
    }
//
//    if (target == null) {
//        if (methodInvocation.isNullAware) {
//            return null
//        }
//        if (methodInvocation.target != null) {
//            throw RuntimeException("Null check operator used on a null value -> $methodName")
//        }
//    }

    // 如果没有目标对象
    if (target == null) {
        // 查找实例 AST 方法或静态 AST 方法
        val method = programStack.get<AstMethod>(methodName)
        if (method != null) {
            return AstMethod.apply2(method, positionalArguments, namedArguments)
        }

        // 默认 AST 构造函数
        val clazz = AstClass.forName(methodName)
        if (clazz != null) {
            return clazz.newInstance("", positionalArguments, namedArguments)
        }
    } else {
        methodInvocation.methodName
        // 如果目标是 AstClass
        if (target is AstClass) {
            if (target.hasConstructor(methodName)) {
                return target.newInstance(methodName, positionalArguments, namedArguments)
            } else {
                return target.invoke(methodName, positionalArguments, namedArguments)
            }
        }

        // 如果目标是 AstInstance
        val instance = AstInstance.forObject(target)
        if (instance != null) {
            return instance.invoke(methodName, positionalArguments, namedArguments)
        }

        throw RuntimeException("InstanceMirror for ${target::class} not found")
    }

    // 检查是否有顶层函数
//    if (runtime.hasTopLevelFunction(methodName)) {
//        return runtime.invoke(methodName, positionalArguments, namedArguments)
//    }
//
//    // 尝试通过反射调用顶层方法
//    val libraryMirror = ReflectionBinding.instance.reflectTopLevelInvoke(methodName)
//    if (libraryMirror != null) {
//        val function = AstMethod.fromMirror(libraryMirror.declarations[methodName] as MethodMirror)
//        processArguments(function.parameters, positionalArguments, namedArguments)
//        return libraryMirror.invoke(methodName, positionalArguments, namedArguments)
//    }

    throw RuntimeException("Error: MethodInvocation -> $methodName, target: $target, runtimeType: ${target?.toString()}")
}

fun executeMethodTarget(
    methodInvocation: MethodInvocation,
    methodName: String,
    positionalArguments: List<Any?>,
    namedArguments: Map<String, Any?>? = null
): Any? {
    if (methodInvocation?.target != null) {
        var target = executeExpression(methodInvocation?.target);
        if (target is AstClass) {
            if (target.hasConstructor(methodName)) {
                return target.newInstance(methodName, positionalArguments, namedArguments)
            } else {
                return target.invoke(methodName, positionalArguments, namedArguments)
            }
        }
        val instance = AstInstance.forObject(target)
        if (instance != null) {
            return instance.invokeGetter(methodInvocation.methodName)
        }
    }
    return null
}

fun executeInstanceCreationExpression(instanceCreationExpression: InstanceCreationExpression): Any? {
    val positionalArguments = mutableListOf<Any?>()
    val namedArguments = mutableMapOf<String, Any?>()

    if (instanceCreationExpression.argumentList?.arguments?.isNotEmpty() == true) {
        for (arg in instanceCreationExpression!!.argumentList!!.arguments!!) {
//            if (arg.isNamedExpression()) {
//                namedArguments.putAll(executeExpression(arg))
//            } else {
            positionalArguments.add(executeExpression(arg))
//            }
        }
    }

    val constructorName = instanceCreationExpression.constructorName?.name
    val typeName = instanceCreationExpression.constructorName?.typeName
    if (typeName == null || constructorName == null) {
        println("Type $typeName is ${typeName == null} not found, constructorName is ${constructorName == null} not found")
        null
    }
    //TODO 这里依然有问题，需要
    // 对于Compose组件，返回一个包含路径和参数的描述对象
    // 而不是直接返回@Composable函数
    val runtime = context.get<AstRuntime>(AstRuntime::class.java)
    val importDirective = runtime?.getReflectClass(typeName?:"")
    if (ComposeComponentFactory.isComponentAvailable(importDirective?.uri?:"")) {
        return ComposeComponentDescriptor(importDirective?.uri?:"", instanceCreationExpression.argumentList?.arguments)
    }

    val astClass: AstClass? = AstClass.forName(typeName ?: "")
    return if (astClass != null) {
        astClass.newInstance(constructorName!!, positionalArguments, namedArguments)
    } else {
        println("Type $typeName not found")
        null
    }
}

fun executeExpression(
    expression: Expression, flag: String? = null, keepVariable: Boolean = false
): Any? {
    return try {
        val programStack = context.get<ProgramStack>(ProgramStack::class.java)
        if (expression.isIdentifier) {
            val name = expression.asIdentifier.name
            val variable = programStack?.getVariable(name)
            if (variable != null) {
                if (keepVariable) {
                    return variable
                } else {
                    if (variable.value is Expression) {
                        return executeExpression(variable.value as Expression)
                    }
                    return variable.value
                }
            }

            val runtime = context.get<AstRuntime>(AstRuntime::class.java)!!
            val method = programStack?.get<AstMethod>(name)
            if (method != null) {
                if (method.isGetter) {
                    return AstMethod.apply2(
                        method,
                        positionalArguments = mutableListOf(),
                        namedArguments = mutableMapOf()
                    )
                }
                val length = method.parameters.size
                val thisReference = context.get(ThisReference::class.java)
                val superReference = context.get(SuperReference::class.java)
                val innerStack = ProgramStack("closure", programStack)
                val overrides = mapOf(
                    ProgramStack::class.java to { innerStack },
                    AstRuntime::class.java to { runtime },
                    ThisReference::class.java to { thisReference },
                    SuperReference::class.java to { superReference })
                return when (length) {
                    0 -> {
                        {
                            context.run(
                                name = "closure override", body = {
                                    AstMethod.apply2(
                                        method,
                                        positionalArguments = mutableListOf(),
                                        namedArguments = mutableMapOf()
                                    )
                                }, overrides = overrides
                            )
                        }
                    }
                    else -> {
                        {
                            context.run(
                                name = "closure override", body = {
                                    AstMethod.apply2(
                                        method,
                                        positionalArguments = mutableListOf(),
                                        namedArguments = mutableMapOf()
                                    )
                                }, overrides = overrides
                            )
                        }
                    }
                }
            } else {

            }
//                val topLevelVariable = runtime.getTopLevelVariable(name)
//                if (topLevelVariable != null) {
//                    if (keepVariable) {
//                        return topLevelVariable
//                    } else {
//                        return topLevelVariable.value
//                    }
//                }
//
            val clazz = AstClass.forName(name)
            if (clazz != null) {
                return clazz
            }
            return null
//                val libraryMirror = ReflectionBinding.instance.reflectTopLevelInvoke(name)
//                if (libraryMirror != null) {
//                    return libraryMirror.invokeGetter(name)
//                }
//
//                throw "Should not happen execute Identifier: ${expression.asIdentifier.name}"
        } else if (expression.isBlockStatement) {
            return _executeBlockStatement(expression.asBlockStatement, flag = flag);
        } else if (expression.isEmptyFunctionBody) {
            return null
        } else if (expression.isInstanceCreationExpression) {
            return executeInstanceCreationExpression(
                expression.asInstanceCreationExpression
            );
        } else if (expression.isPropertyAccess) {
            // 取值表达式，如demo.test()
            val propertyAccess = expression.asPropertyAccess
            var target: Any? = null
//            if (propertyAccess.isCascaded) {
//                target = findCascadeAncestorTargetValue(propertyAccess)
//            } else {
//                target = executeExpression(propertyAccess.targetExpression!!)
            target = executeExpression(propertyAccess.targetExpression!!)
//            }
            val instance = AstInstance.forObject(target)
            if (instance != null && !propertyAccess.name.isNullOrEmpty()) {
                propertyAccess.name?.let {
                    return instance.invokeGetter(it)
                }
            }
            if (target == null) {
//                if (propertyAccess.isNullAware) {
//                    return null
//                } else {
//                    throw IllegalArgumentException("Null check operator used on a null value")
//                }
            } else {

            }
        } else if (expression.isVariableDeclarationList) {
            return _executeVariableDeclaration(expression.asVariableDeclarationList)
        } else if (expression.isBinaryExpression) {
            return _executeBinaryExpression(expression.asBinaryExpression);
        } else if (expression.isReturnStatement) {
            var result: Any? = null
            if (expression.asReturnStatement.argument != null) {
                result = executeExpression(expression.asReturnStatement.argument!!)
            }
            returnFlags[flag ?: ""] = true
            return result

        } else if (expression.isIntegerLiteral) {
            return expression.asIntegerLiteral.value
        } else if (expression.isMethodInvocation) {
            //method调用
            return _exeAndRecMethodInvocation(expression.asMethodInvocation);
        } else if (expression.isStringTemplateExpression) {
            //string
            var result: Any? = null
            if (expression.asStringTemplateExpression != null && expression.asStringTemplateExpression.target != null) {
                result = executeExpression(expression.asStringTemplateExpression.target!!)
                return result
            }
            return result
        } else if (expression.isStringLiteral) {
            return expression.asIntegerLiteral.value
        } else if (expression.isStringTemplateEntry) {
            return expression.asStringTemplateEntry.value
        } else if (expression.isBooleanLiteral) {
            return expression.asBooleanLiteral.value
        } else if (expression.isCallExpression) {
            //callFunction调用
            val clazz = AstClass.forName(expression.asCallExpression.methodName)
            if (clazz != null) {
                return clazz
            } else {
                val runtime = context.get<AstRuntime>(AstRuntime::class.java)!!
                val function = runtime._program.getFunction(expression.asCallExpression.methodName)
                if (function != null) {
                    val result = AstMethod.apply2(
                        function,
                        expression.asCallExpression.argumentList.arguments ?: emptyList(),
                        emptyMap(),
                    )
                    return result
                }

                return null
            }
        } else {
            println("executeExpression ${expression.type} not implement")
        }
    } catch (e: Exception) {
//            if (e is DynamicCardException) {
//                throw e
//            } else {
        throw DynamicException.fromExpression(expression, e.toString())
//            }
    }
    return null
}

class DynamicException(message: String?) : RuntimeException(message) {
    var stackTrace = ""

    companion object {
        fun fromExpression(expression: Expression, e: String): Throwable {
            System.out.println("error:" + e)
            return DynamicException(message = e)
        }
    }

}

fun _executeVariableDeclaration(variableDeclarationList: PropertyStatement): Any? {
    val programStack = context.get<ProgramStack>(ProgramStack::class.java)!!

    val variableDeclarator = variableDeclarationList.target
    // ignore: unnecessary_null_comparison
//    if (variableDeclarator.init == null) {
//        // 光定义，没有赋值的场景
//        programStack.putVariable(variableDeclarator.name, null)
//        return null // TODO
//    }
//    if (variableDeclarator.init!!.isAwaitExpression) {
//        //await expression;
//        val value = _exeAndRecMethodInvocation(variableDeclarator.init!!.asAwaitExpression.expression)
//        programStack.putVariable(variableDeclarator.name, value)
//    } else if (variableDeclarator.init!!.isMethodInvocation) {
//        val value = _exeAndRecMethodInvocation(variableDeclarator.init!!.asMethodInvocation)
//        //存入声明的初始化变量值
//        programStack.putVariable(variableDeclarator.name, value)
//    } else {
//        //存入声明的初始化变量值
    val value = executeExpression(variableDeclarator!!)
    programStack.putVariable(variableDeclarationList.name, value)
//    }
    return null
}
//fun findCascadeAncestorTargetValue(child: Node): Any? {
//    fun helper(node: Node?): Any? {
//        return when {
//            node == null -> null
//            node is CascadeExpression -> node.targetValue
//            else -> helper(node.parent)
//        }
//    }
//    return helper(child)
//}


fun _executeBinaryExpression(binaryExpression: BinaryExpression): Any? {
    val evalLeft = executeExpression(binaryExpression.left)
    val evalRight = executeExpression(binaryExpression.right)
    val operator = binaryExpression.operator
    if (evalLeft is String || evalRight is String) {
        return when (operator) {
            "+" -> "$evalLeft$evalRight"
            else -> throw UnimplementedError("operator Int ${binaryExpression.operator}")
        }
    }
    if (evalLeft is Int && evalRight is Int) {
        return when (operator) {
            "+" -> evalLeft + evalRight
            "-" -> evalLeft - evalRight
            "*" -> evalLeft * evalRight
            "/" -> evalLeft / evalRight
            "<" -> evalLeft < evalRight
            ">" -> evalLeft > evalRight
            "<=" -> evalLeft <= evalRight
            ">=" -> evalLeft >= evalRight
            "==" -> evalLeft == evalRight
            "%" -> evalLeft % evalRight
            "<<" -> evalLeft shl evalRight
            "|" -> evalLeft or evalRight
            "&" -> evalLeft and evalRight
            ">>" -> evalLeft shr evalRight
            "!=" -> evalLeft != evalRight
            "/" -> evalLeft / evalRight
            else -> throw UnimplementedError("operator Int ${binaryExpression.operator}")
        }
    }

    if (evalLeft is Boolean && evalRight is Boolean) {
        return when (operator) {
            "&&" -> evalLeft && evalRight
            "||" -> evalLeft || evalRight
            else -> throw UnimplementedError("operator Boolean ${binaryExpression.operator}")
        }
    }
    return null
}


val returnFlags: MutableMap<String, Boolean> = mutableMapOf()
fun _executeBlockStatement(block: BlockStatement, flag: String? = null): Any? {
    var flagTemp = flag
    if (flag == null) {
        flagTemp = UUID.randomUUID().toString()
    }
    val programStack = context.get<ProgramStack>(ProgramStack::class.java)

    programStack?.push(name = "Block statement")
    var result: Any? = null
    if (block.body?.isNotEmpty() == true) {
        for (expression in block.body!!) {
            if (returnFlags[flag] == true) {
                returnFlags.remove(flagTemp)
                break
            }
            result = executeExpression(expression, flag = flagTemp)
        }
    }
    programStack?.pop()
    return result
}


class AstRuntime(val _program: ProgramNode) {

    public var programStack: ProgramStack? = null

    init {
        // 创建并初始化 ProxyBinding 实例
        val proxyBinding = ProxyBinding()
        proxyBinding.initInstances()
        this.programStack = ProgramStack("root stack:${_program.compilation.toUnit()["source"]}")
    }

    fun invoke2(memberName: String): AstMethod? {
        val function = _program.getFunction(memberName)
        return function
    }

    val classes: List<AstClass>
        get() {
            return context.run(
                name = "AstRuntime",
                body = {
                    _program.classes
                },
                overrides = mapOf(
                    ProgramStack::class.java to { programStack },
                    AstRuntime::class.java to { this }
                )
            ) as List<AstClass>
        }


    fun getClass(className: String): AstClass? {
        return context.run(
            name = "AstRuntime",
            body = {
                _program?.getClass(className)
            },
            overrides = mapOf(
                ProgramStack::class.java to { programStack },
                AstRuntime::class.java to { this }
            )
        ) as AstClass?
    }

    fun getReflectClass(className: String): ImportDirective? {
        return _program?.getReflectClass(className)
    }

    fun invoke2(
        memberName: String,
        positionalArguments: List<Any?>? = null,
        namedArguments: Map<String, Any?>? = null
    ): Any? {
        return context.run(
            name = "Invoke top-level function or variables",
            body = {
                val function = _program?.getFunction(memberName)
                if (function != null) {
                    context.run(name = "", body = {
                        val result = AstMethod.apply2(
                            function, positionalArguments ?: emptyList(), namedArguments
                        )
                        if (result is Variable) {
                            return@run result.value
                        }
                        invokeRunMain(result)
                        return@run result
                    }, overrides = mapOf(ProgramNode::class.java to { function.programNode }))
                } else {
                    null
                }
            },
            overrides = mapOf(
                ProgramStack::class.java to { programStack },
                AstRuntime::class.java to { this })
        )
    }



    fun invoke(
        memberName: String,
        positionalArguments: List<Any?>? = null,
        namedArguments: Map<String, Any?>? = null
    ): Any? {
        return context.run(
            name = "Invoke top-level function or variables",
            body = {
                val function = _program?.getFunction(memberName)
                if (function != null) {
                    context.run(name = "", body = {
                        val result = AstMethod.apply2(
                            function, positionalArguments ?: emptyList(), namedArguments
                        )
                        if (result is Variable) {
                            return@run result.value
                        }
                        return@run result
                    }, overrides = mapOf(ProgramNode::class.java to { function.programNode }))
                } else {
                    null
                }
            },
            overrides = mapOf(
                ProgramStack::class.java to { programStack },
                AstRuntime::class.java to { this })
        )
    }

    class LocalJson(val json: String) : ProgramEntity() {

        override val exists: Boolean
            get() = !TextUtils.isEmpty(json)
        val walker = ProgramDependencyWalker();
        override fun createNode(): ProgramNode {
            val transformMap = transformMap(json)
            val node = walker.getNode(this, CompilationUnit.fromUnit(transformMap))
            return node
        }

        fun transformMap(json: String): Map<String, Any> {
            try {
                // 序列化 Map 对象为 JSON 字符串
                val objectMapper = ObjectMapper()
                // 禁用 FAIL_ON_EMPTY_BEANS 特性
                objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

                // 反序列化 JSON 字符串为 Map 对象
                @Suppress("UNCHECKED_CAST")
                val deserializedMap =
                    objectMapper.readValue(json, HashMap::class.java) as Map<String, Any>
                println("Deserialized Map: $deserializedMap")
                return deserializedMap
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            return emptyMap()
        }

        fun getClassFilePath(clazz: Class<*>): String? {
            // 尝试从保护域获取类文件的位置
            clazz.protectionDomain?.codeSource?.location?.let {
                return try {
                    // 如果是文件形式存在，则返回其路径
                    it.toURI().path
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }

            // 尝试通过类加载器获取资源路径
            val resource = clazz.name.replace('.', '/') + ".class"
            val resourcePath = clazz.classLoader.getResource(resource)?.toURI()?.path
            if (resourcePath != null && resourcePath.endsWith(".jar")) {
                // 如果是在JAR内，则返回JAR文件路径
                return resourcePath
            }

            return resourcePath?.removeSuffix(".class")
        }

        fun convertClassToPath(fullClassName: String): String {
            return "src/test/java/" + fullClassName.replace('.', '/') + ".kt"
        }

        override fun getRelativeEntity(uri: String): ProgramEntity {
            // 获取文件的目录路径
//            val basePath = File(convertClassToPath(uri)).parentFile?.absolutePath
//                ?: throw IllegalArgumentException("Invalid base file path")
            return LocalFile(File(convertClassToPath(uri)))
        }

        private fun createCoreEnvironment(): KotlinCoreEnvironment {
            val disposable = Disposer.newDisposable()
            return KotlinCoreEnvironment.createForProduction(
                disposable,
                ProjectHelper.getConfiguration(),
                EnvironmentConfigFiles.JVM_CONFIG_FILES
            )
        }

        fun parseKotlinCode(content: String, fileName: String = "DummyFile.kt"): KtFile {
            val project: Project = createCoreEnvironment().project
            val psiFileFactory = PsiFileFactory.getInstance(project)
            return psiFileFactory.createFileFromText(fileName, content) as KtFile
        }

    }

    class GeneraJson(val file: File) {

        fun createNodeToJson(): String? {
            val ktFile = parseKotlinCode(file.readText())
            val visitor = MyKtVisitorV2()
            ktFile.accept(visitor)
            val unit = visitor.getResult()
            val json = transformJson(unit)
            return json
        }

        private fun transformJson(unit: Map<String, Any>): String? {
            try {
                // 序列化 Map 对象为 JSON 字符串
                val objectMapper = ObjectMapper()
                // 禁用 FAIL_ON_EMPTY_BEANS 特性
                objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
                val json: String? = objectMapper.writeValueAsString(unit)

                // 输出 JSON 字符串
                println(json)
                return json
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            return null
        }

        private fun transformMap(json: String): Map<String, Any> {
            try {
                // 序列化 Map 对象为 JSON 字符串
                val objectMapper = ObjectMapper()
                // 禁用 FAIL_ON_EMPTY_BEANS 特性
                objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

                // 反序列化 JSON 字符串为 Map 对象
                @Suppress("UNCHECKED_CAST")
                val deserializedMap =
                    objectMapper.readValue(json, HashMap::class.java) as Map<String, Any>
                println("Deserialized Map: $deserializedMap")
                return deserializedMap
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            return emptyMap()
        }

        private fun createCoreEnvironment(): KotlinCoreEnvironment {
            val disposable = Disposer.newDisposable()
            return KotlinCoreEnvironment.createForProduction(
                disposable,
                ProjectHelper.getConfiguration(),
                EnvironmentConfigFiles.JVM_CONFIG_FILES
            )
        }

        fun parseKotlinCode(content: String, fileName: String = "DummyFile.kt"): KtFile {
            val project: Project = createCoreEnvironment().project
            val psiFileFactory = PsiFileFactory.getInstance(project)
            return psiFileFactory.createFileFromText(fileName, content) as KtFile
        }

    }

    class LocalFile(val file: File) : ProgramEntity() {

        override val exists: Boolean
            get() = file.exists()
        val walker = ProgramDependencyWalker();
        override fun createNode(): ProgramNode {
            val ktFile = parseKotlinCode(file.readText())
            val visitor = MyKtVisitorV2()
            ktFile.accept(visitor)
//            val unit = visitor.getResult()
            val transformMap = transformMap(getDefaultJson())
            val node = walker.getNode(this, CompilationUnit.fromUnit(transformMap))
            return node
        }

        private fun transformJson(unit: Map<String, Any>) {
            try {
                // 序列化 Map 对象为 JSON 字符串
                val objectMapper = ObjectMapper()
                // 禁用 FAIL_ON_EMPTY_BEANS 特性
                objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
                val json: String? = objectMapper.writeValueAsString(unit)

                // 输出 JSON 字符串
                println(json)

                // 反序列化 JSON 字符串为 Map 对象
                @Suppress("UNCHECKED_CAST")
                val deserializedMap =
                    objectMapper.readValue(json, HashMap::class.java) as Map<String, Any>
                println("Deserialized Map: $deserializedMap")
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }

        fun transformMap(json: String): Map<String, Any> {
            try {
                // 序列化 Map 对象为 JSON 字符串
                val objectMapper = ObjectMapper()
                // 禁用 FAIL_ON_EMPTY_BEANS 特性
                objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

                // 反序列化 JSON 字符串为 Map 对象
                @Suppress("UNCHECKED_CAST")
                val deserializedMap =
                    objectMapper.readValue(json, HashMap::class.java) as Map<String, Any>
                println("Deserialized Map: $deserializedMap")
                return deserializedMap
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            return emptyMap()
        }

        fun getDefaultJson(): String {
            return "{\n" +
                    "  \"directives\" : [ ],\n" +
                    "  \"declarations\" : [ {\n" +
                    "    \"type\" : \"ClassDeclaration\",\n" +
                    "    \"name\" : \"DemoEmptyFunctionExpress\",\n" +
                    "    \"members\" : [ {\n" +
                    "      \"type\" : \"MethodDeclaration\",\n" +
                    "      \"name\" : \"test2\",\n" +
                    "      \"parameters\" : [ {\n" +
                    "        \"type\" : \"SimpleFormalParameter\",\n" +
                    "        \"name\" : \"index\",\n" +
                    "        \"typeName\" : \"Int\"\n" +
                    "      }, {\n" +
                    "        \"type\" : \"SimpleFormalParameter\",\n" +
                    "        \"name\" : \"item\",\n" +
                    "        \"typeName\" : \"String\"\n" +
                    "      } ],\n" +
                    "      \"typeParameters\" : [ ],\n" +
                    "      \"body\" : {\n" +
                    "        \"type\" : \"BlockStatement\",\n" +
                    "        \"body\" : [ {\n" +
                    "          \"type\" : \"PropertyStatement\",\n" +
                    "          \"name\" : \"a\",\n" +
                    "          \"initializer\" : {\n" +
                    "            \"type\" : \"INTEGER_CONSTANT\",\n" +
                    "            \"value\" : \"2\",\n" +
                    "            \"name\" : \"ConstantExpression\"\n" +
                    "          }\n" +
                    "        }, {\n" +
                    "          \"type\" : \"PropertyStatement\",\n" +
                    "          \"name\" : \"b\",\n" +
                    "          \"initializer\" : {\n" +
                    "            \"type\" : \"BinaryExpression\",\n" +
                    "            \"name\" : null,\n" +
                    "            \"operator\" : \"+\",\n" +
                    "            \"left\" : {\n" +
                    "              \"type\" : \"INTEGER_CONSTANT\",\n" +
                    "              \"value\" : \"2\",\n" +
                    "              \"name\" : \"ConstantExpression\"\n" +
                    "            },\n" +
                    "            \"right\" : {\n" +
                    "              \"type\" : \"Identifier\",\n" +
                    "              \"name\" : \"index\"\n" +
                    "            }\n" +
                    "          }\n" +
                    "        }, {\n" +
                    "          \"type\" : \"ReturnStatement\",\n" +
                    "          \"argument\" : {\n" +
                    "            \"type\" : \"BinaryExpression\",\n" +
                    "            \"name\" : null,\n" +
                    "            \"operator\" : \"*\",\n" +
                    "            \"left\" : {\n" +
                    "              \"type\" : \"Identifier\",\n" +
                    "              \"name\" : \"a\"\n" +
                    "            },\n" +
                    "            \"right\" : {\n" +
                    "              \"type\" : \"Identifier\",\n" +
                    "              \"name\" : \"b\"\n" +
                    "            }\n" +
                    "          }\n" +
                    "        } ]\n" +
                    "      },\n" +
                    "      \"isStatic\" : true,\n" +
                    "      \"isGetter\" : false,\n" +
                    "      \"isSetter\" : false\n" +
                    "    } ],\n" +
                    "    \"body\" : { }\n" +
                    "  } ],\n" +
                    "  \"type\" : \"CompilationUnit\"\n" +
                    "}"
        }

        fun getClassFilePath(clazz: Class<*>): String? {
            // 尝试从保护域获取类文件的位置
            clazz.protectionDomain?.codeSource?.location?.let {
                return try {
                    // 如果是文件形式存在，则返回其路径
                    it.toURI().path
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }

            // 尝试通过类加载器获取资源路径
            val resource = clazz.name.replace('.', '/') + ".class"
            val resourcePath = clazz.classLoader.getResource(resource)?.toURI()?.path
            if (resourcePath != null && resourcePath.endsWith(".jar")) {
                // 如果是在JAR内，则返回JAR文件路径
                return resourcePath
            }

            return resourcePath?.removeSuffix(".class")
        }

        fun convertClassToPath(fullClassName: String): String {
            return "src/test/java/" + fullClassName.replace('.', '/') + ".kt"
        }

        override fun getRelativeEntity(uri: String): ProgramEntity {
            // 获取文件的目录路径
//            val basePath = File(convertClassToPath(uri)).parentFile?.absolutePath
//                ?: throw IllegalArgumentException("Invalid base file path")
            return LocalFile(File(convertClassToPath(uri)))
        }

        private fun createCoreEnvironment(): KotlinCoreEnvironment {
            val disposable = Disposer.newDisposable()
            return KotlinCoreEnvironment.createForProduction(
                disposable,
                ProjectHelper.getConfiguration(),
                EnvironmentConfigFiles.JVM_CONFIG_FILES
            )
        }

        fun parseKotlinCode(content: String, fileName: String = "DummyFile.kt"): KtFile {
            val project: Project = createCoreEnvironment().project
            val psiFileFactory = PsiFileFactory.getInstance(project)
            return psiFileFactory.createFileFromText(fileName, content) as KtFile
        }

    }

    class ProgramDependencyWalker {
        val nodeMap = mutableMapOf<ProgramEntity, ProgramNode>()

        fun evaluate(node: ProgramNode) {
            node.evaluate()
        }

        fun evaluateScc(scc: List<ProgramNode>) {
            for (node in scc) {
                node.markCircular()
            }
        }

        fun getNode(entity: ProgramEntity, compilation: CompilationUnit): ProgramNode {
            val programNode = nodeMap.getOrPut(entity) { ProgramNode(entity, this, compilation) }
            programNode.dependencies = programNode.computeDependencies()
            return programNode
        }
    }

    // Assuming the ProgramNode class has these methods:
    class ProgramNode(
        val entity: ProgramEntity,
        val walker: ProgramDependencyWalker,
        val compilation: CompilationUnit
    ) {
        var _classDeclarations: Map<String, ClassDeclaration>? = null
        var _methodDeclarations: Map<String, MethodDeclaration>? = null
        var _variableDeclarators: Map<String, VariableDeclarator>? = null

        private val _classes: MutableMap<String, AstClass> = mutableMapOf()
        private val _functions: MutableMap<String, AstMethod> = mutableMapOf()
        private val _topLevelVariables: MutableMap<String, AstVariable> = mutableMapOf()
        var dependencies: List<ProgramNode> = emptyList()
        var data: MutableMap<String, ImportDirective> = mutableMapOf()
        fun evaluate() {
            // Implement the evaluation logic here
            TODO("Implement evaluate for ProgramNode")
        }

        fun markCircular() {
            // Implement the circular marking logic here
            TODO("Implement markCircular for ProgramNode")
        }

        private val methodDeclarations: Map<String, MethodDeclaration>?
            get() {
                if (_methodDeclarations == null) {
//                    val result = mutableMapOf<String, FunctionDeclaration>()
//                    compilation.declarations
//                        ?.filterIsInstance<FunctionDeclaration>()
//                        ?.forEach { declaration ->
//                            result[declaration.name] = declaration
//                        }
//                    _functionDeclarations = result
                    val result = mutableMapOf<String, MethodDeclaration>()
                    compilation.declarations?.forEach {
                        (it.get() as ClassDeclaration).members?.forEach { declaration ->
                            if (declaration.get() is MethodDeclaration) {
                                result[((declaration?.get() as MethodDeclaration).name ?: "")] =
                                    declaration.get() as MethodDeclaration
                                _methodDeclarations = result
                            }
                        }
                    }
                }

                return _methodDeclarations
            }


        val classes: List<AstClass>
            get() = compilation.declarations?.filter { it.isClassDeclaration }
                ?.map { it.asClassDeclaration }
                ?.mapNotNull { getClass(it.name) }
                ?.toList() ?: emptyList()

        fun ensureClassDeclarations() {
            var classDeclarations = _classDeclarations
            if (classDeclarations == null) {
                val result = mutableMapOf<String, ClassDeclaration>()
                compilation.declarations
                    ?.filter { it.isClassDeclaration }
                    ?.map { it.asClassDeclaration }
                    ?.forEach { declaration ->
                        result[declaration.name] = declaration
                    }
                _classDeclarations = result.unmodifiable()
            }
        }

        fun getReflectClass(className: String, recursive: Boolean = true): ImportDirective? {
            val importDirective = data[className]
            return importDirective
        }


        fun getClass(className: String, recursive: Boolean = true): AstClass? {
            ensureClassDeclarations()
            _classes[className]?.let { return it }
            _classDeclarations?.get(className)?.let {
                val clazz = AstClass.fromClass(it, this)
                _classes[className] = clazz
                return clazz
            }
            if (!recursive) return null
            dependencies.forEach {
                it.getClass(className, recursive = false)?.let { clazz -> return clazz }
            }
            return null
        }

        fun <K, V> Map<K, V>.unmodifiable(): Map<K, V> {
            return Collections.unmodifiableMap(this)
        }


        // 获取函数声明
        fun getFunction(functionName: String, recursive: Boolean = true): AstMethod? {
            val functions = methodDeclarations
            if (functions != null && functions.containsKey(functionName)) {
                val functionDeclaration = functions[functionName]
                if (functionDeclaration != null) {
                    return AstMethod.fromFunction(functionDeclaration, this)
                }
            }
            if (!recursive) {
                return null
            }

            for (dependency in dependencies) {
                val function = dependency.getFunction(functionName, recursive = false)
                if (function != null) {
                    return function
                }
            }

            return null
        }

        private fun _visitNode(
            entity: ProgramEntity,
            dependencies: MutableSet<ProgramNode>,
            node: ProgramNode
        ) {
            for (directive in node.compilation.directives!!) {
                when (directive) {
                    is ImportDirective -> {
                        val import = Import(directive.uri, "", null)
                        val newEntity = entity.getRelativeEntity(import.uri)
                        val newNode = walker.nodeMap[newEntity] ?: run {
                            if (newEntity.exists) {
                                val newNode = newEntity.createNode()
                                walker.nodeMap.putIfAbsent(newEntity, newNode)
                                newNode
                            } else null
                        }

                        if (newNode != null) {
                            if (!dependencies.contains(newNode)) {
                                dependencies.add(newNode)
                                _visitNode(newEntity, dependencies, newNode)
                            }
                        } else {
                            //反射
                            data[directive.name] = directive
                        }
                    }
//                    is PartDirective -> {
//                        val newEntity = entity.getRelativeEntity(directive.uri)
//                        val newNode = walker.nodeMap[newEntity] ?: run {
//                            if (newEntity.exists) {
//                                val newNode = newEntity.createNode()
//                                walker.nodeMap.putIfAbsent(newEntity, newNode)
//                                newNode
//                            } else null
//                        }
//                        newNode?.let {
//                            if (!dependencies.contains(it)) {
//                                dependencies.add(it)
//                                _visitNode(newEntity, dependencies, it)
//                            }
//                        }
//                    }
                }
            }
        }

        fun computeDependencies(): List<ProgramNode> {
            val dependencies = mutableSetOf<ProgramNode>()
            _visitNode(entity, dependencies, this)
            return dependencies.toList()
        }

    }

    abstract class ProgramEntity {
        abstract val exists: Boolean

        abstract fun createNode(): ProgramNode

        abstract fun getRelativeEntity(uri: String): ProgramEntity
    }

}