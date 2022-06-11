package com.zsu.eden.dsl

object Eden {
    inline fun fakeClass(name: String, packageName: String? = null, action: FakeClass.() -> Unit = {}): FakeClass {
        return FakeClass(name, packageName).apply(action)
    }
}