package com.aether.processor

import com.aether.annotations.GenerateMirror
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec

class ComposeMirrorProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        // 查找所有标记了@GenerateMirror的符号
        val symbols = resolver.getSymbolsWithAnnotation(GenerateMirror::class.qualifiedName!!)

        // 处理标记的函数和类
        symbols.filter { it is KSFunctionDeclaration || it is KSClassDeclaration }
            .forEach {
//                processSymbol(it)
            }

        // 自动处理常用的Compose组件
        processComposeComponents(resolver)

        return emptyList()
    }

    private fun processComposeComponents(resolver: Resolver) {
        // 查找Text组件的所有重载
        val textFunctions = resolver.getAllFiles().flatMap { file ->
            file.declarations.filterIsInstance<KSFunctionDeclaration>()
                .filter { it.simpleName.asString() == "Text" }
        }

        // 为Text组件生成反射元数据
        textFunctions.forEach { generateTextMirror(it) }
    }

    private fun generateTextMirror(function: KSFunctionDeclaration) {
        // 分析函数参数
        val params = function.parameters.map { param ->
            val name = param.name?.asString() ?: ""
            val type = param.type.resolve().declaration.qualifiedName?.asString() ?: "Any"
            val isOptional = param.hasDefault

            ParamInfo(name, type, isOptional)
        }

        // 生成Text组件的反射元数据
        generateMirrorClass("Text", params)
    }

    private fun generateMirrorClass(name: String, params: List<ParamInfo>) {
        val fileName = "${name}Mirror"
        val packageName = "com.your.package.mirror.generated"

        val fileSpec = FileSpec.builder(packageName, fileName)
            .addType(
                TypeSpec.classBuilder(fileName)
                    .addProperty(
                        PropertySpec.builder("name", String::class)
                            .initializer("\"$name\"")
                            .build()
                    )
                    .addProperty(
                        PropertySpec.builder("params", LIST.parameterizedBy(
                            ClassName("com.your.package.mirror", "ParamInfo")
                        ))
                            .initializer(buildParamsList(params))
                            .build()
                    )
                    .addFunction(
                        FunSpec.builder("invoke")
                            .addParameter("args", MAP.parameterizedBy(
                                STRING, ANY.copy(nullable = true)
                            ))
                            .addCode(buildInvokeCode(name, params))
                            .build()
                    )
                    .build()
            )
            .build()

        codeGenerator.createNewFile(
            Dependencies(false),
            packageName,
            fileName
        ).use { outputStream ->
            outputStream.writer().use {
                fileSpec.writeTo(it)
            }
        }
    }

    private fun buildParamsList(params: List<ParamInfo>): CodeBlock {
        return CodeBlock.builder()
            .add("listOf(\n")
            .indent()
            .apply {
                params.forEachIndexed { index, param ->
                    add("ParamInfo(\n")
                    indent()
                    add("name = %S,\n", param.name)
                    add("type = %S,\n", param.type)
                    add("isOptional = %L\n", param.isOptional)
                    unindent()
                    add(")")
                    if (index < params.size - 1) add(",\n") else add("\n")
                }
            }
            .unindent()
            .add(")")
            .build()
    }

    private fun buildInvokeCode(name: String, params: List<ParamInfo>): CodeBlock {
        return CodeBlock.builder()
            .beginControlFlow("return androidx.compose.runtime.Composable { ")
            .add("androidx.compose.material.Text(\n")
            .indent()
            .apply {
                params.forEach { param ->
                    add("%L = args[%S] as? %L,\n",
                        param.name,
                        param.name,
                        param.type.split(".").last()
                    )
                }
            }
            .unindent()
            .add(")\n")
            .endControlFlow()
            .build()
    }

    private data class ParamInfo(
        val name: String,
        val type: String,
        val isOptional: Boolean
    )

    // 处理器提供者
    class ComposeMirrorProcessorProvider2 : SymbolProcessorProvider {
        override fun create(
            environment: SymbolProcessorEnvironment
        ): SymbolProcessor {
            return ComposeMirrorProcessor(
                environment.codeGenerator,
                environment.logger
            )
        }
    }
}