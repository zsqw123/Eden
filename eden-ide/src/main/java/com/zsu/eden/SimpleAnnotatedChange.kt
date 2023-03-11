package com.zsu.eden

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiTreeChangeAdapter
import com.intellij.psi.PsiTreeChangeEvent
import com.intellij.psi.util.parentsOfType
import org.jetbrains.kotlin.psi.KtDeclaration

abstract class SimpleAnnotatedChange(private val shortNames: Collection<String>) :
    PsiTreeChangeAdapter() {
    final override fun childAdded(event: PsiTreeChangeEvent) {
        onChange(event.child)
    }

    final override fun childRemoved(event: PsiTreeChangeEvent) {
        onChange(event.child)
    }

    final override fun childReplaced(event: PsiTreeChangeEvent) {
        onChange(event.newChild)
    }

    final override fun childMoved(event: PsiTreeChangeEvent) {
        onChange(event.child)
    }

    final override fun childrenChanged(event: PsiTreeChangeEvent) {
        onChange(event.parent)
    }

    final override fun propertyChanged(event: PsiTreeChangeEvent) {
        onChange(event.element)
    }

    private fun onChange(element: PsiElement?) {
        element ?: return
        // Checking its 3 parent nodes, not checking too much here, because of performance loss concerns
        for (ktDeclaration in element.parentsOfType<KtDeclaration>().take(3)) {
            val annotations = ktDeclaration.annotationEntries
            if (annotations.isEmpty()) continue
            val hasChange = annotations.any { currentAnnotation ->
                shortNames.any { shortNames ->
                    currentAnnotation.shortName?.asString() == shortNames
                }
            }
            if (hasChange) {
                onAnnotatedElementChange(ktDeclaration)
                break
            }
        }
    }

    protected abstract fun onAnnotatedElementChange(declaration: KtDeclaration)
}
