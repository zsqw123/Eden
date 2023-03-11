package com.zsu.eden

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.psi.search.GlobalSearchScope

@Service
class EdenModuleCache(private val project: Project) {
    private val moduleMap: HashMap<Module, ModuleContent> = hashMapOf()

    internal fun addModule(module: Module) {
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

    @Volatile
    private var loading = false
    fun tryLoadCache(scope: GlobalSearchScope) {
        if (loading) return
        loading = true
        for ((k, v) in moduleMap) {
            if (!scope.isSearchInModuleContent(k)) continue
            v.cached.value
        }
        loading = false
    }

    companion object {
        fun getInstance(project: Project) = project.service<EdenModuleCache>()
    }
}
