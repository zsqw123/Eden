package com.zsu.eden.dsl

import com.intellij.psi.PsiType
import org.intellij.lang.annotations.Language

class FakeMethod(name: String) : FakeElement(name) {
    var isStatic = false
    var isPublic = true // false -> private
    private val typeParams = TypeParams() // first extends second
    private val params = arrayListOf<Pair<String, String>>() // name to type
    var returnType: PsiType? = null
    private var returnTypeText: String? = null

    fun typeParam(
        typeParam: String, extends: String? = null,
    ) {
        typeParams.add(typeParam to extends)
    }

    fun typeParam(
        typeParam: String, extends: PsiType,
    ) {
        typeParams.add(typeParam to extends.canonicalText)
    }

    fun textParam(vararg param: Pair<String, String>) {
        params.addAll(param)
    }

    fun param(vararg param: Pair<String, PsiType>) {
        params.addAll(param.map { it.first to it.second.getCanonicalText(true) })
    }

    fun returnType(returnJavaTypeText: String) {
        returnTypeText = returnJavaTypeText
    }

    override fun toString(): String = raw ?: buildString {
        append(if (isPublic) "public " else "private ")
        append(if (isStatic) "static " else "")
        append(typeParams.asString())

        append(' ')
        val returnTypeText = returnTypeText
        if (returnTypeText != null) {
            append(returnTypeText)
        } else {
            val returnType = returnType
            if (returnType == null) {
                append("void")
            } else {
                append(returnType.canonicalText)
            }
        }

        append(' ')
        append("$name(")
        append(params.joinToString { "${it.second} ${it.first}" })
        append(") { throw new Exception(); }")
    }

    companion object {
        fun fromJavaText(@Language("java") javaText: String): FakeMethod {
            return FakeMethod(STUB_NAME).apply { raw = javaText }
        }
    }
}
