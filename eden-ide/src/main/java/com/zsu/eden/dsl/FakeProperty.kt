package com.zsu.eden.dsl

class FakeProperty(
    name: String,
    private val type: String,
    internal val isField: Boolean,
    private val isFinalField: Boolean,
    private val isPublicField: Boolean,
) : FakeElement(name) {
    override fun toString(): String = "$type $name"
    internal fun toFakeField() = FakeField(name, type, isFinalField, isPublicField)
}