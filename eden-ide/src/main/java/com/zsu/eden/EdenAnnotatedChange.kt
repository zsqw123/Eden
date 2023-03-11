package com.zsu.eden

import com.intellij.openapi.util.SimpleModificationTracker
import org.jetbrains.kotlin.psi.KtDeclaration

open class EdenAnnotatedChange(
    annotationShortNames: Collection<String>,
    private val tracker: SimpleModificationTracker,
) : SimpleAnnotatedChange(annotationShortNames) {
    override fun onAnnotatedElementChange(declaration: KtDeclaration) {
        tracker.incModificationCount()
    }
}
