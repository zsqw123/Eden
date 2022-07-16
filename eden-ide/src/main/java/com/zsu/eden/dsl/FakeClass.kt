package com.zsu.eden.dsl

import org.intellij.lang.annotations.Language

class FakeClass(name: String) : FakeElement(name) {
    var isStatic = false
    var isPublic = true // false -> private
    private val typeParams = TypeParams() // first extends second
    private var extends: String? = null
    private val implements = arrayListOf<String>()

    @PublishedApi
    internal val constructors = arrayListOf<FakeConstructor>()

    @PublishedApi
    internal val methods = arrayListOf<FakeMethod>()

    @PublishedApi
    internal val fields = arrayListOf<FakeField>()

    @PublishedApi
    internal val classes = arrayListOf<FakeClass>()


    fun extends(parent: String) {
        extends = parent
    }

    fun implements(vararg interfaces: String) {
        implements.addAll(interfaces)
    }

    fun typeParam(
        typeParam: String,
        extends: String? = null,
    ) {
        typeParams.add(typeParam to extends)
    }

    inline fun constructor(body: FakeConstructor.() -> Unit = {}) {
        constructors.add(FakeConstructor(name).apply(body))
    }

    inline fun method(methodName: String, body: FakeMethod.() -> Unit = {}) {
        methods.add(FakeMethod(methodName).apply(body))
    }

    inline fun field(
        fieldName: String, action: FakeField.() -> Unit = {},
    ) {
        fields.add(FakeField(fieldName).apply(action))
    }

    inline fun clazz(className: String, body: FakeClass.() -> Unit = {}) {
        classes.add(FakeClass(className).apply(body))
    }

    override fun toString(): String = raw ?: buildString {
        append(if (isPublic) "public " else "private ")
        append(if (isStatic) "static " else "")
        append("class ")
        append(name)
        append(typeParams.asString())
        append(' ')
        extends?.let {
            append("extends $it ")
        }
        if (implements.isNotEmpty()) {
            append("implements ${implements.joinToString()} ")
        }
        append("{\n")
        if (fields.isNotEmpty()) {
            append(fields.joinToString("\n"))
            append('\n')
        }
        if (constructors.isNotEmpty()) {
            val constructorFields = constructors
                .flatMap { it.properties }
                .filter { it.isField }
                .distinctBy { it.name }
            if (constructorFields.isNotEmpty()) {
                append(constructorFields.joinToString("\n") { it.toFakeField().toString() })
                append('\n')
            }
            append(constructors.joinToString("\n"))
            append('\n')
        }
        if (methods.isNotEmpty()) {
            append(methods.joinToString("\n"))
            append('\n')
        }
        if (classes.isNotEmpty()) {
            append(classes.joinToString("\n"))
            append('\n')
        }
        append("}")
    }

    companion object {
        fun fromJavaText(@Language("java") javaText: String): FakeClass {
            return FakeClass(STUB_NAME).apply { raw = javaText }
        }
    }
}