package com.noirmusic

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class SplashActivity : AppCompatActivity() {

    private val SPLASH_DELAY = 2500L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Apply animations for premium feel
        applyAnimations()

        // Delay and then check permissions
        Handler(Looper.getMainLooper()).postDelayed({
            if (hasStoragePermission()) {
                navigateToMain()
            } else {
                requestStoragePermission()
            }
        }, SPLASH_DELAY)
    }

    private fun applyAnimations() {
        val logo = findViewById<ImageView>(R.id.logoImageView)
        val appName = findViewById<TextView>(R.id.appNameTextView)
        val madeBy = findViewById<TextView>(R.id.madeByTextView)

        val fadeInScale = AnimationUtils.loadAnimation(this, R.anim.fade_in_scale)
        val slideUpFadeIn = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in)

        logo.startAnimation(fadeInScale)
        appName.startAnimation(slideUpFadeIn)
        madeBy.startAnimation(slideUpFadeIn)
    }

    private fun hasStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestStoragePermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        ActivityCompat.requestPermissions(
            this,
            arrayOf(permission),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            navigateToMain()
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }
}
