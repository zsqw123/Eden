package com.zsu.eden.extensions

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.search.GlobalSearchScope
import com.zsu.eden.EdenModuleCache

private val stubArray = arrayOf("__FAKE_STUB")

@Service
class EdenClassNamesCache(project: Project) : ShortClassNamesCache() {
    private val edenModuleCache = EdenModuleCache.getInstance(project)
    override fun getAllClassNames(): Array<String> = stubArray
    override fun getClassesByName(name: String, scope: GlobalSearchScope): Array<PsiClass> {
        edenModuleCache.tryLoadCache(scope)
        return PsiClass.EMPTY_ARRAY
    }
}
