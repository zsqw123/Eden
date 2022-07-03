package com.zsu.eden.dsl

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiJavaFile
import org.jetbrains.kotlin.idea.util.module
import org.jetbrains.kotlin.psi.KtPsiFactory

class FakeClass(name: String, var packageName: String? = null) : FakeElement(name) {
    private val imports = arrayListOf<String>()
    var isStatic = false
    var isPublic = true // false -> private
    private val typeParams = TypeParams() // first extends second
    private var extends: String? = null
    private val implements = arrayListOf<String>()
    val constructors = arrayListOf<FakeConstructor>()
    val methods = arrayListOf<FakeMethod>()
    val fields = arrayListOf<FakeField>()
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

    inline fun constructor(body: FakeConstructor.() -> Unit = {}) {
        constructors.add(FakeConstructor(name).apply(body))
    }

    inline fun method(methodName: String, body: FakeMethod.() -> Unit = {}) {
        methods.add(FakeMethod(methodName).apply(body))
    }

    inline fun field(
        fieldName: String, type: String,
        isFinal: Boolean = true,
        isPublic: Boolean = true, isStatic: Boolean = false,
        action: FakeField.() -> Unit = {},
    ) {
        fields.add(FakeField(fieldName, type, isFinal, isPublic, isStatic).apply(action))
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
        if (constructors.isNotEmpty()) {
            val constructorFields = constructors
                .flatMap { it.properties }
                .filter { it.isField }
                .distinctBy { it.name }
            if (constructorFields.isNotEmpty()) {
                append(constructorFields.joinToString("\n") { it.toFakeField().toString() })
                append('\n')
            }
            append(constructors.joinToString("\n"))
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

    fun toKtClass(context: PsiElement): PsiClass? {
        val curText = """
            package com.aaa
            class $name
        """.trimIndent()
        if (psiText == curText) return psiCache
        else psiText = curText
        val ktFile = KtPsiFactory(context.project).createFileWithLightClassSupport(
            "$name.kt", curText, context
        )
//        ApplicationManager.getApplication().invokeLaterOnWriteThread {
//            val module = context.module ?: return@invokeLaterOnWriteThread
//            ModuleRootModificationUtil.addContentRoot(module, "/")
//        }
        return ktFile.classes.firstOrNull().also { psiCache = it }
    }
}