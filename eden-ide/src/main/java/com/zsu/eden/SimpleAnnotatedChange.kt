package com.zsu.eden

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiTreeChangeAdapter
import com.intellij.psi.PsiTreeChangeEvent
import com.intellij.psi.util.parentsOfType
import org.jetbrains.kotlin.psi.KtDeclaration

abstract class SimpleAnnotatedChange(private val annotationShortName: String) : PsiTreeChangeAdapter() {
    override fun childAdded(event: PsiTreeChangeEvent) {
        onChange(event.child)
    }

    override fun childRemoved(event: PsiTreeChangeEvent) {
        onChange(event.child)
    }

    override fun childReplaced(event: PsiTreeChangeEvent) {
        onChange(event.newChild)
    }

    override fun childMoved(event: PsiTreeChangeEvent) {
        onChange(event.child)
    }

    override fun childrenChanged(event: PsiTreeChangeEvent) {
        onChange(event.parent)
    }

    override fun propertyChanged(event: PsiTreeChangeEvent) {
        onChange(event.element)
    }

    private fun onChange(element: PsiElement?) {
        element ?: return
        // Checking its 2 parent nodes, not checking too much here, because of performance loss concerns
        for (ktDeclaration in element.parentsOfType<KtDeclaration>().take(2)) {
            val annotations = ktDeclaration.annotationEntries
            if (annotations.isEmpty()) continue
            if (annotations.any { it.shortName?.asString() == annotationShortName }) {
                onAnnotatedElementChange(ktDeclaration)
                break
            }
        }
    }

    protected abstract fun onAnnotatedElementChange(declaration: KtDeclaration)
}