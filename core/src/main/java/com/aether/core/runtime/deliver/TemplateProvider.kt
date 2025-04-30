package com.aether.core.runtime.deliver

import com.aether.core.runtime.AstRuntime
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.io.File
import java.util.zip.ZipFile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.aether.core.runtime.AppContextManager.context
import com.aether.core.runtime._exeAndRecMethodInvocation
import com.aether.core.runtime.reflectable.ComposeComponentDescriptor
import com.aether.core.runtime.reflectable.RenderComponent

/**
 * Arguments for a dynamic Composable
 */
class Arguments(
    val positionalArguments: List<Any?> = emptyList(),
    val namedArguments: Map<String, Any?> = emptyMap()
)

/**
 * Events that can be triggered during CloudComposable lifecycle
 */
enum class EasyComposeEvent {
    LOAD_COMPLETE,
    LOAD_ERROR
}

/**
 * Interface for template providers
 */
interface TemplateProvider {
    val key: String
    fun resolve(): Flow<TemplateResult>
}

/**
 * Result of template loading
 */
data class TemplateResult(
    val programNode: AstRuntime.ProgramNode,
    val sourceKey: String
)

/**
 * Network template provider
 */
class NetworkTemplate(
    private val url: String,
    private val fileMd5: String? = null
) : TemplateProvider {
    override val key: String
        get() = "network_$url${fileMd5?.let { "_$it" } ?: ""}"

    override fun resolve(): Flow<TemplateResult> {
        return TemplateLoader.loadFromNetwork(url, fileMd5)
            .flowOn(Dispatchers.IO)
    }
}

/**
 * Asset template provider
 */
class AssetTemplate(
    private val assetName: String
) : TemplateProvider {
    override val key: String
        get() = "asset_$assetName"

    override fun resolve(): Flow<TemplateResult> {
        return TemplateLoader.loadFromAsset(assetName)
            .flowOn(Dispatchers.IO)
    }
}

/**
 * File template provider
 */
class FileTemplate(
    private val file: File
) : TemplateProvider {
    override val key: String
        get() = "file_${file.absolutePath}"

    override fun resolve(): Flow<TemplateResult> {
        return TemplateLoader.loadFromFile(file)
            .flowOn(Dispatchers.IO)
    }
}

/**
 * Zip file template provider
 */
class ZipFileTemplate(
    private val zipFile: File
) : TemplateProvider {
    override val key: String
        get() = "zip_${zipFile.absolutePath}"

    override fun resolve(): Flow<TemplateResult> {
        return TemplateLoader.loadFromZipFile(zipFile)
            .flowOn(Dispatchers.IO)
    }
}

/**
 * Helper class for loading templates
 */
object TemplateLoader {
    fun loadFromNetwork(url: String, fileMd5: String? = null): Flow<TemplateResult> {
        val resultFlow = MutableStateFlow<TemplateResult?>(null)

        try {
            // Download code from network and create local file
            val tempFile = File.createTempFile("network_template", ".kt")
            // For demo purposes, using placeholder content
            tempFile.writeText(
                """
                @Composable
                fun Main() {
                    Text("Network content from $url")
                }
            """.trimIndent()
            )

            // Create AstRuntime from file
            val localFile = AstRuntime.LocalFile(tempFile)
            val node = localFile.createNode()

            resultFlow.value = TemplateResult(
                programNode = node,
                sourceKey = "network_$url${fileMd5?.let { "_$it" } ?: ""}"
            )
        } catch (e: Exception) {
            throw e
        }

        return resultFlow as Flow<TemplateResult>
    }

