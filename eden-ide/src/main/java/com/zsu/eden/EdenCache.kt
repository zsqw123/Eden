package com.zsu.eden

import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiJavaFile
import com.zsu.eden.dsl.FakeFile
import org.jetbrains.uast.UDeclaration

internal val edenClassGotoKey = Key.create<Array<PsiElement>>("EDEN_CLASS_GOTO")

abstract class EdenCache(internal val project: Project, internal val annotationFqn: String) {
    abstract fun processAnnotation(annotations: List<UDeclaration>): List<FakeFile>

    fun getClasses(): List<PsiClass> {
        val annotations = EdenSearch.getAnnotatedElements(project, annotationFqn)
        val fakeFiles = runReadAction {
            processAnnotation(annotations)
        }
        val javaFiles: List<PsiJavaFile> = fakeFiles.mapNotNull {
            runReadAction { it.toJavaFile(project) }
        }
        return javaFiles.flatMap { it.classes.toList() }
    }
}
