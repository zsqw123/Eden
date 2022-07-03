package com.zsu.eden

import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.zsu.eden.dsl.FakeClass
import com.zsu.eden.dsl.FakeField
import com.zsu.eden.dsl.FakeMethod
import org.jetbrains.kotlin.psi.KtDeclaration

internal val edenClassGotoKey = Key.create<Array<PsiElement>>("EDEN_CLASS_GOTO")

abstract class EdenCache(internal val project: Project, internal val annotationFqn: String) {
    abstract fun processAnnotation(annotations: Sequence<KtDeclaration>): Sequence<FakeClass>

    fun getClasses(): Sequence<PsiClass> {
        val annotations = EdenSearch.getAnnotatedElements(project, annotationFqn)
        val classes = runReadAction {
            processAnnotation(annotations)
        }
        val single = annotations.firstOrNull() ?: return emptySequence()
        return classes.mapNotNull {
//            it.genPsiClass(project)
            it.toKtClass(single)
        }
    }

    private fun FakeClass.genPsiClass(project: Project): PsiClass? {
        val psiClass = toPsiClass(project) ?: return null
        val classGoto = navigateTo?.let { arrayOf(it) } ?: emptyArray()
        psiClass.putUserData(edenClassGotoKey, classGoto)
        val methods = methods
        if (methods.isNotEmpty()) {
            val methodGotoMap = methods
                .groupBy(FakeMethod::name)
                .mapValues {
                    it.value.mapNotNull(FakeMethod::navigateTo).toTypedArray()
                }
            for (psiMethod in psiClass.methods) {
                var gotoTaget = methodGotoMap[psiMethod.name]
                if (gotoTaget.isNullOrEmpty()) gotoTaget = classGoto
                psiMethod.putUserData(edenClassGotoKey, gotoTaget)
            }
        }

        val fields = fields
        if (fields.isNotEmpty()) {
            val fieldGotoMap = fields
                .groupBy(FakeField::name)
                .mapValues {
                    it.value.mapNotNull(FakeField::navigateTo).toTypedArray()
                }
            for (psiField in psiClass.fields) {
                var gotoTaget = fieldGotoMap[psiField.name]
                if (gotoTaget.isNullOrEmpty()) gotoTaget = classGoto
                psiField.putUserData(edenClassGotoKey, gotoTaget)
            }
        }

        return psiClass
    }
}