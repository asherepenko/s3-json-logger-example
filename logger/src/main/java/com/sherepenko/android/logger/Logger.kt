@file:JvmName("LoggerWrapper")

package com.sherepenko.android.logger

import androidx.work.Operation

typealias LogParameter = String

typealias LogContext = Map<LogParameter, String>

fun LogContext.extend(addedLogContext: LogContext): LogContext =
    LinkedHashMap<LogParameter, String>().apply {
        this.putAll(this@extend)
        this.putAll(addedLogContext)
    }

enum class LogLevel {
    DEBUG,
    INFO,
    WARNING,
    ERROR
}

interface LogWriter {

    fun write(logLevel: LogLevel, logContext: LogContext)

    fun forceUpload(): Operation
}

/**
 * Logger instance that works with [LogWriter] to write logs with [LogContext].
 */
interface Logger {
    /**
     * "Debug" level log message. Designates fine-grained informational events that are most useful to
     * debug an application.
     */
    fun debug(message: String)

    /**
     * "Info" level log message. Designates informational messages that highlight the progress of
     * the application at coarse-grained level.
     */
    fun info(message: String)

    /**
     * "Warning" level log message. Designates potentially harmful situations.
     */
    fun warning(message: String)

    /**
     * "Error" level log message. Designates error events that might still allow the application to continue running.
     */
    fun error(message: String)

    /**
     * "Error" level log message. Designates error events that might still allow the application to continue running.
     */
    fun error(message: String, throwable: Throwable?)

    /**
     * Build a new logger with extended context.
     *
     * For example: current context is "applicationId:com.example.app" and now you have a logged in user, so
     * you create a new logger with context "applicationId:com.example.app, userId:123456"
     */
    fun extendContext(addedLogContext: LogContext): Logger

    /**
     * Force logs upload (calls force upload on [LogWriter]).
     */
    fun forceLogUpload(): Operation
}

fun Logger.withApplicationId(applicationId: String): Logger =
    this.extendContext(mapOf(BaseLoggerParams.APPLICATION_ID to applicationId))

fun Logger.withTag(tag: String): Logger =
    this.extendContext(mapOf(BaseLoggerParams.TAG to tag))

fun Logger.withUserId(userId: String): Logger =
    this.extendContext(mapOf(BaseLoggerParams.USER_ID to userId))

fun Logger.withAppInstallId(appInstallId: String): Logger =
    this.extendContext(mapOf(BaseLoggerParams.APP_INSTALL_ID to appInstallId))

fun Logger.withDeviceSerial(deviceSerial: String): Logger =
    this.extendContext(mapOf(BaseLoggerParams.DEVICE_SERIAL to deviceSerial))

fun Logger.withDeviceId(deviceId: String): Logger =
    this.extendContext(mapOf(BaseLoggerParams.DEVICE_ID to deviceId))

fun Logger.withJobId(jobId: String): Logger =
    this.extendContext(mapOf(BaseLoggerParams.JOB_ID to jobId))

object BaseLoggerParams {
    const val MESSAGE: LogParameter = "message"
    const val TIMESTAMP: LogParameter = "timestamp"
    const val LOG_LEVEL: LogParameter = "log_level"
    const val APPLICATION_ID: LogParameter = "application_id"

    const val TAG: LogParameter = "tag"
    const val USER_ID: LogParameter = "user_id"
    const val APP_INSTALL_ID: LogParameter = "app_install_id"
    const val DEVICE_SERIAL: LogParameter = "device_serial"
    const val DEVICE_ID: LogParameter = "device_id"
    const val JOB_ID: LogParameter = "job_id"
    const val EXCEPTION: LogParameter = "exception"
}
