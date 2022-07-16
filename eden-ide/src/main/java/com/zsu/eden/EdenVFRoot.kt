package com.zsu.eden

import com.intellij.openapi.vfs.NonPhysicalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileSystem
import com.intellij.openapi.vfs.ex.dummy.DummyFileSystem
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

object EdenVFS : DummyFileSystem(), NonPhysicalFileSystem {
    override fun getProtocol(): String = "eden-dummy"
}

class EdenVFRoot(private val edenCache: EdenCache, private val modificationTracker: EdenModificationTracker) : VirtualFile() {
    private val edenVirtualFiles: CachedValue<List<VirtualFile>>

    init {
        val cachedValueManager = CachedValuesManager.getManager(edenCache.project)
        edenVirtualFiles = cachedValueManager.createCachedValue {
            val classes = edenCache.getClasses()
            val allFiles = classes.map { it.containingFile.virtualFile }.toList()
            CachedValueProvider.Result.create(allFiles, modificationTracker)
        }
    }

    override fun getFileSystem(): VirtualFileSystem = EdenVFS
    override fun getName(): String = "eden-root"
    override fun getPath(): String = "/$name"
    override fun isWritable(): Boolean = false
    override fun isDirectory(): Boolean = true
    override fun isValid(): Boolean = true
    override fun getParent(): VirtualFile? = null
    override fun getChildren(): Array<VirtualFile> {
        return edenVirtualFiles.value.toTypedArray()
    }


    override fun contentsToByteArray(): ByteArray =
        throw IOException("eden shouldn't call contentsToByteArray.")

    override fun getTimeStamp(): Long = -1
    override fun getLength(): Long = 0

    override fun refresh(asynchronous: Boolean, recursive: Boolean, postRunnable: Runnable?) = Unit
    override fun getOutputStream(requestor: Any?, newModificationStamp: Long, newTimeStamp: Long): OutputStream =
        throw IOException("eden shouldn't has outputStream.")

    override fun getInputStream(): InputStream = throw IOException("eden shouldn't has inputStream.")
}