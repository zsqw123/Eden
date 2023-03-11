package com.zsu.eden

import com.intellij.psi.PsiField
import com.intellij.psi.PsiMethod
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiShortNamesCache
import com.intellij.util.Processor

abstract class ShortClassNamesCache : PsiShortNamesCache() {
    private val emptyArray = emptyArray<String>()
    override fun getAllMethodNames(): Array<String> = emptyArray
    override fun getAllFieldNames(): Array<String> = emptyArray
    override fun getMethodsByName(name: String, scope: GlobalSearchScope): Array<PsiMethod> = PsiMethod.EMPTY_ARRAY
    override fun getMethodsByNameIfNotMoreThan(name: String, scope: GlobalSearchScope, maxCount: Int): Array<PsiMethod> =
        PsiMethod.EMPTY_ARRAY

    override fun processMethodsWithName(name: String, scope: GlobalSearchScope, processor: Processor<in PsiMethod>): Boolean = true
    override fun getFieldsByNameIfNotMoreThan(name: String, scope: GlobalSearchScope, maxCount: Int): Array<PsiField> = PsiField.EMPTY_ARRAY
    override fun getFieldsByName(name: String, scope: GlobalSearchScope): Array<PsiField> = PsiField.EMPTY_ARRAY
}
