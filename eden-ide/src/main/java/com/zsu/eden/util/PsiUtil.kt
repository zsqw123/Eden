package com.zsu.eden.util

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiType
import com.intellij.psi.util.PsiTypesUtil
import org.jetbrains.kotlin.asJava.elements.FakeFileForLightClass
import org.jetbrains.kotlin.idea.core.getPackage
import java.util.*

val PsiType.packageName: String?
    get() = typeClass?.packageName

val PsiClass.classType: PsiType
    get() = PsiTypesUtil.getClassType(this)

val PsiType.typeClass: PsiClass?
    get() = PsiTypesUtil.getPsiClass(this)

val PsiElement.packageName: String?
    get() = containingFile.containingDirectory?.getPackage()?.qualifiedName

val PsiClass.ktFakePackageName: String?
    get() {
        val file = containingFile as? FakeFileForLightClass ?: return null
        return file.packageName
    }

val PsiFile.packageName: String?
    get() = containingDirectory?.getPackage()?.qualifiedName

fun PsiClass.allChildClasses(): Collection<PsiClass> {
    val childClasses = innerClasses
    if (childClasses.isEmpty()) return listOf()
    val res = LinkedList<PsiClass>()
    res.addAll(childClasses)
    for (child in childClasses) {
        res.addAll(child.allChildClasses())
    }
    return res
}