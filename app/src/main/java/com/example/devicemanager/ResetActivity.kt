package com.example.devicemanager

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Process
import androidx.activity.ComponentActivity
import kotlin.system.exitProcess

class ResetActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Clear saved URL
        getSharedPreferences("device_manager", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()

        // Kill the app completely
        finishAffinity()
        Process.killProcess(Process.myPid())
        exitProcess(0)
    }
}
