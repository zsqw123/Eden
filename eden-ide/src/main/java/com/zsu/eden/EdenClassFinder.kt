package com.zsu.eden

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElementFinder
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiPackage
import com.intellij.psi.search.GlobalSearchScope
import com.zsu.eden.util.allChildClasses
import com.zsu.eden.util.packageName

open class EdenClassFinder(
    private val edenCache: EdenCache
) : PsiElementFinder() {
    private var classCached: Collection<PsiClass> = listOf()
    private var fqnClassCached: Map<String, PsiClass> = mapOf()
    private var packageCached: Map<String, Collection<PsiClass>> = mapOf()
    private val modificationTracker = edenCache.modificationTracker
    private var lastModified = -1L

    init {
        PsiManager.getInstance(edenCache.project)
            .addPsiTreeChangeListener(ChangeListener(), edenCache.project)
    }

    private fun refreshCache() {
        if (edenCache.isProcessing) return
        val classes = edenCache.generatedClasses.value
        val allChildren = classes.flatMap { it.allChildClasses() }
        classCached = classes + allChildren
        fqnClassCached = classCached.associateBy { it.qualifiedName ?: FAKE_STUB }
        packageCached = classCached.groupBy { it.packageName ?: "" }
    }

    @Synchronized
    private fun tryRefresh() {
        if (lastModified == modificationTracker.modificationCount) return
        refreshCache()
        lastModified = modificationTracker.modificationCount
    }

    inner class ChangeListener : EdenAnnotatedChange(
        edenCache.annotationFqn.substringAfterLast('.'), modificationTracker,
    )

    override fun findClass(qualifiedName: String, scope: GlobalSearchScope): PsiClass? {
        tryRefresh()
        return fqnClassCached[qualifiedName]
    }

    override fun findClasses(qualifiedName: String, scope: GlobalSearchScope): Array<PsiClass> {
        val c = findClass(qualifiedName, scope) ?: return PsiClass.EMPTY_ARRAY
        return arrayOf(c)
    }

    // for `*` import
    override fun getClasses(psiPackage: PsiPackage, scope: GlobalSearchScope): Array<PsiClass> {
        tryRefresh()
        return packageCached[psiPackage.qualifiedName]?.toTypedArray() ?: PsiClass.EMPTY_ARRAY
    }
}
