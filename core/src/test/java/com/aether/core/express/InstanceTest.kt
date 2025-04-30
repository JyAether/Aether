package com.aether.core.express

import org.jline.utils.Log

class InstanceTest {
    private var age = 18;
    private val name = "zhangsan";
    private val TAG = "InstanceTest";

    companion object {
        private val name2 = "wangwu";
    }

    constructor(age: Int) {

    }

    fun triggerTest(): Int {
        Log.info(TAG, ": wode :" + triggerTest2(2))
        return age
    }

    fun triggerTest2(index: Int): Int {
        return index + 1
    }
}