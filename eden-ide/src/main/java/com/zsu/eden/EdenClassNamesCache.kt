package com.zsu.eden

import com.intellij.psi.PsiClass
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValue
import org.jetbrains.kotlin.idea.util.cachedValue

internal const val FAKE_STUB = "__FAKE_STUB"

open class EdenClassNamesCache(edenClassCache: EdenCache) :
    ShortClassNamesCache() {
    private val modificationTracker = edenClassCache.modificationTracker

    private val edenClassCachedValue: CachedValue<Map<String, Collection<PsiClass>>> =
        cachedValue(edenClassCache.project, modificationTracker) {
            val classes = edenClassCache.generatedClasses.value
            val classShortNameMap = classes.groupBy { it.name ?: FAKE_STUB }
            classShortNameMap
        }

    override fun getClassesByName(name: String, scope: GlobalSearchScope): Array<PsiClass> {
//        modificationTracker.incModificationCount()
        return edenClassCachedValue.value[name]?.toTypedArray() ?: PsiClass.EMPTY_ARRAY
    }

    override fun getAllClassNames(): Array<String> {
        return edenClassCachedValue.value.keys.toTypedArray()
    }
}
