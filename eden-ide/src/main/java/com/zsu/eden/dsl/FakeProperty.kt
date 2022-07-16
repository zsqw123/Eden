package com.zsu.eden.dsl

internal class FakeProperty(
    name: String,
    private val javaTypeText: String,
    internal val isField: Boolean,
    private val isFinalField: Boolean,
    private val isPublicField: Boolean,
) : FakeElement(name) {
    override fun toString(): String = raw ?: "$javaTypeText $name"
    internal fun toFakeField() = FakeField(name).also {
        it.type(javaTypeText)
        it.isFinal = isFinalField
        it.isPublic = isPublicField
    }
}
