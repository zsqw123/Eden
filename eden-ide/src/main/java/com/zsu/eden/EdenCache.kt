package com.zsu.eden

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.zsu.eden.dsl.FakeClass
import org.jetbrains.kotlin.psi.KtDeclaration

abstract class EdenCache(internal val project: Project, internal val annotationFqn: String) {
    abstract fun processAnnotation(annotations: Sequence<KtDeclaration>): Sequence<FakeClass>

    fun getClasses(): Sequence<PsiClass> {
        val annotations = EdenSearch.getAnnotatedElements(project, annotationFqn)
        val classes = processAnnotation(annotations)
        return classes.mapNotNull { it.toPsiClass(project) }
    }
}