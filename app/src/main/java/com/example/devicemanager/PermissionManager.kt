package com.example.devicemanager

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


class PermissionManager(private val activity: ComponentActivity) {
    private val _permissionsGranted = MutableStateFlow(false)
    val permissionsGranted = _permissionsGranted.asStateFlow()

    companion object {
        private const val PERMISSION_REQUEST_CODE = 123

        // Define permissions based on API level
        fun getRequiredPermissions(context: Context): Array<String> {
            val basePermissions = mutableListOf(
                Manifest.permission.INTERNET,
                Manifest.permission.CAMERA,
                Manifest.permission.POST_NOTIFICATIONS
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13 and above
                basePermissions.addAll(
                    listOf(
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO,
                        Manifest.permission.READ_MEDIA_AUDIO
                    )
                )
            } else {
                // Below Android 13
                basePermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    basePermissions.add(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
                }
            }

            return basePermissions.toTypedArray()
        }
    }

    private val permissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val deniedPermissions = permissions.filter { !it.value }.keys

        if (deniedPermissions.isNotEmpty()) {
            // Some permissions were denied
            handleDeniedPermissions(deniedPermissions.toList())
        } else {
            // All permissions granted
            onAllPermissionsGranted()
        }
    }

    private fun handleDeniedPermissions(deniedPermissions: List<String>) {
        // Check if we should show rationale for any permissions
        val rationalePermissions = deniedPermissions.filter {
            ActivityCompat.shouldShowRequestPermissionRationale(activity, it)
        }

        if (rationalePermissions.isNotEmpty()) {
            showPermissionRationale(rationalePermissions)
        } else {
            // User clicked "Don't ask again" for some permissions
            showSettingsDialog()
        }
    }

    private fun showPermissionRationale(permissions: List<String>) {
        val message = buildRationaleMessage(permissions)

        MaterialAlertDialogBuilder(activity)
            .setTitle("Permissions Required")
            .setMessage(message)
            .setPositiveButton("Try Again") { _, _ ->
                permissionLauncher.launch(permissions.toTypedArray())
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showSettingsDialog() {
        MaterialAlertDialogBuilder(activity)
            .setTitle("Permissions Required")
            .setMessage("Some permissions are required for the app to work properly. Please grant them in Settings.")
            .setPositiveButton("Settings") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun openAppSettings() {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", activity.packageName, null)
            activity.startActivity(this)
        }
    }

    private fun buildRationaleMessage(permissions: List<String>): String {
        val explanations = permissions.joinToString("\n") { permission ->
            when (permission) {
                Manifest.permission.CAMERA -> "• Camera: Required for capturing photos and videos"
                Manifest.permission.POST_NOTIFICATIONS -> "• Notifications: Required for download updates"
                Manifest.permission.READ_MEDIA_IMAGES -> "• Photos: Required for saving images"
                Manifest.permission.READ_MEDIA_VIDEO -> "• Videos: Required for saving videos"
                Manifest.permission.READ_MEDIA_AUDIO -> "• Audio: Required for saving audio files"
                Manifest.permission.WRITE_EXTERNAL_STORAGE -> "• Storage: Required for saving files"
                Manifest.permission.MANAGE_EXTERNAL_STORAGE -> "• Storage Management: Required for file operations"
                else -> "• ${permission.split(".").last()}: Required for app functionality"
            }
        }
        return "The following permissions are required:\n\n$explanations"
    }

    private fun onAllPermissionsGranted() {
        _permissionsGranted.value = true
        Toast.makeText(activity, "All permissions granted!", Toast.LENGTH_SHORT).show()
    }

    fun checkAndRequestPermissions() {
        val requiredPermissions = getRequiredPermissions(activity)
        permissionLauncher.launch(requiredPermissions)
    }
}