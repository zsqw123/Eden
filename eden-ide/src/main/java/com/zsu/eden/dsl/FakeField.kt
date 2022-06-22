package com.zsu.eden.dsl

class FakeField(
    name: String, private val type: String,
    private val isFinal: Boolean = true,
    private val isPublic: Boolean = true,// false -> private
    private val isStatic: Boolean = false
) : FakeElement(name) {
    override fun toString(): String = buildString {
        append(if (isFinal) "final " else "")
        append(if (isPublic) "public " else "private ")
        append(if (isStatic) "static " else "")
        append(type)
        append(' ')
        append(name)
        append(';')
    }
}