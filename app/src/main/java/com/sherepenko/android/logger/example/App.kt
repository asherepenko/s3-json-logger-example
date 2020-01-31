package com.sherepenko.android.logger.example

import android.app.Application
import android.os.Build
import android.os.Environment
import android.os.Process
import android.util.Base64
import android.util.Log
import androidx.work.ListenableWorker
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Region
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.sherepenko.android.archivarius.Archivarius
import com.sherepenko.android.archivarius.ArchivariusAnalytics
import com.sherepenko.android.archivarius.ArchivariusStrategy
import com.sherepenko.android.archivarius.data.LogType
import com.sherepenko.android.archivarius.uploaders.LogUploadWorker
import com.sherepenko.android.archivarius.uploaders.LogUploader
import com.sherepenko.android.archivarius.uploaders.S3LogUploader
import com.sherepenko.android.archivarius.uploaders.S3LogUrlGeneratorApi
import com.sherepenko.android.logger.ArchivariusLogger
import com.sherepenko.android.logger.Logger
import com.sherepenko.android.logger.withTag
import com.sherepenko.android.logger.withUserId
import java.io.File
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.factory
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton

class App : Application(), KodeinAware {

    companion object {

        private const val APPLICATION_MODULE = "applicationModule"

        private const val DEFAULT_LOG_POSTFIX = ""

        private const val PER_USER_RANGE = 100000

        private val currentUserId: Int = Process.myUid() / PER_USER_RANGE
    }

    override val kodein by Kodein.lazy {
        import(androidXModule(this@App))
        import(applicationModule)
    }

    private val applicationModule = Kodein.Module(APPLICATION_MODULE) {
        bind<ArchivariusAnalytics.ArchivariusAnalyticsImpl>() with singleton {
            object : ArchivariusAnalytics.ArchivariusAnalyticsImpl {

                override fun reportToCrashlytics(tag: String, e: Throwable) {
                    FirebaseCrashlytics.getInstance().apply {
                        recordException(e)
                        sendUnsentReports()
                    }
                }
            }
        }

        bind<ArchivariusStrategy.ArchivariusStrategyImpl>() with singleton {
            object : ArchivariusStrategy.ArchivariusStrategyImpl {

                override val isInDebugMode: Boolean = true

                override val isLogcatEnabled: Boolean = true

                override val authority: String = ""

                override val logName: String = Archivarius.DEFAULT_LOG_NAME

                override val rotateFilePostfix: String = DEFAULT_LOG_POSTFIX

                override val parentLogDir: File by lazy {
                    if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
                        this@App.obbDir
                    } else {
                        this@App.filesDir
                    }
                }

                override val logUploader: LogUploader by lazy {
                    val logBucketMeta = provideLogBucketMeta()

                    S3LogUploader(
                        logBucketMeta = logBucketMeta,
                        api = object : S3LogUrlGeneratorApi(logBucketMeta) {

                            override fun generateLogKey(logName: String, logType: LogType): String =
                                "${Build.MANUFACTURER.replace("\\s".toRegex(), "")}/" +
                                    "${Build.MODEL.replace("\\s".toRegex(), "")}/" +
                                    super.generateLogKey(logName, logType)
                        }
                    )
                }

                override val logUploadWorker: Class<out ListenableWorker> =
                    LogUploadWorker::class.java

                private fun provideAwsCredentials(): AWSCredentials {
                    val accessKeyDecoded =
                        String(Base64.decode(BuildConfig.AWS_ACCESS_KEY_ENCODED, Base64.DEFAULT))
                    val secretKeyDecoded =
                        String(Base64.decode(BuildConfig.AWS_SECRET_KEY_ENCODED, Base64.DEFAULT))

                    Log.d("AWS", "accessKey = $accessKeyDecoded; secretKey = $secretKeyDecoded")

                    return BasicAWSCredentials(accessKeyDecoded, secretKeyDecoded)
                }

                private fun provideLogBucketMeta(): S3LogUploader.LogBucketMeta =
                    S3LogUploader.LogBucketMeta(
                        BuildConfig.AWS_BUCKET_NAME,
                        provideAwsCredentials(),
                        Region.getRegion(BuildConfig.AWS_BUCKET_REGION)
                    )
            }
        }

        bind<Archivarius>() with singleton {
            ArchivariusAnalytics.init(instance())
            ArchivariusStrategy.init(instance())
            Archivarius.Builder(this@App).build().apply {
                schedulePeriodicLogsUpload()
            }
        }

        bind<Logger>() with factory { tag: String ->
            ArchivariusLogger(BuildConfig.APPLICATION_ID, instance<Archivarius>())
                .withUserId(currentUserId.toString())
                .withTag(tag)
        }
    }
}
