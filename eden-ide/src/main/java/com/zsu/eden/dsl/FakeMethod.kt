package com.zsu.eden.dsl

class FakeMethod(name: String) : FakeElement(name) {
    var isStatic = false
    var isPublic = true // false -> private
    private val typeParams = TypeParams() // first extends second
    private val params = arrayListOf<Pair<String, String>>() // name to type
    var returnType: String? = null

    fun typeParam(
        typeParam: String,
        extends: String? = null,
    ) {
        typeParams.add(typeParam to extends)
    }

    fun param(vararg param: Pair<String, String>) {
        params.addAll(param)
    }

    override fun toString(): String = buildString {
        append(if (isPublic) "public " else "private ")
        append(if (isStatic) "static " else "")
        append(typeParams.asString())
        append(' ')
        if (returnType == null) {
            append("void")
        } else {
            append(returnType)
        }
        append(' ')
        append("$name(")
        append(params.joinToString { "${it.second} ${it.first}" })
        append("){ throw new Exception(); }")
    }
}