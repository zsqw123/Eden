package com.zsu.eden.sample

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiType
import com.zsu.eden.EdenCache
import com.zsu.eden.EdenClassFinder
import com.zsu.eden.EdenClassNamesCache
import com.zsu.eden.EdenModificationTracker
import com.zsu.eden.dsl.Eden
import com.zsu.eden.dsl.FakeFile
import com.zsu.eden.util.packageName
import org.jetbrains.kotlin.util.capitalizeDecapitalize.capitalizeAsciiOnly
import org.jetbrains.uast.UDeclaration
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.kotlin.KotlinUMethod
import org.jetbrains.uast.tryResolve

internal const val fakeFqn = "com.fake.FakeClass"

@Service
class FakeClassCache(project: Project) :
    EdenCache(project, FakeTracker.getInstance(project), fakeFqn) {
    override fun processAnnotation(annotations: List<UDeclaration>)
        : List<FakeFile> = annotations.mapNotNull { declaration ->
        val method = declaration as? UMethod ?: return@mapNotNull null
        val originReturnType = method.returnType ?: PsiType.VOID
        val name = declaration.name
        val className = "Fake${name.capitalizeAsciiOnly()}"
        Eden.fakeClassFile(className, method.sourcePsi?.packageName ?: "") {
            method("fakeMethod") {
                returnType = originReturnType
            }
            field("fakeField")
            clazz("FakeInnerClass")
            method("fakeMethodStatic") {
                navigateTo = method.sourcePsi
                isStatic = true
            }
            constructor {
                property("c1", PsiType.INT, isField = true)
            }
            field("fakeFieldStatic")
            clazz("FakeInnerClassStatic") {
                isStatic = true
            }
            method("fakeMethodParams") {
                textParam(
                    "a" to "String",
                    "b" to "int",
                )
                returnType = PsiType.INT
            }
            navigateTo = method.sourcePsi
        }
    }

    companion object {
        fun getInstance(project: Project): FakeClassCache {
            return project.getService(FakeClassCache::class.java)
        }
    }
}

// 这个是为了输入字符的时候实时提示
class FakeShortNameCache(project: Project) :
    EdenClassNamesCache(FakeClassCache.getInstance(project))

// 这个是为了让类不爆红
class FakeClassFinder(project: Project) : EdenClassFinder(FakeClassCache.getInstance(project))

// 用于刷新缓存
@Service
class FakeTracker : EdenModificationTracker() {
    companion object {
        fun getInstance(project: Project): FakeTracker {
            return project.getService(FakeTracker::class.java)
        }
    }
}
