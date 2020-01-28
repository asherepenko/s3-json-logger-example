package com.sherepenko.android.logger.example

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.work.Operation
import com.sherepenko.android.logger.Logger
import kotlinx.android.synthetic.main.activity_main.uploadButton
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class MainActivity : AppCompatActivity(), KodeinAware {

    override val kodein: Kodein by kodein()

    private val logger: Logger by instance(arg = "MainActivity")

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
    }

    private fun setupViews() {
        supportActionBar?.setTitle(R.string.app_name)

        uploadButton.setOnClickListener {
            uploadButton.isEnabled = false
            uploadLogs()
        }
    }

    private fun uploadLogs() {
        logger.forceLogUpload().state.observe(this@MainActivity, Observer {
            when (it) {
                is Operation.State.SUCCESS -> {
                    logger.info("Logs successfully uploaded")
                    Toast.makeText(
                        this@MainActivity,
                        "Logs successfully uploaded",
                        Toast.LENGTH_LONG
                    ).show()
                    uploadButton.isEnabled = true
                }
                is Operation.State.IN_PROGRESS -> {
                    logger.info("Logs uploading...")
                }
                is Operation.State.FAILURE -> {
                    logger.error("Cannot upload logs from device")
                    Toast.makeText(
                        this@MainActivity,
                        "Cannot upload logs from device",
                        Toast.LENGTH_LONG
                    ).show()
                    uploadButton.isEnabled = true
                }
            }
        })
    }
}
