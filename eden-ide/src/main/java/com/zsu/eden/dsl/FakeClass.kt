package com.zsu.eden.dsl

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiJavaFile

class FakeClass(name: String, var packageName: String? = null) : FakeElement(name) {
    private val imports = arrayListOf<String>()
    var isStatic = false
    var isPublic = true // false -> private
    private val typeParams = TypeParams() // first extends second
    private var extends: String? = null
    private val implements = arrayListOf<String>()
    val methods = arrayListOf<FakeMethod>()
    private val fields = arrayListOf<FakeField>()
    val classes = arrayListOf<FakeClass>()

    fun imports(vararg body: String) {
        imports.addAll(body)
    }

    fun extends(parent: String) {
        extends = parent
    }

    fun implements(vararg interfaces: String) {
        implements.addAll(interfaces)
    }

    fun typeParam(
        typeParam: String,
        extends: String? = null,
    ) {
        typeParams.add(typeParam to extends)
    }

    inline fun method(methodName: String, body: FakeMethod.() -> Unit = {}) {
        methods.add(FakeMethod(methodName).apply(body))
    }

    fun field(
        fieldName: String, type: String,
        isPublic: Boolean = true, isStatic: Boolean = false,
    ) {
        fields.add(FakeField(fieldName, type, isPublic, isStatic))
    }

    inline fun clazz(className: String, body: FakeClass.() -> Unit = {}) {
        classes.add(FakeClass(className, packageName).apply(body))
    }

    override fun toString(): String = buildString {
        if (imports.isNotEmpty()) {
            val importList = imports.joinToString("\n") {
                "import $it;"
            }
            append(importList)
            append('\n')
        }
        append(if (isPublic) "public " else "private ")
        append(if (isStatic) "static " else "")
        append("class ")
        append(name)
        append(typeParams.asString())
        append(' ')
        extends?.let {
            append("extends $it ")
        }
        if (implements.isNotEmpty()) {
            append("implements ${implements.joinToString()} ")
        }
        append("{\n")
        if (fields.isNotEmpty()) {
            append(fields.joinToString("\n"))
            append('\n')
        }
        if (methods.isNotEmpty()) {
            append(methods.joinToString("\n"))
            append('\n')
        }
        if (classes.isNotEmpty()) {
            append(classes.joinToString("\n"))
        }
        append("\n}")
    }

    private var psiText: String? = null
    private var psiCache: PsiClass? = null

    fun toPsiClass(project: Project): PsiClass? {
        val javaFactory = PsiFileFactory.getInstance(project)
        val fileText = buildString {
            packageName?.let {
                if (it.isBlank()) return@let
                append("package ")
                append(it)
                append(";\n")
            }
            append(this@FakeClass.toString())
        }
        if (fileText == psiText) return psiCache
        psiText = fileText
        val psiFile = javaFactory.createFileFromText("$name.java", JavaFileType.INSTANCE, fileText)
        val javaFile = (psiFile as? PsiJavaFile) ?: return null
        return javaFile.classes.firstOrNull().also { psiCache = it }
    }
}