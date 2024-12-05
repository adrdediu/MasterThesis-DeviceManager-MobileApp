package com.example.devicemanager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class QRScannerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val scanIntent = Intent(context, QRScannerActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        context.startActivity(scanIntent)
    }
}
