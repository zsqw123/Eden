package com.zsu.eden

import org.jetbrains.kotlin.psi.KtDeclaration

open class EdenAnnotatedChange(annotationShortName: String, private val tracker: EdenModificationTracker) : SimpleAnnotatedChange(annotationShortName) {
    override fun onAnnotatedElementChange(declaration: KtDeclaration) {
        tracker.incModificationCount()
    }
}