package com.zsu.eden

import com.intellij.openapi.module.Module
import com.intellij.psi.JavaPsiFacade
import org.jetbrains.kotlin.idea.search.ideaExtensions.KotlinAnnotatedElementsSearcher
import org.jetbrains.kotlin.psi.KtNamedDeclaration

object EdenSearch {
    fun getAnnotatedElements(
        module: Module, annotationFqn: String,
    ): List<KtNamedDeclaration> = buildList {
        val annotationClass = JavaPsiFacade.getInstance(module.project).findClass(
            annotationFqn,
            module.getModuleWithDependenciesAndLibrariesScope(true),
        ) ?: return@buildList

        // in IDEA 2023 (223), it has been changed with:
        // org.jetbrains.kotlin.idea.base.searching.KotlinAnnotatedElementsSearcher.
        KotlinAnnotatedElementsSearcher.processAnnotatedMembers(
            annotationClass,
            module.moduleScope,
        ) { declaration ->
            if (declaration is KtNamedDeclaration) add(declaration)
            true
        }
    }
}

