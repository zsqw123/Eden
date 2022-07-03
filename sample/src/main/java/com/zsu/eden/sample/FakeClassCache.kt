package com.zsu.eden.sample

import com.ibm.icu.impl.CacheValue
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.util.CachedValueProvider
import com.intellij.util.indexing.IndexableSetContributor
import com.zsu.eden.*
import com.zsu.eden.dsl.Eden
import com.zsu.eden.dsl.FakeClass
import com.zsu.eden.util.packageName
import org.jetbrains.kotlin.idea.caches.project.getModuleInfo
import org.jetbrains.kotlin.idea.core.util.CachedValue
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.util.capitalizeDecapitalize.capitalizeAsciiOnly

internal const val fakeFqn = "com.fake.FakeClass"

@Service
class FakeClassCache(project: Project) : EdenCache(project, fakeFqn) {
    override fun processAnnotation(annotations: Sequence<KtDeclaration>): Sequence<FakeClass> = annotations.mapNotNull { ktDeclaration ->
        val name = ktDeclaration.name ?: return@mapNotNull null
        val className = "Fake${name.capitalizeAsciiOnly()}"
        Eden.fakeClass(className, ktDeclaration.packageName) {
            method("fakeMethod")
            field("fakeField", "String")
            clazz("FakeInnerClass")
            method("fakeMethodStatic") {
                navigateTo = ktDeclaration.firstChild
                isStatic = true
            }
            constructor {
                property("a", "int", isField = true)
            }
            field("fakeFieldStatic", "int", isStatic = true)
            clazz("FakeInnerClassStatic") {
                isStatic = true
            }
            method("fakeMethodParams") {
                param(
                    "a" to "String",
                    "b" to "int",
                )
                returnType = "int"
            }
            navigateTo = ktDeclaration
        }
    }

    companion object {
        fun getInstance(project: Project): FakeClassCache {
            return project.getService(FakeClassCache::class.java)
        }
    }
}

// 这个是为了输入字符的时候实时提示
class FakeShortNameCache(project: Project) : EdenClassNamesCache(FakeClassCache.getInstance(project), FakeTracker.getInstance(project))

// 这个是为了让类不爆红
class FakeClassFinder(project: Project) : EdenClassFinder(FakeClassCache.getInstance(project), FakeTracker.getInstance(project))

class FakeKotlinScopeEnlarger : EdenResolveScopeEnlarger({ FakeClassCache.getInstance(it) })

// 用于刷新缓存
@Service
class FakeTracker : EdenModificationTracker() {
    companion object {
        fun getInstance(project: Project): FakeTracker {
            return project.getService(FakeTracker::class.java)
        }
    }
}

class FakeIndexContributor : IndexableSetContributor() {
    override fun getAdditionalRootsToIndex(): MutableSet<VirtualFile> = mutableSetOf()
    override fun getAdditionalProjectRootsToIndex(project: Project): MutableSet<VirtualFile> {
        if (DumbService.isDumb(project)) return mutableSetOf()
        val files = FakeClassCache.getInstance(project).getClasses().map { it.containingFile.virtualFile }
        return files.toMutableSet()
    }
}