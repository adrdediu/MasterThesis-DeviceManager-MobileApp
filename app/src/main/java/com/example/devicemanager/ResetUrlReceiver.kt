package com.example.devicemanager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ResetUrlReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        context.getSharedPreferences("device_manager", Context.MODE_PRIVATE)
            .edit()
            .remove("saved_url")
            .apply()

        // Launch main activity
        val launchIntent = Intent(context, MainActivity::class.java)
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        context.startActivity(launchIntent)
    }
}
