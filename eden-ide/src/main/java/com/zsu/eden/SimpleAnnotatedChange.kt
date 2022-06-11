package com.zsu.eden

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiTreeChangeAdapter
import com.intellij.psi.PsiTreeChangeEvent
import org.jetbrains.kotlin.psi.KtDeclaration

abstract class SimpleAnnotatedChange(private val annotationShortName: String) : PsiTreeChangeAdapter() {
    override fun childAdded(event: PsiTreeChangeEvent) {
        onChange(event.parent)
    }

    override fun childRemoved(event: PsiTreeChangeEvent) {
        onChange(event.parent)
    }

    override fun childReplaced(event: PsiTreeChangeEvent) {
        onChange(event.parent)
    }

    override fun childMoved(event: PsiTreeChangeEvent) {
        onChange(event.newParent)
    }

    override fun childrenChanged(event: PsiTreeChangeEvent) {
        onChange(event.parent)
    }

    override fun propertyChanged(event: PsiTreeChangeEvent) {
        onChange(event.element)
    }

    private fun onChange(element: PsiElement?) {
        if (element !is KtDeclaration) return
        val annotations = element.annotationEntries
        if (annotations.isEmpty()) return
        if (annotations.any { it.shortName?.asString() == annotationShortName }) {
            onAnnotatedElementChange(element)
        }
    }

    protected abstract fun onAnnotatedElementChange(declaration: KtDeclaration)
}