    fun loadFromAsset(assetName: String): Flow<TemplateResult> {
        val resultFlow = MutableStateFlow<TemplateResult?>(null)

        try {
//            val tempFile = File.createTempFile("asset_template", ".kt")
//            tempFile.writeText(
//                """
//                @Composable
//                fun Main() {
//                    Text("Asset content from $assetName")
//                }
//            """.trimIndent()
//            )
//            val json = "{\n" +
//                    "  \"directives\" : [ {\n" +
//                    "    \"type\" : \"ImportDirective\",\n" +
//                    "    \"importPath\" : \"androidx.compose.foundation.background\",\n" +
//                    "    \"alias\" : null,\n" +
//                    "    \"name\" : \"background\"\n" +
//                    "  }, {\n" +
//                    "    \"type\" : \"ImportDirective\",\n" +
//                    "    \"importPath\" : \"androidx.compose.foundation.layout.Box\",\n" +
//                    "    \"alias\" : null,\n" +
//                    "    \"name\" : \"Box\"\n" +
//                    "  }, {\n" +
//                    "    \"type\" : \"ImportDirective\",\n" +
//                    "    \"importPath\" : \"androidx.compose.foundation.layout.Column\",\n" +
//                    "    \"alias\" : null,\n" +
//                    "    \"name\" : \"Column\"\n" +
//                    "  }, {\n" +
//                    "    \"type\" : \"ImportDirective\",\n" +
//                    "    \"importPath\" : \"androidx.compose.foundation.layout.fillMaxWidth\",\n" +
//                    "    \"alias\" : null,\n" +
//                    "    \"name\" : \"fillMaxWidth\"\n" +
//                    "  }, {\n" +
//                    "    \"type\" : \"ImportDirective\",\n" +
//                    "    \"importPath\" : \"androidx.compose.foundation.layout.height\",\n" +
//                    "    \"alias\" : null,\n" +
//                    "    \"name\" : \"height\"\n" +
//                    "  }, {\n" +
//                    "    \"type\" : \"ImportDirective\",\n" +
//                    "    \"importPath\" : \"androidx.compose.foundation.layout.padding\",\n" +
//                    "    \"alias\" : null,\n" +
//                    "    \"name\" : \"padding\"\n" +
//                    "  }, {\n" +
//                    "    \"type\" : \"ImportDirective\",\n" +
//                    "    \"importPath\" : \"androidx.compose.foundation.shape.RoundedCornerShape\",\n" +
//                    "    \"alias\" : null,\n" +
//                    "    \"name\" : \"RoundedCornerShape\"\n" +
//                    "  }, {\n" +
//                    "    \"type\" : \"ImportDirective\",\n" +
//                    "    \"importPath\" : \"androidx.compose.material.Button\",\n" +
//                    "    \"alias\" : null,\n" +
//                    "    \"name\" : \"Button\"\n" +
//                    "  }, {\n" +
//                    "    \"type\" : \"ImportDirective\",\n" +
//                    "    \"importPath\" : \"androidx.compose.material.Text\",\n" +
//                    "    \"alias\" : null,\n" +
//                    "    \"name\" : \"Text\"\n" +
//                    "  }, {\n" +
//                    "    \"type\" : \"ImportDirective\",\n" +
//                    "    \"importPath\" : \"androidx.compose.runtime.Composable\",\n" +
//                    "    \"alias\" : null,\n" +
//                    "    \"name\" : \"Composable\"\n" +
//                    "  }, {\n" +
//                    "    \"type\" : \"ImportDirective\",\n" +
//                    "    \"importPath\" : \"androidx.compose.ui.Alignment\",\n" +
//                    "    \"alias\" : null,\n" +
//                    "    \"name\" : \"Alignment\"\n" +
//                    "  }, {\n" +
//                    "    \"type\" : \"ImportDirective\",\n" +
//                    "    \"importPath\" : \"androidx.compose.ui.Modifier\",\n" +
//                    "    \"alias\" : null,\n" +
//                    "    \"name\" : \"Modifier\"\n" +
//                    "  }, {\n" +
//                    "    \"type\" : \"ImportDirective\",\n" +
//                    "    \"importPath\" : \"androidx.compose.ui.draw.clip\",\n" +
//                    "    \"alias\" : null,\n" +
//                    "    \"name\" : \"clip\"\n" +
//                    "  }, {\n" +
//                    "    \"type\" : \"ImportDirective\",\n" +
//                    "    \"importPath\" : \"androidx.compose.ui.graphics.Color\",\n" +
//                    "    \"alias\" : null,\n" +
//                    "    \"name\" : \"Color\"\n" +
//                    "  }, {\n" +
//                    "    \"type\" : \"ImportDirective\",\n" +
//                    "    \"importPath\" : \"androidx.compose.ui.unit.dp\",\n" +
//                    "    \"alias\" : null,\n" +
//                    "    \"name\" : \"dp\"\n" +
//                    "  } ],\n" +
//                    "  \"declarations\" : [ {\n" +
//                    "    \"type\" : \"ClassDeclaration\",\n" +
//                    "    \"name\" : \"DemoCompose\",\n" +
//                    "    \"members\" : [ {\n" +
//                    "      \"type\" : \"MethodDeclaration\",\n" +
//                    "      \"name\" : \"Main\",\n" +
//                    "      \"parameters\" : [ ],\n" +
//                    "      \"typeParameters\" : [ ],\n" +
//                    "      \"body\" : {\n" +
//                    "        \"type\" : \"BlockStatement\",\n" +
//                    "        \"body\" : [ {\n" +
//                    "          \"type\" : \"InstanceCreationExpression\",\n" +
//                    "          \"constructorName\" : \"Column\",\n" +
//                    "          \"argumentList\" : {\n" +
//                    "            \"type\" : \"ArgumentList\",\n" +
//                    "            \"arguments\" : [ {\n" +
//                    "              \"type\" : \"Argument\",\n" +
//                    "              \"body\" : {\n" +
//                    "                \"type\" : \"MethodInvocation\",\n" +
//                    "                \"name\" : null,\n" +
//                    "                \"methodName\" : {\n" +
//                    "                  \"type\" : \"CallExpression\",\n" +
//                    "                  \"methodName\" : {\n" +
//                    "                    \"type\" : \"Identifier\",\n" +
//                    "                    \"name\" : \"fillMaxWidth\"\n" +
//                    "                  },\n" +
//                    "                  \"target\" : {\n" +
//                    "                    \"type\" : \"Identifier\",\n" +
//                    "                    \"name\" : \"fillMaxWidth\"\n" +
//                    "                  },\n" +
//                    "                  \"argumentList\" : {\n" +
//                    "                    \"type\" : \"ArgumentList\",\n" +
//                    "                    \"arguments\" : [ ]\n" +
//                    "                  },\n" +
//                    "                  \"valueArgument\" : [ ]\n" +
//                    "                },\n" +
//                    "                \"target\" : {\n" +
//                    "                  \"type\" : \"Identifier\",\n" +
//                    "                  \"name\" : \"Modifier\"\n" +
//                    "                }\n" +
//                    "              }\n" +
//                    "            }, {\n" +
//                    "              \"type\" : \"Argument\",\n" +
//                    "              \"body\" : {\n" +
//                    "                \"type\" : \"MethodInvocation\",\n" +
//                    "                \"name\" : null,\n" +
//                    "                \"methodName\" : {\n" +
//                    "                  \"type\" : \"Identifier\",\n" +
//                    "                  \"name\" : \"CenterHorizontally\"\n" +
//                    "                },\n" +
//                    "                \"target\" : {\n" +
//                    "                  \"type\" : \"Identifier\",\n" +
//                    "                  \"name\" : \"Alignment\"\n" +
//                    "                }\n" +
//                    "              }\n" +
//                    "            } ]\n" +
//                    "          },\n" +
//                    "          \"valueArgument\" : [ ]\n" +
//                    "        } ]\n" +
//                    "      },\n" +
//                    "      \"isStatic\" : false,\n" +
//                    "      \"isGetter\" : true,\n" +
//                    "      \"isSetter\" : false\n" +
//                    "    } ],\n" +
//                    "    \"body\" : { }\n" +
//                    "  } ],\n" +
//                    "  \"type\" : \"CompilationUnit\"\n" +
//                    "}"

            val json = "{\n" +
                    "  \"directives\" : [ {\n" +
                    "    \"type\" : \"ImportDirective\",\n" +
                    "    \"importPath\" : \"androidx.compose.material.Text\",\n" +
                    "    \"alias\" : null,\n" +
                    "    \"name\" : \"Text\"\n" +
                    "  }, {\n" +
                    "    \"type\" : \"ImportDirective\",\n" +
                    "    \"importPath\" : \"androidx.compose.runtime.Composable\",\n" +
                    "    \"alias\" : null,\n" +
                    "    \"name\" : \"Composable\"\n" +
                    "  } ],\n" +
                    "  \"declarations\" : [ {\n" +
                    "    \"type\" : \"ClassDeclaration\",\n" +
                    "    \"name\" : \"DemoCompose\",\n" +
                    "    \"members\" : [ {\n" +
                    "      \"type\" : \"MethodDeclaration\",\n" +
                    "      \"name\" : \"Main\",\n" +
                    "      \"parameters\" : [ ],\n" +
                    "      \"typeParameters\" : [ ],\n" +
                    "      \"body\" : {\n" +
                    "        \"type\" : \"BlockStatement\",\n" +
                    "        \"body\" : [ {\n" +
                    "          \"type\" : \"InstanceCreationExpression\",\n" +
                    "          \"constructorName\" : \"Text\",\n" +
                    "          \"argumentList\" : {\n" +
                    "            \"type\" : \"ArgumentList\",\n" +
                    "            \"arguments\" : [ {\n" +
                    "              \"type\" : \"Argument\",\n" +
                    "              \"body\" : {\n" +
                    "                \"type\" : \"StringTemplateExpression\",\n" +
                    "                \"name\" : null,\n" +
                    "                \"body\" : {\n" +
                    "                  \"type\" : \"StringTemplateEntry\",\n" +
                    "                  \"body\" : null,\n" +
                    "                  \"value\" : \"这个一个最小试验单位的动态化卡片，它的产物来自云端打包编译，可以根据产品诉求随意替换\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            } ]\n" +
                    "          },\n" +
                    "          \"valueArgument\" : [ ]\n" +
                    "        } ]\n" +
                    "      },\n" +
                    "      \"isStatic\" : false,\n" +
                    "      \"isGetter\" : true,\n" +
                    "      \"isSetter\" : false\n" +
                    "    } ],\n" +
                    "    \"body\" : { }\n" +
                    "  } ],\n" +
                    "  \"type\" : \"CompilationUnit\"\n" +
                    "}"
            
            val localFile = AstRuntime.LocalJson(json)
            val node = localFile.createNode()

            resultFlow.value = TemplateResult(
                programNode = node,
                sourceKey = "asset_$assetName"
            )
        } catch (e: Exception) {
            throw e
        }

        return resultFlow as Flow<TemplateResult>
    }

