package com.zsu.eden.dsl

import com.intellij.psi.PsiType
import org.intellij.lang.annotations.Language

class FakeField(
    name: String,
) : FakeElement(name) {
    var isStatic = false
    var isPublic = true // false -> private
    var type: PsiType? = null
    var isFinal: Boolean = true
    private var typeText: String? = null
    fun type(javaTypeText: String? = null) {
        typeText = javaTypeText
    }

    override fun toString(): String = raw ?: buildString {
        append(if (isFinal) "final " else "")
        append(if (isPublic) "public " else "private ")
        append(if (isStatic) "static " else "")
        append(typeText ?: type?.getCanonicalText(true) ?: "Object")
        append(' ')
        append(name)
        append(';')
    }

    companion object {
        fun fromJavaText(@Language("java") javaText: String): FakeField {
            return FakeField(STUB_NAME).apply { raw = javaText }
        }
    }
}