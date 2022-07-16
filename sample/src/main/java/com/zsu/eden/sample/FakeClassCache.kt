package com.zsu.eden.sample

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.zsu.eden.*
import com.zsu.eden.dsl.Eden
import com.zsu.eden.dsl.FakeClass
import com.zsu.eden.util.packageName
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.idea.quickfix.createFromUsage.callableBuilder.getReturnTypeReference
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension
import org.jetbrains.kotlin.resolve.lazy.LazyClassContext
import org.jetbrains.kotlin.resolve.lazy.declarations.PackageMemberDeclarationProvider
import org.jetbrains.kotlin.util.capitalizeDecapitalize.capitalizeAsciiOnly

internal const val fakeFqn = "com.fake.FakeClass"

@Service
class FakeClassCache(project: Project) : EdenCache(project, fakeFqn) {
    override fun processAnnotation(annotations: Sequence<KtDeclaration>): Sequence<FakeClass> = annotations.mapNotNull { ktDeclaration ->
        val name = ktDeclaration.name ?: return@mapNotNull null
        val className = "Fake${name.capitalizeAsciiOnly()}"
        (ktDeclaration as KtFunction).getReturnTypeReference()
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

class FakeSynEx(project: Project) : SyntheticResolveExtension {
    override fun generateSyntheticClasses(
        thisDescriptor: PackageFragmentDescriptor,
        name: Name,
        ctx: LazyClassContext,
        declarationProvider: PackageMemberDeclarationProvider,
        result: MutableSet<ClassDescriptor>
    ) {
        super.generateSyntheticClasses(thisDescriptor, name, ctx, declarationProvider, result)
    }
}