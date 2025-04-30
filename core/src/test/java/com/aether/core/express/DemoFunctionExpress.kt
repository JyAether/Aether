package com.aether.core.express

import com.aether.core.express.DemoTest
import com.aether.core.express.InstanceTest
import org.jline.utils.Display
import org.jline.utils.Log

object DemoFunctionExpress {
    /**
     * result =10
     * result2 =10
     * index = 5
     */
    fun test2(index: Int, item: String): Int {
        val result = DemoTest.demo1Test1()
        val result2 = DemoTest.demo1Test2()
        Log.info(1,23,456)
        val instanceTest = InstanceTest(18)
        instanceTest.triggerTest()
        val a = 2
        val b = 2 + index
        return a * b + result + result2
    }
}