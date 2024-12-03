package com.example.devicemanager.ui.screens

data class ConnectionScreenState(
    val deviceId: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
