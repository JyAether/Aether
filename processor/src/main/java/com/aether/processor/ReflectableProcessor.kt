package com.aether.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.aether.annotations.Reflectable

class ReflectableProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(Reflectable::class.qualifiedName!!)
        val ret = symbols.filter { !it.validate() }.toList()
        
        symbols
            .filter { it is KSClassDeclaration && it.validate() }
            .forEach { it.accept(ReflectableVisitor(), Unit) }
            
        return ret
    }

    inner class ReflectableVisitor : KSVisitorVoid() {
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            val packageName = classDeclaration.packageName.asString()
            val className = classDeclaration.simpleName.asString()
            
            // 生成反射支持代码
            val file = codeGenerator.createNewFile(
                dependencies = Dependencies(false),
                packageName = packageName,
                fileName = "${className}Reflector"
            )
            
            file.appendText("""
                package $packageName
                
                import androidx.compose.runtime.Composable
                import com.aether.core.runtime.reflectable.ComposeComponentRegistry
                
                object ${className}Reflector {
                    @Composable
                    fun createInstance(
                        name: String,
                        arguments: Map<String, Any> = emptyMap()
                    ) {
                        when (name) {
                            "${classDeclaration.qualifiedName?.asString()}" -> {
                                $className()
                            }
                            else -> {
                                // 处理其他情况
                            }
                        }
                    }
                }
            """.trimIndent())
            
            file.close()
        }
    }
}

class ReflectableProcessorProvider : SymbolProcessorProvider {
    override fun create(
        environment: SymbolProcessorEnvironment
    ): SymbolProcessor {
        return ReflectableProcessor(
            environment.codeGenerator,
            environment.logger,
            environment.options
        )
    }
} 