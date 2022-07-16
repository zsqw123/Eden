package com.zsu.eden.dsl

import com.intellij.psi.PsiType
import org.intellij.lang.annotations.Language

class FakeConstructor(name: String) : FakeElement(name) {
    internal val properties = arrayListOf<FakeProperty>()
    var isPublic = true // false -> private
    fun property(
        name: String, javaTypeText: String,
        isField: Boolean = false,
        isFinalField: Boolean = true,
        isPublicField: Boolean = true,
    ) {
        properties += FakeProperty(name, javaTypeText, isField, isFinalField, isPublicField)
    }

    fun property(
        name: String, type: PsiType,
        isField: Boolean = false,
        isFinalField: Boolean = true,
        isPublicField: Boolean = true,
    ) {
        properties += FakeProperty(
            name, type.getCanonicalText(true),
            isField, isFinalField, isPublicField,
        )
    }

    override fun toString(): String = raw ?: buildString {
        append(if (isPublic) "public " else "private ")
        append("$name(")
        append(properties.joinToString())
        append("){ throw new Exception(); }")
    }

    companion object {
        fun fromJavaText(@Language("java") javaText: String): FakeConstructor {
            return FakeConstructor(STUB_NAME).apply { raw = javaText }
        }
    }
}
