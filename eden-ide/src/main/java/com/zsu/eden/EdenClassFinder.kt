package com.zsu.eden

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElementFinder
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiPackage
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.zsu.eden.util.allChildClasses
import com.zsu.eden.util.packageName

open class EdenClassFinder(
    private val edenCache: EdenCache,
    private val modificationTracker: EdenModificationTracker
) : PsiElementFinder() {
    private val classCached: CachedValue<Collection<PsiClass>>
    private val fqnClassCached: CachedValue<Map<String, PsiClass>>
    private val packageCached: CachedValue<Map<String, Collection<PsiClass>>>

    init {
        val cachedValuesManager = CachedValuesManager.getManager(edenCache.project)
        classCached = cachedValuesManager.createCachedValue {
            val classes = edenCache.getClasses().toList()
            val allChildren = classes.flatMap { it.allChildClasses() }
            CachedValueProvider.Result.create(classes + allChildren, modificationTracker)
        }
        fqnClassCached = cachedValuesManager.createCachedValue {
            val map = classCached.value.associateBy { it.qualifiedName ?: FAKE_STUB }
            CachedValueProvider.Result.create(map, modificationTracker)
        }
        packageCached = cachedValuesManager.createCachedValue {
            val map = classCached.value.groupBy { it.packageName ?: "" }
            CachedValueProvider.Result.create(map, modificationTracker)
        }
        PsiManager.getInstance(edenCache.project)
            .addPsiTreeChangeListener(ChangeListener(), edenCache.project)
    }

    inner class ChangeListener : EdenAnnotatedChange(
        edenCache.annotationFqn.substringAfterLast('.'), modificationTracker
    )

    override fun findClass(qualifiedName: String, scope: GlobalSearchScope): PsiClass? {
        return fqnClassCached.value[qualifiedName]
    }

    override fun findClasses(qualifiedName: String, scope: GlobalSearchScope): Array<PsiClass> {
        val c = findClass(qualifiedName, scope) ?: return PsiClass.EMPTY_ARRAY
        return arrayOf(c)
    }

    // for `*` import
    override fun getClasses(psiPackage: PsiPackage, scope: GlobalSearchScope): Array<PsiClass> {
        return packageCached.value[psiPackage.qualifiedName]?.toTypedArray() ?: PsiClass.EMPTY_ARRAY
    }
}
