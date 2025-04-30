package com.aether.core.express

import com.aether.core.runtime.AstRuntime
import org.jline.utils.Log
import org.junit.Test
import java.io.File

class InstanceCreationExpressionTest {

    @Test
    fun mainTest() {
        val targetDirectory =
            File("src/test/java/com/aether/core/express/DemoFunctionExpress.kt")
        val localFile = AstRuntime.LocalFile(targetDirectory)
        val node = localFile.createNode()
        val runtime = AstRuntime(node);
        val test2 = runtime.invoke("test2", mutableListOf(5,"a"))
        System.out.println("动态化引擎输出结果：" + test2)
    }
}