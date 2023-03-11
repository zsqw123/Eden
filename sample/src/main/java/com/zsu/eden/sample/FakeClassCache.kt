package com.zsu.eden.sample

import com.intellij.openapi.project.Project
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import com.zsu.eden.EdenClassFinder
import com.zsu.eden.EdenClassNamesCache
import com.zsu.eden.fast.EdenApt
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.util.capitalizeDecapitalize.capitalizeFirstWord

internal const val fakeFqn = "com.fake.FakeClass"

//@Service
//class FakeClassCache(project: Project) :
//    EdenCache(project, FakeTracker.getInstance(project), fakeFqn) {
//    override fun processAnnotation(annotations: List<KtNamedDeclaration>)
//        : List<FakeFile> = annotations.mapNotNull { declaration ->
//        val method = declaration as? KtNamedFunction ?: return@mapNotNull null
//        val originReturnType = method.returnType ?: PsiType.VOID
//        val name = declaration.name
//        val className = "Fake${name.capitalizeAsciiOnly()}"
//        Eden.fakeClassFile(className, method.packageName ?: "") {
//            method("fakeMethod") {
//                returnType = originReturnType
//            }
//            field("fakeField")
//            clazz("FakeInnerClass")
//            method("fakeMethodStatic") {
//                navigateTo = method
//                isStatic = true
//            }
//            constructor {
//                property("c1", PsiType.INT, isField = true)
//            }
//            field("fakeFieldStatic")
//            clazz("FakeInnerClassStatic") {
//                isStatic = true
//            }
//            method("fakeMethodParams") {
//                textParam(
//                    "a" to "String",
//                    "b" to "int",
//                )
//                returnType = PsiType.INT
//            }
//            navigateTo = method
//        }
//    }
//
//    companion object {
//        fun getInstance(project: Project): FakeClassCache {
//            return project.getService(FakeClassCache::class.java)
//        }
//    }
//}

// 这个是为了输入字符的时候实时提示
class FakeShortNameCache(project: Project) : EdenClassNamesCache(project)

// 这个是为了让类不爆红
class FakeClassFinder(project: Project) : EdenClassFinder(project)

class FakeApt : EdenApt {
    override val annotationFqn: String = fakeFqn
    override fun processSingleModule(all: List<KtNamedDeclaration>): List<FileSpec> {
        val allNames = all.mapNotNull { it.name }
        val allFiles = allNames.map { name ->
            val className = "Fake${name.capitalizeFirstWord()}"
            val type = TypeSpec.classBuilder(className).build()
            FileSpec.builder("com.zsu", className)
                .addType(type)
                .build()
        }
        return allFiles
    }
}
