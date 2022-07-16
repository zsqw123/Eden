package com.zsu.eden.dsl

import org.intellij.lang.annotations.Language

object Eden {
    inline fun fakeFile(
        name: String, packageName: String = "",
        action: FakeFile.() -> Unit = {}
    ): FakeFile {
        return FakeFile(name, packageName).apply(action)
    }

    fun fakeFile(@Language("java") rawJavaText: String) = FakeFile.fromJavaText(rawJavaText)

    inline fun fakeClassFile(
        name: String,
        packageName: String = "",
        action: FakeClass.() -> Unit = {}
    ): FakeFile = fakeFile(name, packageName) {
        clazz(name, action)
    }
}
