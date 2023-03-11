package com.zsu.eden.extensions

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElementFinder
import com.intellij.psi.PsiPackage
import com.intellij.psi.search.GlobalSearchScope
import com.zsu.eden.EdenModuleCache

@Service
class EdenClassFinder(project: Project) : PsiElementFinder() {
    private val edenModuleCache = EdenModuleCache.getInstance(project)
    private fun tryRefresh(scope: GlobalSearchScope) {
        edenModuleCache.tryLoadCache(scope)
    }

    override fun findClass(qualifiedName: String, scope: GlobalSearchScope): PsiClass? {
        tryRefresh(scope)
        return null
    }

    override fun findClasses(qualifiedName: String, scope: GlobalSearchScope): Array<PsiClass> {
        findClass(qualifiedName, scope)
        return PsiClass.EMPTY_ARRAY
    }

    // for `*` import
    override fun getClasses(psiPackage: PsiPackage, scope: GlobalSearchScope): Array<PsiClass> {
        tryRefresh(scope)
        return PsiClass.EMPTY_ARRAY
    }
}
