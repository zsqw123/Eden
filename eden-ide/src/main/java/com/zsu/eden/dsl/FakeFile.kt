package com.zsu.eden.dsl

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiJavaFile
import com.zsu.eden.edenClassGotoKey
import org.intellij.lang.annotations.Language

internal const val STUB_NAME = "stub"

class FakeFile(name: String, private var packageName: String = "") : FakeElement(name) {
    private val imports = arrayListOf<String>()

    @PublishedApi
    internal val classes = arrayListOf<FakeClass>()

    fun imports(vararg body: String) {
        imports.addAll(body)
    }

    inline fun clazz(className: String, body: FakeClass.() -> Unit = {}) {
        classes.add(FakeClass(className).apply(body))
    }

    fun clazzText(@Language("java") javaText: String) {
        classes.add(FakeClass.fromJavaText(javaText))
    }

    private var psiText: String? = null
    private var psiCache: PsiJavaFile? = null
    fun toJavaFile(project: Project): PsiJavaFile? {
        val javaFactory = PsiFileFactory.getInstance(project)
        val fileText = toString()
        if (fileText == psiText) return psiCache
        psiText = fileText
        val psiFile = javaFactory.createFileFromText(
            "$name.java", JavaFileType.INSTANCE, fileText,
        ) as? PsiJavaFile ?: return null
        for (psiClass in psiFile.classes) {
            val fakeClass = this.classes.find { it.name == psiClass.name }
            fakeClass?.attachGoto(psiClass)
        }
        return psiFile
    }

    private fun FakeClass.attachGoto(psiClass: PsiClass): PsiClass {
        val classGoto = this.navigateTo?.let { arrayOf(it) } ?: emptyArray()
        psiClass.putUserData(edenClassGotoKey, classGoto)
        val methods = this.methods
        if (methods.isNotEmpty()) {
            val methodGotoMap = methods
                .groupBy(FakeMethod::name)
                .mapValues {
                    it.value.mapNotNull(FakeMethod::navigateTo).toTypedArray()
                }
            for (psiMethod in psiClass.methods) {
                var gotoTaget = methodGotoMap[psiMethod.name]
                if (gotoTaget.isNullOrEmpty()) gotoTaget = classGoto
                psiMethod.putUserData(edenClassGotoKey, gotoTaget)
            }
        }

        val fields = this.fields
        if (fields.isNotEmpty()) {
            val fieldGotoMap = fields
                .groupBy(FakeField::name)
                .mapValues {
                    it.value.mapNotNull(FakeField::navigateTo).toTypedArray()
                }
            for (psiField in psiClass.fields) {
                var gotoTaget = fieldGotoMap[psiField.name]
                if (gotoTaget.isNullOrEmpty()) gotoTaget = classGoto
                psiField.putUserData(edenClassGotoKey, gotoTaget)
            }
        }
        return psiClass
    }

    override fun toString(): String = raw ?: buildString {
        if (packageName.isNotEmpty()) {
            append("package ")
            append(packageName)
            append(";\n")
        }
        if (imports.isNotEmpty()) {
            val importList = imports.joinToString("\n") {
                "import $it;"
            }
            append(importList)
            append('\n')
        }
        if (classes.isNotEmpty()) {
            append(classes.joinToString("\n"))
        }
    }

    companion object {
        fun fromJavaText(@Language("java") javaText: String): FakeFile {
            return FakeFile(STUB_NAME).apply { raw = javaText }
        }
    }
}
