package com.aether.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.writeTo

class ComposeReflectProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation("com.aether.annotations.ComposeReflect")
        val ret = symbols.filter { !it.validate() }.toList()
        
        symbols
            .filter { it is KSClassDeclaration && it.validate() }
            .forEach { it.accept(ComposeReflectVisitor(), Unit) }
            
        return ret
    }

    inner class ComposeReflectVisitor : KSVisitorVoid() {
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            val packageName = classDeclaration.packageName.asString()
            val className = classDeclaration.simpleName.asString()
            
            // Generate reflection class
            val reflectClassName = "${className}Reflect"
            val fileSpec = FileSpec.builder(packageName, reflectClassName)
                .addType(
                    TypeSpec.classBuilder(reflectClassName)
                        .addModifiers(KModifier.PUBLIC)
                        .primaryConstructor(
                            FunSpec.constructorBuilder()
                                .addParameter("instance", ClassName(packageName, className))
                                .build()
                        )
                        .addProperty(
                            PropertySpec.builder("instance", ClassName(packageName, className))
                                .initializer("instance")
                                .build()
                        )
                        .addFunctions(generateReflectFunctions(classDeclaration))
                        .build()
                )
                .build()
                
            fileSpec.writeTo(codeGenerator, false)
        }

        private fun generateReflectFunctions(classDeclaration: KSClassDeclaration): List<FunSpec> {
            val functions = mutableListOf<FunSpec>()
            
            classDeclaration.declarations
                .filterIsInstance<KSFunctionDeclaration>()
                .filter { it.annotations.any { ann -> ann.annotationType.resolve().declaration.qualifiedName?.asString() == "com.aether.annotations.Reflectable" } }
                .forEach { function ->
                    val functionName = function.simpleName.asString()
                    val parameters = function.parameters.map { param ->
                        ParameterSpec.builder(
                            param.name?.asString() ?: "param",
                            param.type.resolve().toTypeName()
                        ).build()
                    }
                    
                    functions.add(
                        FunSpec.builder("invoke$functionName")
                            .addModifiers(KModifier.PUBLIC)
                            .addParameters(parameters)
                            .returns(Unit::class)
                            .addCode("""
                                |instance.$functionName(${parameters.joinToString { it.name }})
                            """.trimMargin())
                            .build()
                    )
                }
                
            return functions
        }
    }
}

class ComposeReflectProcessorProvider : SymbolProcessorProvider {
    override fun create(
        environment: SymbolProcessorEnvironment
    ): SymbolProcessor {
        return ComposeReflectProcessor(
            environment.codeGenerator,
            environment.logger,
            environment.options
        )
    }
} 