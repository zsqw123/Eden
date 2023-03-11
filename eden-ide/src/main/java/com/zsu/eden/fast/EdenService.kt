package com.zsu.eden.fast

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.squareup.kotlinpoet.FileSpec
import org.jetbrains.kotlin.psi.KtNamedDeclaration

@Service
class EdenService(private val project: Project) {
    internal val allApt = HashMap<String, EdenApt>()
    fun addApt(edenApt: EdenApt) {
        allApt[edenApt.annotationFqn] = edenApt
    }

    init {
        EdenApt.getAll().forEach {
            addApt(it)
        }
    }

    companion object {
        fun getInstance(project: Project) = project.service<EdenService>()
    }
}

interface EdenApt {
    val annotationFqn: String
    fun processSingleModule(all: List<KtNamedDeclaration>): List<FileSpec>

    companion object {
        @Suppress("MemberVisibilityCanBePrivate")
        val EP_NAME: ExtensionPointName<EdenApt> =
            ExtensionPointName.create("com.zsu.eden.edenApt")

        fun getAll(): Array<EdenApt> = EP_NAME.extensions
    }
}
