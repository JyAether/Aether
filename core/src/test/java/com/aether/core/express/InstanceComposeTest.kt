package com.aether.core.express

import com.aether.core.runtime.AstRuntime
import org.jline.utils.Log
import org.junit.Test
import java.io.File

class InstanceComposeTest {

    @Test
    fun mainTest() {
        val localFile = AstRuntime.LocalJson(getDefaultJson())
        val node = localFile.createNode()
        val runtime = AstRuntime(node);
        val test2 = runtime.invoke("test2", mutableListOf(5, "a"))
        System.out.println("动态化引擎输出结果：" + test2)
    }


    @Test
    fun generateJson() {
        val targetDirectory =
            File("src/test/java/com/aether/core/compose/DemoCompose.kt")
        val json = AstRuntime.GeneraJson(targetDirectory).createNodeToJson()
        System.out.println("生成的dsl：" + json)
    }

    companion object {
        fun getDefaultJson(): String {
            return "{\n" +
                    "  \"directives\" : [ {\n" +
                    "    \"type\" : \"ImportDirective\",\n" +
                    "    \"importPath\" : \"androidx.compose.foundation.layout.Column\",\n" +
                    "    \"alias\" : null,\n" +
                    "    \"name\" : \"Column\"\n" +
                    "  }, {\n" +
                    "    \"type\" : \"ImportDirective\",\n" +
                    "    \"importPath\" : \"androidx.compose.foundation.layout.padding\",\n" +
                    "    \"alias\" : null,\n" +
                    "    \"name\" : \"padding\"\n" +
                    "  }, {\n" +
                    "    \"type\" : \"ImportDirective\",\n" +
                    "    \"importPath\" : \"androidx.compose.material.Text\",\n" +
                    "    \"alias\" : null,\n" +
                    "    \"name\" : \"Text\"\n" +
                    "  }, {\n" +
                    "    \"type\" : \"ImportDirective\",\n" +
                    "    \"importPath\" : \"androidx.compose.runtime.Composable\",\n" +
                    "    \"alias\" : null,\n" +
                    "    \"name\" : \"Composable\"\n" +
                    "  }, {\n" +
                    "    \"type\" : \"ImportDirective\",\n" +
                    "    \"importPath\" : \"androidx.compose.ui.Modifier\",\n" +
                    "    \"alias\" : null,\n" +
                    "    \"name\" : \"Modifier\"\n" +
                    "  }, {\n" +
                    "    \"type\" : \"ImportDirective\",\n" +
                    "    \"importPath\" : \"androidx.compose.ui.unit.dp\",\n" +
                    "    \"alias\" : null,\n" +
                    "    \"name\" : \"dp\"\n" +
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
                    "                  \"value\" : \"Asset content from demo\"\n" +
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
        }
    }
}