package com.sherepenko.android.logger.example

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sherepenko.android.logger.Logger
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.activity_main.uploadButton
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.di
import org.kodein.di.generic.instance

class MainActivity : AppCompatActivity(), DIAware {

    override val di: DI by di()

    private val logger: Logger by instance(arg = "MainActivity")

    private val disposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupViews()
        logger.warning("Activity created")
    }

    override fun onStart() {
        super.onStart()
        logger.info("Activity started")
    }

    override fun onResume() {
        super.onResume()
        logger.debug("Activity resumed")
    }

    override fun onPause() {
        super.onPause()
        logger.debug("Activity paused")
    }

    override fun onStop() {
        super.onStop()
        logger.info("Activity stopped")
    }

    override fun onDestroy() {
        super.onDestroy()
        logger.warning("Activity destroyed")
        disposable.dispose()
    }

    private fun setupViews() {
        supportActionBar?.setTitle(R.string.app_name)

        uploadButton.setOnClickListener {
            uploadButton.isEnabled = false
            uploadLogs()
        }
    }

    private fun uploadLogs() {
        logger.forceLogUpload()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = {
                    logger.info("Logs successfully uploaded")
                    Toast.makeText(
                        this@MainActivity,
                        "Logs successfully uploaded",
                        Toast.LENGTH_LONG
                    ).show()
                    uploadButton.isEnabled = true
                },
                onError = {
                    logger.error("Cannot upload logs from device", it)
                    Toast.makeText(
                        this@MainActivity,
                        "Cannot upload logs from device",
                        Toast.LENGTH_LONG
                    ).show()
                    uploadButton.isEnabled = true
                }
            )
            .addTo(disposable)
    }
}
