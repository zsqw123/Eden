package com.zsu.eden

import com.squareup.kotlinpoet.FileSpec
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import java.io.File

abstract class EdenPoetApt : EdenApt() {
    abstract fun processWithPoet(all: List<KtNamedDeclaration>): List<FileSpec>
    override fun processSingleModule(all: List<KtNamedDeclaration>): List<EdenFile> {
        return processWithPoet(all).map { PoetEdenFile(it) }
    }
}

private class PoetEdenFile(private val poetFile: FileSpec) : EdenFile {
    override val name: String = poetFile.name
    override val packageName: String = poetFile.packageName
    override fun writeTo(ioFile: File) = poetFile.writeTo(ioFile)
}