    fun loadFromFile(file: File): Flow<TemplateResult> {
        val resultFlow = MutableStateFlow<TemplateResult?>(null)

        try {
            val localFile = AstRuntime.LocalFile(file)
            val node = localFile.createNode()

            resultFlow.value = TemplateResult(
                programNode = node,
                sourceKey = "file_${file.absolutePath}"
            )
        } catch (e: Exception) {
            throw e
        }

        return resultFlow as Flow<TemplateResult>
    }

    fun loadFromZipFile(zipFile: File): Flow<TemplateResult> {
        val resultFlow = MutableStateFlow<TemplateResult?>(null)

        try {
            val extractedFile = extractMainFileFromZip(zipFile)
            val localFile = AstRuntime.LocalFile(extractedFile)
            val node = localFile.createNode()

            resultFlow.value = TemplateResult(
                programNode = node,
                sourceKey = "zip_${zipFile.absolutePath}"
            )
        } catch (e: Exception) {
            throw e
        }

        return resultFlow as Flow<TemplateResult>
    }

    private fun extractMainFileFromZip(zipFile: File): File {
        ZipFile(zipFile).use { zip ->
            val entry = zip.entries().asSequence().firstOrNull {
                it.name.endsWith(".kt") && !it.isDirectory
            } ?: throw IllegalArgumentException("No Kotlin files found in zip")

            val extractedFile = File.createTempFile("extracted", ".kt")
            zip.getInputStream(entry).use { input ->
                extractedFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            return extractedFile
        }
    }
}

/**
 * Holder for Composable content
 */
class ComposableHolder {
    var componentDescriptor: ComposeComponentDescriptor? = null
}

/**
 * Context provider for CloudComposable
 */
object ComposeContext {
    private val contextMap = mutableMapOf<String, Any>()

