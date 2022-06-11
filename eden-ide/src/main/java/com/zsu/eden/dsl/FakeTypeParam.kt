package com.zsu.eden.dsl

class FakeTypeParam(
    name: String,
    var extends: String? = null,
    var supers: String? = null,
) : FakeElement(name) {
    override fun toString(): String = when {
        extends != null -> java("$name extends $extends")
        supers != null -> java("$name super $supers")
        else -> name
    }
}