package com.zsu.eden

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
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

abstract class EdenApt {
    abstract val annotationFqn: String
    abstract fun processSingleModule(all: List<KtNamedDeclaration>): List<FileSpec>

    open fun getGeneratePath(module: Module): VirtualFile? = null
    open fun checkEnable(module: Module): Boolean = true
    open val variant: String = "main"

    companion object {
        @Suppress("MemberVisibilityCanBePrivate")
        val EP_NAME: ExtensionPointName<EdenApt> =
            ExtensionPointName.create("com.zsu.eden.edenApt")

        fun getAll(): Array<EdenApt> = EP_NAME.extensions
    }
}
