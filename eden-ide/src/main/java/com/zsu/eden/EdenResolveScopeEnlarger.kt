package com.zsu.eden

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import org.jetbrains.kotlin.idea.caches.resolve.util.KotlinResolveScopeEnlarger
import org.jetbrains.kotlin.idea.search.fileScope

//class BindingScopeEnlarger(private val edenCache: EdenCache) : ResolveScopeEnlarger() {
//    override fun getAdditionalResolveScope(file: VirtualFile, project: Project): SearchScope? {
//        return edenCache.getEdenScope()
//    }
//}

private fun EdenCache.getEdenScope(module: Module): GlobalSearchScope {
    return CachedValuesManager.getManager(project).getCachedValue(module) {
        val classes = getClasses().mapNotNull { it.containingFile }.toList()
        val scopes = classes.map { it.fileScope() }
        val scope = scopes.fold(GlobalSearchScope.EMPTY_SCOPE) { acc, new -> acc.union(new) }
        CachedValueProvider.Result.create(scope, PsiModificationTracker.EVER_CHANGED)
    }
}

abstract class EdenResolveScopeEnlarger(private val edenCacheSupplier: (Project) -> EdenCache) : KotlinResolveScopeEnlarger() {
    override fun getAdditionalResolveScope(module: Module, isTestScope: Boolean): SearchScope? {
        if (isTestScope) return null
        return edenCacheSupplier(module.project).getEdenScope(module)
    }
}