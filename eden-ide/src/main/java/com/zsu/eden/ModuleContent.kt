package com.zsu.eden

import com.intellij.openapi.Disposable
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.SimpleModificationTracker
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiTreeChangeListener
import com.intellij.psi.search.PsiSearchScopeUtil
import com.intellij.psi.util.CachedValue
import org.jetbrains.kotlin.idea.util.cachedValue
import org.jetbrains.kotlin.psi.KtDeclaration
import kotlin.concurrent.thread

internal class ModuleContent(private val module: Module) : Disposable {
    private val project = module.project
    private val scope = module.moduleScope
    private val annotatedCache = HashMap<String, ModuleAnnotatedHolder>()
    private val edenService = EdenService.getInstance(project)
    private val availableApt = edenService.allApt.filter { it.value.checkEnable(module) }

    private val allAptFqn: Collection<String>
    private val allAptSimple: Collection<String>

    init {
        if (availableApt.isNotEmpty()) {
            allAptFqn = availableApt.keys
            allAptSimple = allAptFqn.map { it.substringAfterLast('.') }
                .filter { it.isNotBlank() }
            allAptFqn.forEach {
                annotatedCache[it] = ModuleAnnotatedHolder(module, it)
            }
        } else {
            allAptFqn = emptyList()
            allAptSimple = emptyList()
        }
    }

    private val tracker = SimpleModificationTracker()
    private val psiManager = PsiManager.getInstance(project)

    private val changeListener: PsiTreeChangeListener?

    init {
        if (availableApt.isNotEmpty()) {
            changeListener = ChangeListener()
            psiManager.addPsiTreeChangeListener(changeListener, this)
        } else {
            changeListener = null
        }
    }

    val cached: CachedValue<*> = cachedValue(
        project, tracker,
        DumbService.getInstance(project).modificationTracker,
    ) {
        annotatedCache.values.forEach { it.create() }
    }

    override fun dispose() {
        if (changeListener != null) {
            psiManager.removePsiTreeChangeListener(changeListener)
        }
    }

    fun refresh(fqns: List<String>?, needClean: Boolean) {
        if (fqns == null) {
            tracker.incModificationCount()
            return
        }
        for ((annotation, holder) in annotatedCache) {
            if (annotation in fqns) {
                if (needClean) {
                    holder.cleanAllGenerated()
                }
                holder.create()
                break
            }
        }
    }

    private inner class ChangeListener : SimpleAnnotatedChange(allAptSimple) {
        private var lastChanged = 0L
        override fun onAnnotatedElementChange(declaration: KtDeclaration) {
            if (!PsiSearchScopeUtil.isInScope(scope, declaration)) return
            val current = System.currentTimeMillis()
            val timeAfterLastChanged = (current - lastChanged).coerceAtLeast(0)
            if (timeAfterLastChanged < 1500) {
                shouldCheck = false
                thread {
                    Thread.sleep(1500 - timeAfterLastChanged)
                    lastChanged = System.currentTimeMillis()
                    shouldCheck = true
                    tracker.incModificationCount()
                }
            } else {
                lastChanged = System.currentTimeMillis()
                tracker.incModificationCount()
            }
        }
    }
}