    fun <T> run(name: String, body: () -> T, overrides: Map<Any, () -> Any?> = emptyMap()): T {
        val savedContext = HashMap(contextMap)

        try {
            overrides.forEach { (key, provider) ->
                contextMap[key.toString()] = provider() ?: Any()
            }

            return body()
        } finally {
            contextMap.clear()
            contextMap.putAll(savedContext)
        }
    }

    fun <T> get(key: Class<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return contextMap[key.toString()] as? T
    }
}

/**
 * View model for CloudComposable to manage state
 */
class CloudComposeViewModel : ViewModel() {
    var templateResult by mutableStateOf<TemplateResult?>(null)
    var arguments by mutableStateOf<Arguments?>(null)
    var error by mutableStateOf<Throwable?>(null)
    var astRuntime by mutableStateOf<AstRuntime?>(null)
    var composableCache by mutableStateOf<(@Composable () -> Unit)?>(null)

    fun loadTemplate(
        template: TemplateProvider,
        arguments: Arguments?,
        errorCallback: ((Throwable, Array<StackTraceElement>) -> Unit)?,
        eventCallback: ((EasyComposeEvent) -> Unit)?
    ) {
        viewModelScope.launch {
            try {
                template.resolve()
                    .catch { e ->
                        error = e
                        eventCallback?.invoke(EasyComposeEvent.LOAD_ERROR)
                        errorCallback?.invoke(e, e.stackTrace)
                    }
                    .collect { result ->
                        templateResult = result
                        this@CloudComposeViewModel.arguments = arguments
                        error = null
                        composableCache = null

                        astRuntime = AstRuntime(result.programNode)
                        eventCallback?.invoke(EasyComposeEvent.LOAD_COMPLETE)
                    }
            } catch (e: Exception) {
                error = e
                eventCallback?.invoke(EasyComposeEvent.LOAD_ERROR)
                errorCallback?.invoke(e, e.stackTrace)
            }
        }
    }

