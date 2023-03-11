package com.zsu.eden

import com.intellij.openapi.Disposable
import com.intellij.openapi.module.Module
import com.intellij.openapi.util.SimpleModificationTracker
import com.intellij.psi.PsiManager
import com.intellij.psi.search.PsiSearchScopeUtil
import com.intellij.psi.util.CachedValue
import org.jetbrains.kotlin.idea.util.cachedValue
import org.jetbrains.kotlin.psi.KtDeclaration

internal class ModuleContent(private val module: Module) : Disposable {
    private val project = module.project
    private val scope = module.moduleScope
    private val annotatedCache = HashMap<String, ModuleAnnotatedHolder>()
    private val edenService = EdenService.getInstance(project)
    private val allAptFqn = edenService.allApt.keys
    private val allAptSimple = allAptFqn.map { it.substringAfterLast('.') }
        .filter { it.isNotBlank() }

    init {
        allAptFqn.forEach {
            annotatedCache[it] = ModuleAnnotatedHolder(module, it)
        }
    }

    private val psiManager = PsiManager.getInstance(project)
    private val tracker = SimpleModificationTracker()
    private val changeListener = ChangeListener()

    init {
        psiManager.addPsiTreeChangeListener(changeListener, this)
    }

    val cached: CachedValue<*> = cachedValue(project, tracker) {
        annotatedCache.values.forEach { it.create() }
    }

    override fun dispose() {
        psiManager.removePsiTreeChangeListener(changeListener)
    }

    private inner class ChangeListener : SimpleAnnotatedChange(allAptSimple) {
        override fun onAnnotatedElementChange(declaration: KtDeclaration) {
            if (PsiSearchScopeUtil.isInScope(scope, declaration)) {
                tracker.incModificationCount()
            }
        }
    }
}
