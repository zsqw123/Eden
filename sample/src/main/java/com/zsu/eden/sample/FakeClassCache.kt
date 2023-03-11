package com.zsu.eden.sample

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import com.zsu.eden.EdenApt
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.util.capitalizeDecapitalize.capitalizeAsciiOnly

internal const val fakeFqn = "com.fake.FakeClass"

class FakeApt : EdenApt() {
    override val annotationFqn: String = fakeFqn
    override fun processSingleModule(all: List<KtNamedDeclaration>): List<FileSpec> {
        val allNames = all.mapNotNull { it.name }
        val allFiles = allNames.map { name ->
            val className = "Fake${name.capitalizeAsciiOnly()}"
            val type = TypeSpec.classBuilder(className).build()
            FileSpec.builder("com.zsu", className)
                .addType(type)
                .build()
        }
        return allFiles
    }
}