    fun updateArguments(arguments: Arguments?) {
        if (templateResult != null) {
            this.arguments = arguments
            composableCache = null
        }
    }
}

/**
 * Type for loading builder
 */
typealias TemplateLoadingBuilder = @Composable () -> Unit

/**
 * Type for error builder
 */
typealias TemplateErrorWidgetBuilder = @Composable (Throwable, Array<StackTraceElement>?) -> Unit

/**
 * Main Composable function for dynamic UI
 */
@Composable
fun CloudComposableFunction(
    template: TemplateProvider,
    arguments: Arguments? = null,
    loadingBuilder: TemplateLoadingBuilder? = null,
    errorBuilder: TemplateErrorWidgetBuilder? = null,
    errorCallback: ((Throwable, Array<StackTraceElement>) -> Unit)? = null,
    eventCallback: ((EasyComposeEvent) -> Unit)? = null,
    viewModel: CloudComposeViewModel = remember { CloudComposeViewModel() }
) {
    // Load template when provider changes
    LaunchedEffect(template) {
        viewModel.loadTemplate(template, arguments, errorCallback, eventCallback)
    }

    // Update arguments when they change
    LaunchedEffect(arguments) {
        viewModel.updateArguments(arguments)
    }

    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            // Cleanup resources
        }
    }

    // Render appropriate content
    when {
        viewModel.error != null -> {
            if (errorBuilder != null) {
                errorBuilder(viewModel.error!!, viewModel.error!!.stackTrace)
            } else {
                DefaultErrorContent(viewModel.error!!)
            }
        }

        viewModel.templateResult == null -> {
            if (loadingBuilder != null) {
                loadingBuilder()
            } else {
                DefaultLoadingContent()
            }
        }

        else -> {
            if (viewModel.composableCache == null) {
                viewModel.composableCache = createDynamicComposable(
                    viewModel.astRuntime!!,
                    viewModel.arguments
                )
            }

            // Use Column instead of Box to avoid BoxScope issues
            Text(
                text = "viewModel.composableCache?.invoke()...",
                modifier = Modifier.padding(top = 16.dp)
            )
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colors.background
            ) {

                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(onClick = {}) {
                        Text("Click me!")
                    }
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .height(100.dp)
                                .fillMaxWidth()
                                .padding(16.dp) // 设置外部边缘（padding）
                                .clip(RoundedCornerShape(10.dp)) // 设置圆角半径为10dp
                                .background(Color(0xFFD1EAFB)), // 设置背景颜色
                            contentAlignment = Alignment.Center // 将 Text 垂直和水平居中
                        ) {
                            viewModel.composableCache?.invoke()
                        }
                    }
                }
            }
        }
    }
}

