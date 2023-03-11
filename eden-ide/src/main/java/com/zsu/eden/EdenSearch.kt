package com.zsu.eden

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.descendantsOfType
import org.jetbrains.kotlin.idea.stubindex.KotlinAnnotationsIndex
import org.jetbrains.kotlin.idea.stubindex.KotlinSourceFilterScope
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtNamedDeclaration

object EdenSearch {
    private fun getAnnotation(
        project: Project, annotationFQN: String, scope: GlobalSearchScope
    ): PsiClass? {
        return JavaPsiFacade.getInstance(project).findClass(annotationFQN, scope)
    }

    fun getAnnotatedElements(
        module: Module, annotationFqn: String,
    ): List<KtNamedDeclaration> = buildList {
        search(module.project, annotationFqn, module) {
            add(it)
        }
    }

    // Copied some from com.android.tools.idea.dagger.DaggerAnnotatedElementsSearch
    private inline fun search(
        project: Project, annotationFQN: String, module: Module,
        kotlinProcessor: (KtNamedDeclaration) -> Unit,
    ) {
        val annotationClass = getAnnotation(
            project, annotationFQN,
            module.getModuleWithDependenciesAndLibrariesScope(false),
        ) ?: return
        val candidates = getKotlinAnnotationCandidates(annotationClass, module.moduleScope)
        candidates.filterIsInstance<KtAnnotationEntry>().forEach { annotation ->
            val declaration =
                PsiTreeUtil.getParentOfType(annotation, KtNamedDeclaration::class.java)
                    ?: return@forEach
            kotlinProcessor(declaration)
        }
    }

    // Copied from KotlinAnnotatedElementsSearcher#getKotlinAnnotationCandidates
    private fun getKotlinAnnotationCandidates(annClass: PsiClass, useScope: SearchScope): Collection<PsiElement> {
        if (useScope is GlobalSearchScope) {
            val name = annClass.name ?: return emptyList()
            val scope = KotlinSourceFilterScope.sourcesAndLibraries(useScope, annClass.project)
            return KotlinAnnotationsIndex.getInstance().get(name, annClass.project, scope)
        }
        return (useScope as LocalSearchScope).scope.flatMap { it.descendantsOfType<KtAnnotationEntry>() }
    }
}

