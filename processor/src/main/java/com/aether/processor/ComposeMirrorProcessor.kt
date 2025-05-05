package com.aether.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.writeTo

class ComposeMirrorProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation("com.aether.annotations.ComposeMirror")
        val ret = symbols.filter { !it.validate() }.toList()
        
        symbols
            .filter { it is KSClassDeclaration && it.validate() }
            .forEach { it.accept(ComposeMirrorVisitor(), Unit) }
            
        return ret
    }

    inner class ComposeMirrorVisitor : KSVisitorVoid() {
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            val packageName = classDeclaration.packageName.asString()
            val className = classDeclaration.simpleName.asString()
            
            // Generate mirror class
            val mirrorClassName = "${className}Mirror"
            val fileSpec = FileSpec.builder(packageName, mirrorClassName)
                .addType(
                    TypeSpec.classBuilder(mirrorClassName)
                        .addModifiers(KModifier.PUBLIC)
                        .addFunction(
                            FunSpec.builder("invoke")
                                .addModifiers(KModifier.PUBLIC)
                                .addParameter("params", List::class.asTypeName().parameterizedBy(Any::class.asTypeName()))
                                .returns(Any::class)
                                .addCode("""
                                    |return $className().apply {
                                    |    params.forEachIndexed { index, param ->
                                    |        when(index) {
                                    |            // Add parameter assignments based on constructor parameters
                                    |        }
                                    |    }
                                    |}
                                """.trimMargin())
                                .build()
                        )
                        .build()
                )
                .build()
                
            fileSpec.writeTo(codeGenerator, false)
        }
    }
}

class ComposeMirrorProcessorProvider : SymbolProcessorProvider {
    override fun create(
        environment: SymbolProcessorEnvironment
    ): SymbolProcessor {
        return ComposeMirrorProcessor(
            environment.codeGenerator,
            environment.logger,
            environment.options
        )
    }
} 