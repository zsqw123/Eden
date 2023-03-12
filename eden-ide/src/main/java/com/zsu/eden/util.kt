package com.zsu.eden

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProcessCanceledException

fun Logger.logErrorIfNeeded(message: String, e: Throwable) {
    if (e is ProcessCanceledException) {
        return // process cancellation is not allowed to be logged and will throw
    }
    error(message, e)
}
