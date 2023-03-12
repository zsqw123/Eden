package com.zsu.eden

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.psi.search.GlobalSearchScope

@Service
class EdenModuleCache(private val project: Project) {
    private val moduleMap: HashMap<Module, ModuleContent> = hashMapOf()
    private val enabled = EdenService.getInstance(project).allApt.values.any {
        it.checkEnable(project)
    }

    internal fun addModule(module: Module) {
        if (!enabled) return
        moduleMap[module] = ModuleContent(module)
    }

    internal fun removeModule(module: Module) {
        val content = moduleMap.remove(module)
        if (content != null) {
            Disposer.dispose(content)
        }
    }

    internal fun clear() {
        moduleMap.values.forEach {
            Disposer.dispose(it)
        }
    }

    internal fun refresh(module: Module, fqns: List<String>?, needClean: Boolean) {
        moduleMap[module]?.refresh(fqns, needClean)
    }

    internal fun refresh(fqns: List<String>?, needClean: Boolean) {
        moduleMap.forEach { it.value.refresh(fqns, needClean) }
    }

    @Volatile
    private var loading = false
    fun tryLoadCache(scope: GlobalSearchScope) {
        if (loading) return
        if (DumbService.isDumb(project)) return
        try {
            loading = true
            for ((k, v) in moduleMap) {
                if (!scope.isSearchInModuleContent(k)) continue
                v.cached.value
            }
        } finally {
            loading = false
        }
    }

    companion object {
        fun getInstance(project: Project) = project.service<EdenModuleCache>()
    }
}
