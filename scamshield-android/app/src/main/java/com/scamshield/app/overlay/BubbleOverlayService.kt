package com.scamshield.app.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.scamshield.app.ui.theme.ScamShieldTheme
import androidx.lifecycle.LifecycleService
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner

class BubbleOverlayService : LifecycleService(), SavedStateRegistryOwner {
    
    companion object {
        var isRunning = false
            private set
    }

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private var windowManager: WindowManager? = null
    private var composeView: ComposeView? = null
    private var isViewAttached = false

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        startForegroundNotification()
        setupOverlayView()
        isRunning = true
    }

    private fun startForegroundNotification() {
        val channelId = "scamshield_overlay"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "ScamShield Active",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows real-time protection status"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("ScamShield Protection Active")
            .setContentText("Watching for suspicious links")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            } else {
                startForeground(1, notification)
            }
        } catch (e: Exception) {
            Log.e("ScamShield", "Error starting foreground service", e)
            try {
                startForeground(1, notification)
            } catch (fallbackEx: Exception) {
                Log.e("ScamShield", "Fallback foreground failed", fallbackEx)
            }
        }
    }

    private fun setupOverlayView() {
        if (isViewAttached) return

        try {
            windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

            composeView = ComposeView(this).apply {
                setContent {
                    ScamShieldTheme {
                        BubbleView()
                    }
                }
            }

            composeView?.setViewTreeLifecycleOwner(this)
            composeView?.setViewTreeSavedStateRegistryOwner(this)

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = 20
                y = 200
            }

            windowManager?.addView(composeView, params)
            isViewAttached = true
        } catch (e: Exception) {
            Log.e("ScamShield", "Error adding overlay view", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        try {
            if (isViewAttached && composeView != null && windowManager != null) {
                windowManager?.removeView(composeView)
                isViewAttached = false
            }
        } catch (e: Exception) {
            Log.e("ScamShield", "Error removing overlay view", e)
        }
    }
}
