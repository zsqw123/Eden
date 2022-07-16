package com.zsu.eden.dsl

import com.intellij.psi.PsiElement
import org.intellij.lang.annotations.Language

@FakeElementDsl
abstract class FakeElement(
    val name: String
) {
    internal open var raw: String? = null

    @Language("java")
    abstract override fun toString(): String

    /** 当使用 command 点击元素时跳转到的位置 */
    open var navigateTo: PsiElement? = null

    protected fun TypeParams.asString() = buildString {
        if (this@asString.isNotEmpty()) {
            append('<')
            append(joinToString {
                val second = it.second
                if (second == null) it.first
                else "${it.first} extends ${it.second}"
            })
            append('>')
        }
    }
}

internal fun java(@Language("java") string: String) = string


@DslMarker
annotation class FakeElementDsl

typealias TypeParams = ArrayList<Pair<String, String?>>