package com.zsu.eden

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.rootManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.squareup.kotlinpoet.FileSpec
import org.jetbrains.jps.model.java.JavaSourceRootType
import org.jetbrains.jps.model.module.JpsModuleSourceRootType
import org.jetbrains.kotlin.config.SourceKotlinRootType
import org.jetbrains.kotlin.idea.configuration.externalProjectPath
import org.jetbrains.kotlin.idea.util.application.runReadAction
import java.io.File
import java.util.concurrent.Executors

internal class ModuleAnnotatedHolder(
    private val module: Module,
    private val annotationFqn: String,
) {
    private val edenService = EdenService.getInstance(module.project)
    private var generated: List<File> = emptyList()
    fun create(): Boolean {
        val singleApt = edenService.allApt[annotationFqn]
        if (singleApt == null) {
            logger.warn("[$annotationFqn] apt didn't exists when scan module [${module.name}]")
            return false
        }
        val allFiles = runReadAction {
            val all = EdenSearch.getAnnotatedElements(module, annotationFqn)
            singleApt.processSingleModule(all)
        }
        executor.execute {
            generated.forEach { if (it.exists()) it.delete() }
            createFiles(module, allFiles, singleApt) {
                generated = it
            }
        }
        return true
    }

    companion object {
        private val logger = Logger.getInstance(ModuleAnnotatedHolder::class.java)
        private val kotlinSourceRoots: MutableSet<JpsModuleSourceRootType<*>> =
            hashSetOf(JavaSourceRootType.SOURCE, SourceKotlinRootType)

        private val executor = Executors.newSingleThreadExecutor()
        private inline fun createFiles(
            module: Module, allFiles: List<FileSpec>, apt: EdenApt,
            addedCallback: (added: List<File>) -> Unit,
        ) {
            if (allFiles.isEmpty()) return
            val sourceRoot = apt.getGeneratePath(module)
                ?: module.rootManager.getSourceRoots(kotlinSourceRoots).firstOrNull()
            if (sourceRoot == null) {
                logger.warn("no kotlin/java source root found in [${module.name}]!")
                return
            }
            val ioFile = VfsUtilCore.virtualToIoFile(sourceRoot)
            val added = arrayListOf<File>()
            for (file in allFiles) {
                file.writeTo(ioFile)
                var outputDirectory = ioFile.toPath()
                if (file.packageName.isNotEmpty()) {
                    for (packageComponent in file.packageName.split('.')
                        .dropLastWhile { it.isEmpty() }) {
                        outputDirectory = outputDirectory.resolve(packageComponent)
                    }
                }
                val outputPath = outputDirectory.resolve("${file.name}.kt")
                added += outputPath.toFile()
            }
            addedCallback(added)
            sourceRoot.refresh(true, true)
        }

        private fun guessKspPath(apt: EdenApt, module: Module): VirtualFile? {
            val sourceRoot = apt.getGeneratePath(module)
            if (sourceRoot != null) return sourceRoot

            val extProjectPath = module.externalProjectPath
            if (extProjectPath != null) {
                val file = File(extProjectPath, "build/generated/ksp/${apt.variant}/kotlin")
                if (!file.exists()) file.mkdirs()
                return LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file)
            }
            return module.rootManager.getSourceRoots(kotlinSourceRoots).firstOrNull()
        }
    }
}