/**
 * Create a composable from AstRuntime
 */
private fun createDynamicComposable(
    runtime: AstRuntime,
    arguments: Arguments?
): @Composable () -> Unit {
    return {
        val holder = ComposableHolder()
        var descriptorState by remember { mutableStateOf<ComposeComponentDescriptor?>(null) }

        // Create or retrieve the dynamic composable
        LaunchedEffect(runtime, arguments) {
            context.run(
                name = "CloudComposable",
                body = {
                    if (runtime.hasTopLevelFunction("Main")) {
                        // Execute main function if it exists
                        Log.d("CloudComposable", "Executing main function")
                        val invoke = runtime.invoke2(
                            "Main",
                            arguments?.positionalArguments ?: emptyList(),
                            arguments?.namedArguments
                        )
                        // Get the holder from context
                        // 获取holder
                        val composableHolder = context.get(ComposableHolder::class.java)
                        if (composableHolder != null && composableHolder.componentDescriptor != null) {
                            holder.componentDescriptor = composableHolder.componentDescriptor
                        } else {
                            // 创建描述符而不是直接创建Composable函数
                            holder.componentDescriptor = ComposeComponentDescriptor(
                                "androidx.compose.material.Text",
                                null
                            )
                        }
                    } else {
                        // Look for composable classes
                        // 查找Composable类
                        Log.d("CloudComposable", "Looking for composable classes")
                        var foundComposable = false

                        for (astClass in runtime.classes) {
                            if (astClass.superclass?.simpleName == "\$ProxyComposable" ||
                                astClass.superclass?.simpleName == "\$ComposableWidget"
                            ) {
                                Log.d(
                                    "CloudComposable",
                                    "Found composable class: ${astClass.simpleName}"
                                )

                                // 创建描述符
                                holder.componentDescriptor = ComposeComponentDescriptor(
                                    "androidx.compose.material.Text",
                                    null
                                )

                                foundComposable = true
                                break
                            }
                        }

                        if (!foundComposable) {
                            // 创建描述符
                            holder.componentDescriptor = ComposeComponentDescriptor(
                                "androidx.compose.material.Text",
                                null
                            )
                        }
                    }

                    // 然后在更新时
                    if (holder.componentDescriptor != null) {
                        descriptorState = holder.componentDescriptor!!
                    }
                },
                overrides = mapOf(
                    ComposableHolder::class.java to { holder }
                )
            )
        }

        // Render the composable
        if (descriptorState != null) {
            // 使用工厂方法渲染组件
            RenderComponent(descriptorState!!)
        } else {
            // 默认加载显示
            Text("Loading composable content...")
        }
    }
}

/**
 * Check if runtime has top-level function
 */
fun AstRuntime.hasTopLevelFunction(name: String): Boolean {
    return invoke2(name) != null
}

/**
 * Default loading content
 */
@Composable
private fun DefaultLoadingContent() {
    Text(
        text = "Loading template...",
        modifier = Modifier.padding(top = 16.dp)
    )
}

