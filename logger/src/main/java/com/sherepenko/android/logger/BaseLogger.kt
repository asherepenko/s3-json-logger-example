package com.sherepenko.android.logger

import androidx.work.Operation
import com.sherepenko.android.archivarius.utils.DateTimeUtils
import java.io.PrintWriter
import java.io.StringWriter
import java.util.Calendar
import java.util.GregorianCalendar

open class BaseLogger @JvmOverloads protected constructor(
    protected open val logWriter: LogWriter,
    protected open val logContext: LogContext = emptyMap()
) : Logger {

    constructor(applicationId: String, logWriter: LogWriter) :
        this(logWriter, mapOf(BaseLoggerParams.APPLICATION_ID to applicationId))

    override fun debug(message: String) {
        logWriter.write(LogLevel.DEBUG, logContext.addBaseFields(message, LogLevel.DEBUG))
    }

    override fun info(message: String) {
        logWriter.write(LogLevel.INFO, logContext.addBaseFields(message, LogLevel.INFO))
    }

    override fun warning(message: String) {
        logWriter.write(LogLevel.WARNING, logContext.addBaseFields(message, LogLevel.WARNING))
    }

    override fun error(message: String) {
        error(message, null)
    }

    override fun error(message: String, throwable: Throwable?) {
        val context = logContext
            .addBaseFields(message, LogLevel.ERROR)
            .run {
                if (throwable != null) {
                    val stringWriter = StringWriter()
                    throwable.printStackTrace(PrintWriter(stringWriter))
                    this.extend(mapOf(BaseLoggerParams.EXCEPTION to stringWriter.toString()))
                } else {
                    this
                }
            }

        logWriter.write(LogLevel.ERROR, context)
    }

    override fun extendContext(addedLogContext: LogContext): Logger =
        BaseLogger(logWriter, logContext.extend(addedLogContext))

    override fun forceLogUpload(): Operation =
        logWriter.forceUpload()

    private fun LogContext.addBaseFields(message: String, logLevel: LogLevel): LogContext =
        this.extend(
            mapOf(
                BaseLoggerParams.MESSAGE to message,
                BaseLoggerParams.TIMESTAMP to fromCalendar(GregorianCalendar.getInstance()),
                BaseLoggerParams.LOG_LEVEL to when (logLevel) {
                    LogLevel.DEBUG -> "debug"
                    LogLevel.INFO -> "info"
                    LogLevel.WARNING -> "warn"
                    LogLevel.ERROR -> "error"
                }
            )
        )

    /** Transform Calendar to ISO 8601 string.  */
    private fun fromCalendar(calendar: Calendar): String =
        DateTimeUtils.format(calendar.time)
}
