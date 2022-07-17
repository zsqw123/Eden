package com.zsu.eden

import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.util.CachedValue
import com.zsu.eden.dsl.FakeFile
import org.jetbrains.kotlin.idea.util.cachedValue
import org.jetbrains.uast.UDeclaration

internal val edenClassGotoKey = Key.create<Array<PsiElement>>("EDEN_CLASS_GOTO")

abstract class EdenCache(
    internal val project: Project,
    internal val modificationTracker: EdenModificationTracker,
    internal val annotationFqn: String
) {
    abstract fun processAnnotation(annotations: List<UDeclaration>): List<FakeFile>
    internal var isProcessing = false

    private val cachedAnnotatedElements: CachedValue<List<UDeclaration>> =
        cachedValue(project, modificationTracker) {
            synchronized(this) {
                isProcessing = true
                EdenSearch.getAnnotatedElements(project, annotationFqn).also {
                    isProcessing = false
                }
            }
        }

    private var lastGenerated: List<PsiClass> = listOf()

    @Synchronized
    private fun tryRegenerate() = runCatching {
        val annotations = cachedAnnotatedElements.value
        isProcessing = true
        val fakeFiles = runReadAction {
            processAnnotation(annotations)
        }
        isProcessing = false
        val javaFiles: List<PsiJavaFile> = fakeFiles.mapNotNull {
            runReadAction { it.toJavaFile(project) }
        }
        val psiClasses = javaFiles.flatMap { it.classes.toList() }
        lastGenerated = psiClasses
    }

    internal val generatedClasses: CachedValue<List<PsiClass>> =
        cachedValue(project, modificationTracker) {
            tryRegenerate()
            lastGenerated
        }
}
