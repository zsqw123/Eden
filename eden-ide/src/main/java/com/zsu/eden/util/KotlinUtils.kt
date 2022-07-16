package com.zsu.eden.util

import com.intellij.openapi.project.Project
import org.jetbrains.uast.kotlin.KotlinUastResolveProviderService

fun getKotlinJavaResolver(project: Project): KotlinUastResolveProviderService {
    return project.getService(KotlinUastResolveProviderService::class.java)
}
