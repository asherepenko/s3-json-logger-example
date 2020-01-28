package com.sherepenko.android.logger

import android.util.Log
import androidx.work.Operation
import com.sherepenko.android.archivarius.Archivarius
import com.sherepenko.android.archivarius.ArchivariusStrategy
import com.sherepenko.android.archivarius.entries.JsonLogEntry

interface GlobalLogContextContainer {

    fun getGlobalContext(): LogContext

    fun setGlobalContext(logContext: LogContext)

    fun extendGlobalContext(logContext: LogContext)
}

class ArchivariusLogWriter(
    private val archivarius: Archivarius
) : LogWriter, GlobalLogContextContainer {

    private var globalLogContext: LogContext = emptyMap()

    override fun write(logLevel: LogLevel, logContext: LogContext) {
        val extendedContext = logContext.extend(globalLogContext)
        archivarius.log(JsonLogEntry(extendedContext))

        if (ArchivariusStrategy.get().isLogcatEnabled) {
            val tag = extendedContext[BaseLoggerParams.TAG]
            val message = extendedContext[BaseLoggerParams.MESSAGE]

            val contextString = extendedContext.entries.joinToString()

            when (logLevel) {
                LogLevel.DEBUG -> Log.d(tag, "$message \n Context: $contextString")
                LogLevel.INFO -> Log.i(tag, "$message \n Context: $contextString")
                LogLevel.WARNING -> Log.w(tag, "$message \n Context: $contextString")
                LogLevel.ERROR -> Log.e(tag, "$message \n Context: $contextString")
            }
        }
    }

    override fun forceUpload(): Operation =
        archivarius.scheduleLogsUpload()

    override fun getGlobalContext(): LogContext =
        globalLogContext

    override fun setGlobalContext(logContext: LogContext) {
        globalLogContext = logContext
    }

    override fun extendGlobalContext(logContext: LogContext) {
        globalLogContext = globalLogContext.extend(logContext)
    }
}
