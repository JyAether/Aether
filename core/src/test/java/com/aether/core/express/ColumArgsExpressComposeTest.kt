package com.aether.core.express

import com.aether.core.runtime.AstRuntime
import org.junit.Test
import java.io.File

class ColumArgsExpressComposeTest {

    @Test
    fun generateJson() {
        val targetDirectory =
            File("src/test/java/com/aether/core/compose/ColumArgsComposeExpressText.kt")
        val json = AstRuntime.GeneraJson(targetDirectory).createNodeToJson()
        System.out.println("生成的dsl：" + json)
    }
}