//
///**
// * Default error content
// */
@Composable
private fun DefaultErrorContent(error: Throwable) {
    // Use a simple Text as a fallback
    Text(
        text = "Error loading template: ${error.message}",
        color = Color.Red,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}

/**
 * Object to host CloudComposable extension functions
 */
object CloudComposable {
    /**
     * Convenience function for network templates
     */
    @Composable
    fun Network(
        url: String,
        fileMd5: String? = null,
        arguments: Arguments? = null,
        loadingBuilder: TemplateLoadingBuilder? = null,
        errorBuilder: TemplateErrorWidgetBuilder? = null,
        errorCallback: ((Throwable, Array<StackTraceElement>) -> Unit)? = null,
        eventCallback: ((EasyComposeEvent) -> Unit)? = null
    ) {
        CloudComposableFunction(
            template = NetworkTemplate(url, fileMd5),
            arguments = arguments,
            loadingBuilder = loadingBuilder,
            errorBuilder = errorBuilder,
            errorCallback = errorCallback,
            eventCallback = eventCallback
        )
    }

    /**
     * Convenience function for asset templates
     */
    @Composable
    fun Asset(
        assetName: String,
        arguments: Arguments? = null,
        loadingBuilder: TemplateLoadingBuilder? = null,
        errorBuilder: TemplateErrorWidgetBuilder? = null,
        errorCallback: ((Throwable, Array<StackTraceElement>) -> Unit)? = null,
        eventCallback: ((EasyComposeEvent) -> Unit)? = null
    ) {
        CloudComposableFunction(
            template = AssetTemplate(assetName),
            arguments = arguments,
            loadingBuilder = loadingBuilder,
            errorBuilder = errorBuilder,
            errorCallback = errorCallback,
            eventCallback = eventCallback
        )
    }

    /**
     * Convenience function for file templates
     */
    @Composable
    fun File(
        file: File,
        arguments: Arguments? = null,
        loadingBuilder: TemplateLoadingBuilder? = null,
        errorBuilder: TemplateErrorWidgetBuilder? = null,
        errorCallback: ((Throwable, Array<StackTraceElement>) -> Unit)? = null,
        eventCallback: ((EasyComposeEvent) -> Unit)? = null
    ) {
        CloudComposableFunction(
            template = FileTemplate(file),
            arguments = arguments,
            loadingBuilder = loadingBuilder,
            errorBuilder = errorBuilder,
            errorCallback = errorCallback,
            eventCallback = eventCallback
        )
    }

    /**
     * Convenience function for zip file templates
     */
    @Composable
    fun ZipFile(
        zipFile: File,
        arguments: Arguments? = null,
        loadingBuilder: TemplateLoadingBuilder? = null,
        errorBuilder: TemplateErrorWidgetBuilder? = null,
        errorCallback: ((Throwable, Array<StackTraceElement>) -> Unit)? = null,
        eventCallback: ((EasyComposeEvent) -> Unit)? = null
    ) {
        CloudComposableFunction(
            template = ZipFileTemplate(zipFile),
            arguments = arguments,
            loadingBuilder = loadingBuilder,
            errorBuilder = errorBuilder,
            errorCallback = errorCallback,
            eventCallback = eventCallback
        )
    }
}

/**
 * Example usage
 */
@Composable
fun ExampleUsage() {
//    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
//        Button(onClick = { }) {
//            Text("Click me!")
//        }
//        Column(
//            Modifier.fillMaxWidth(),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Text("这是一个KMP加载的动态化的View")
//        }
//    }
//    MaterialTheme {
//        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
//            Button(onClick = { }) {
//                Text("Click me!")
//            }
//            AnimatedVisibility(true) {
//                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
//                    Text("这是一个KMP加载的动态化的View")
//                }
//            }
//        }
//    }
    val testFile = File("src/test/java/com/aether/core/compose/DemoCompose.kt")
    CloudComposable.Asset(
        assetName = "mytest",
        arguments = Arguments(
            positionalArguments = listOf(5, "a"),
            namedArguments = mapOf("name" to "value")
        ),
        eventCallback = { event ->
            when (event) {
                EasyComposeEvent.LOAD_COMPLETE -> Log.d("Example", "Template loaded")
                EasyComposeEvent.LOAD_ERROR -> Log.d("Example", "Template failed to load")
            }
        }
    )
}