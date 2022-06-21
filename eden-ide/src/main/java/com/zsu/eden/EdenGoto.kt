package com.zsu.eden

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiJavaCodeReferenceElement
import com.intellij.psi.util.parentOfType
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.nj2k.postProcessing.resolve
import org.jetbrains.kotlin.psi.KtReferenceExpression

class EdenGoto : GotoDeclarationHandler {
    override fun getGotoDeclarationTargets(sourceElement: PsiElement?, offset: Int, editor: Editor?): Array<PsiElement>? {
        sourceElement ?: return null
        val res = runReadAction {
            return@runReadAction when (sourceElement.language) {
                is KotlinLanguage -> {
                    val ktExpression = sourceElement.parentOfType<KtReferenceExpression>(true)
                    val resolvedClass = ktExpression?.resolve()
                    resolvedClass?.getUserData(edenClassGotoKey)
                }
                is JavaLanguage -> {
                    val resolvedClass = sourceElement.parentOfType<PsiJavaCodeReferenceElement>()?.resolve()
                    resolvedClass?.getUserData(edenClassGotoKey)
                }
                else -> null
            }
        }
        return res
    }
}