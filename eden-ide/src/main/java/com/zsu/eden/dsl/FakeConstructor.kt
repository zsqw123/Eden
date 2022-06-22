package com.zsu.eden.dsl

class FakeConstructor(name: String) : FakeElement(name) {
    internal val properties = arrayListOf<FakeProperty>()
    var isPublic = true // false -> private
    fun property(
        name: String, type: String,
        isField: Boolean = false,
        isFinal: Boolean = true,
        isPublic: Boolean = true,
    ) {
        properties.add(FakeProperty(name, type, isField, isFinal, isPublic))
    }

    override fun toString(): String = buildString {
        append(if (isPublic) "public " else "private ")
        append("$name(")
        append(properties.joinToString())
        append("){ throw new Exception(); }")
    }
}