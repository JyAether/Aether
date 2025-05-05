package com.aether.core.express

import com.aether.core.runtime.AstRuntime
import org.junit.Test
import java.io.File

class ColumExpressComposeTest {

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
            File("src/test/java/com/aether/core/compose/ColumComposeNameExpressText.kt")
        val json = AstRuntime.GeneraJson(targetDirectory).createNodeToJson()
        System.out.println("生成的dsl：" + json)
    }

    companion object {
        fun getDefaultJson(): String {
            return ""
        }
    }
}