package com.zsu.eden

import com.intellij.psi.PsiClass
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager

internal const val FAKE_STUB = "FAKE_STUB"

open class EdenClassNamesCache(edenClassCache: EdenCache, private val modificationTracker: EdenModificationTracker) :
    ShortClassNamesCache() {
    private val edenClassCachedValue: CachedValue<Map<String, Collection<PsiClass>>>

    init {
        val cachedValuesManager = CachedValuesManager.getManager(edenClassCache.project)
        edenClassCachedValue = cachedValuesManager.createCachedValue {
            val classes = edenClassCache.getClasses()
            val classShortNameMap = classes.groupBy { it.name ?: FAKE_STUB }
            CachedValueProvider.Result.create(classShortNameMap, modificationTracker)
        }
    }

    override fun getClassesByName(name: String, scope: GlobalSearchScope): Array<PsiClass> {
        modificationTracker.incModificationCount()
        return edenClassCachedValue.value[name]?.toTypedArray() ?: PsiClass.EMPTY_ARRAY
    }

    override fun getAllClassNames(): Array<String> {
        return edenClassCachedValue.value.keys.toTypedArray()
    }
}