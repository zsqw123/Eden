package com.zsu.eden

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project

@Service
class Regenerator(private val project: Project) {
    private val moduleCache = EdenModuleCache.getInstance(project)

    /**
     * @param scope refresh scope
     * @param fqns annotation fqns which need refresh
     * @param needClean remove generated before processing
     * @return true if refresh success
     * @see Scope
     */
    fun refresh(
        scope: Scope,
        fqns: List<String>? = null,
        needClean: Boolean = false,
    ): Boolean {
        when (scope) {
            Scope.CURRENT_MODULE -> {
                val selectedFile = FileEditorManager.getInstance(project).selectedFiles
                    .firstOrNull() ?: return false
                val currentModule = ModuleUtilCore.findModuleForFile(selectedFile, project)
                    ?: return false
                moduleCache.refresh(currentModule, fqns, needClean)
                return true
            }

            Scope.PROJECT -> {
                moduleCache.refresh(fqns, needClean)
                return true
            }
        }
    }

    enum class Scope {
        CURRENT_MODULE, // refresh selected file's project
        PROJECT, // refresh whole project
    }

    companion object {
        fun getInstance(project: Project) = project.service<Regenerator>()
    }
}
