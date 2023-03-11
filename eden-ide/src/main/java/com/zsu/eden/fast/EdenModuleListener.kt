package com.zsu.eden.fast

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.ModuleListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener

class EdenModuleListener : ModuleListener, ProjectManagerListener {
    override fun moduleAdded(project: Project, module: Module) {
        EdenModuleCache.getInstance(project).addModule(module)
    }

    override fun moduleRemoved(project: Project, module: Module) {
        EdenModuleCache.getInstance(project).removeModule(module)
    }

    override fun projectClosing(project: Project) {
        EdenModuleCache.getInstance(project).clear()
    }
}
