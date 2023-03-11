package com.zsu.eden

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.rootManager
import com.intellij.openapi.vfs.VfsUtilCore
import com.squareup.kotlinpoet.FileSpec
import org.jetbrains.jps.model.java.JavaSourceRootType
import org.jetbrains.jps.model.module.JpsModuleSourceRootType
import org.jetbrains.kotlin.config.SourceKotlinRootType
import org.jetbrains.kotlin.idea.util.application.runReadAction
import java.util.concurrent.Executors

internal class ModuleAnnotatedHolder(
    private val module: Module,
    private val annotationFqn: String,
) {
    private val edenService = EdenService.getInstance(module.project)
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
        postCreateFiles(module, allFiles, singleApt)
        return true
    }

    companion object {
        private val logger = Logger.getInstance(ModuleAnnotatedHolder::class.java)
        private val kotlinSourceRoots: MutableSet<JpsModuleSourceRootType<*>> =
            hashSetOf(JavaSourceRootType.SOURCE, SourceKotlinRootType)

        private val executors = Executors.newCachedThreadPool()
        private fun postCreateFiles(
            module: Module, allFiles: List<FileSpec>, apt: EdenApt,
        ) = executors.execute {
            if (allFiles.isEmpty()) return@execute
            val sourceRoot = apt.getGeneratePath(module)
                ?: module.rootManager.getSourceRoots(kotlinSourceRoots).firstOrNull()
            if (sourceRoot == null) {
                logger.warn("no kotlin/java source root found in [${module.name}]!")
                return@execute
            }
            val ioFile = VfsUtilCore.virtualToIoFile(sourceRoot)
            for (file in allFiles) {
                file.writeTo(ioFile)
            }
            sourceRoot.refresh(true, true)
        }
    }
}
