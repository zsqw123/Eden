package com.zsu.eden

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import java.io.File

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
    abstract fun processSingleModule(all: List<KtNamedDeclaration>): List<EdenFile>

    open fun getGeneratePath(module: Module): VirtualFile? = null
    open fun checkEnable(module: Module): Boolean = true
    open val kspVariant: String = "main"

    companion object {
        @Suppress("MemberVisibilityCanBePrivate")
        val EP_NAME: ExtensionPointName<EdenApt> =
            ExtensionPointName.create("com.zsu.eden.edenApt")

        fun getAll(): Array<EdenApt> = EP_NAME.extensions
    }
}

interface EdenFile {
    val packageName: String
    val name: String // without `.kt`

    /** @param ioFile sourceRoot of this file */
    fun writeTo(ioFile: File)

    abstract class Impl(
        override val packageName: String,
        override val name: String,
    ) : EdenFile {
        abstract fun content(): String
        override fun writeTo(ioFile: File) {
            val packageName = packageName
            var outputDirectory = ioFile.toPath()
            if (packageName.isNotEmpty()) {
                for (packageComponent in packageName.split('.')
                    .dropLastWhile { it.isEmpty() }) {
                    outputDirectory = outputDirectory.resolve(packageComponent)
                }
            }
            val outputFolder = outputDirectory.toFile()
            if (!outputFolder.exists()) outputFolder.mkdirs()
            File(outputFolder, "$name.kt").writeText(content())
        }
    }
}
