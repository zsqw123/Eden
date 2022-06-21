package com.zsu.eden

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.zsu.eden.dsl.FakeClass
import org.jetbrains.kotlin.psi.KtDeclaration

internal val edenClassGotoKey = Key.create<PsiElement>("EDEN_CLASS_GOTO")

abstract class EdenCache(internal val project: Project, internal val annotationFqn: String) {
    abstract fun processAnnotation(annotations: Sequence<KtDeclaration>): Sequence<FakeClass>

    fun getClasses(): Sequence<PsiClass> {
        val annotations = EdenSearch.getAnnotatedElements(project, annotationFqn)
        val classes = processAnnotation(annotations)
        return classes.mapNotNull { it.genPsiClass(project) }
    }

    private fun FakeClass.genPsiClass(project: Project): PsiClass? {
        val psiClass = toPsiClass(project) ?: return null
        goto?.let { psiClass.putUserData(edenClassGotoKey, it) }
        return psiClass
    }
}