package com.example.devicemanager

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat

class NotificationHelper(private val context: Context) {
    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        private const val CHANNEL_ID = "default_channel"
        private const val CHANNEL_NAME = "Default Channel"
        private const val CHANNEL_DESCRIPTION = "Default notification channel"
        private const val DOWNLOAD_CHANNEL_ID = "download_channel"
        private const val DOWNLOAD_CHANNEL_NAME = "Downloads"
        private const val DOWNLOAD_CHANNEL_DESCRIPTION = "Download progress notifications"
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Default channel
            val defaultChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
            }

            // Download channel
            val downloadChannel = NotificationChannel(
                DOWNLOAD_CHANNEL_ID,
                DOWNLOAD_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW  // Lower importance for progress notifications
            ).apply {
                description = DOWNLOAD_CHANNEL_DESCRIPTION
            }

            notificationManager.createNotificationChannels(listOf(defaultChannel, downloadChannel))
        }
    }

    fun showNotification(
        title: String,
        message: String,
        notificationId: Int = System.currentTimeMillis().toInt(),
        pendingIntent: PendingIntent? = null
    ) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        pendingIntent?.let {
            builder.setContentIntent(it)
        }

        notificationManager.notify(notificationId, builder.build())
    }

    fun createDownloadNotification(
        filename: String,
        notificationId: Int = System.currentTimeMillis().toInt()
    ): DownloadNotificationBuilder {
        return DownloadNotificationBuilder(context, notificationManager, notificationId, filename)
    }

    class DownloadNotificationBuilder(
        private val context: Context,
        private val notificationManager: NotificationManager,
        private val notificationId: Int,
        private val filename: String
    ) {
        private val builder = NotificationCompat.Builder(context, DOWNLOAD_CHANNEL_ID)
            .setSmallIcon(com.example.devicemanager.R.drawable.device_manager)
            .setContentTitle("Downloading $filename")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setProgress(100, 0, true)

        fun updateProgress(bytesDownloaded: Long, totalBytes: Long) {
            val progress = ((bytesDownloaded.toFloat() / totalBytes.toFloat()) * 100).toInt()
            builder.setProgress(100, progress, false)
                .setContentText("$progress% complete")
            notificationManager.notify(notificationId, builder.build())
        }

        fun completeDownload(uri: Uri, mimeType: String) {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )

            builder.setContentText("Download complete")
                .setProgress(0, 0, false)
                .setOngoing(false)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            notificationManager.notify(notificationId, builder.build())
        }

        fun failDownload(error: String) {
            builder.setContentText("Download failed: $error")
                .setProgress(0, 0, false)
                .setOngoing(false)
                .setAutoCancel(true)
            notificationManager.notify(notificationId, builder.build())
        }

        fun getNotificationId() = notificationId
    }
}