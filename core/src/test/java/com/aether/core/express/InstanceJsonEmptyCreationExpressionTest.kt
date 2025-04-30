package com.aether.core.express

import com.aether.core.runtime.AstRuntime
import org.junit.Test

class InstanceJsonEmptyCreationExpressionTest {

    @Test
    fun mainTest() {
        val localFile = AstRuntime.LocalJson(getDefaultJson())
        val node = localFile.createNode()
        val runtime = AstRuntime(node);
        val test2 = runtime.invoke("test2", mutableListOf(5,"a"))
        System.out.println("动态化引擎输出结果：" + test2)
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
}