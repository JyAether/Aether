package com.aether.core.runtime.reflectable

//Mirror 接口作为父接口存在。
interface Mirror {
}

 interface ObjectMirror : Mirror {

    /**
     * 调用函数或方法 [memberName]，返回结果。
     *
     * @param memberName 方法名
     * @param positionalArguments 位置参数列表
     * @param namedArguments 命名参数映射（Symbol -> value）
     *
     * 实现逻辑应模拟 o.memberName(positionalArguments..., k1 = v1, ...)
     * 并支持访问私有成员。
     */
    fun invoke(
        memberName: String,
        positionalArguments: List<Any?>,
        namedArguments: Map<String, Any?>? = null
    ): Any?

    /**
     * 调用 getter 方法并返回结果。
     *
     * 如果是实例镜像且 [getterName] 是方法，则返回闭包。
     * 如果是库镜像且 [getterName] 是顶层方法，则返回闭包。
     * 如果是类镜像且 [getterName] 是静态方法，则返回闭包。
     */
    fun invokeGetter(getterName: String): Any?

    /**
     * 调用 setter 方法并返回结果。
     *
     * setter 名称可以带 `=`，也可以不带，内部会自动处理。
     */
    fun invokeSetter(setterName: String, value: Any?): Any?
}