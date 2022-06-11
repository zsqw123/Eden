package com.zsu.eden.util

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiPrimitiveType
import com.intellij.psi.PsiType
import com.intellij.psi.util.PsiTypesUtil
import org.jetbrains.kotlin.idea.core.getPackage
import org.jetbrains.kotlin.idea.highlighter.markers.collectContainingClasses
import org.jetbrains.kotlin.psi.KtElement

val PsiType.packageName: String?
    get() = typeClass?.packageName

val PsiClass.classType: PsiType
    get() = PsiTypesUtil.getClassType(this)

val PsiType.typeClass: PsiClass?
    get() = PsiTypesUtil.getPsiClass(this)

val PsiElement.packageName: String?
    get() = containingFile.containingDirectory?.getPackage()?.qualifiedName

val PsiFile.packageName: String?
    get() = containingDirectory?.getPackage()?.qualifiedName

fun PsiClass.allChildClasses(): Collection<PsiClass> {
    val childClasses = allInnerClasses
    if (childClasses.isEmpty()) return emptyList()
    val res = childClasses.toMutableList()
    for (child in childClasses) {
        res.addAll(child.allChildClasses())
    }
    return res
}