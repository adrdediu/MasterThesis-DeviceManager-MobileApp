package com.example.devicemanager.viewmodels

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.webkit.URLUtil
import android.widget.Toast
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import com.example.devicemanager.QRScannerActivity
import com.example.devicemanager.QRScannerReceiver
import com.example.devicemanager.ResetUrlReceiver
import com.example.devicemanager.ui.screens.ConnectionScreenState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Loading : ConnectionState()
    data class Connected(val serverIp: String) : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}


class ConnectionViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ConnectionScreenState())
    val uiState: StateFlow<ConnectionScreenState> = _uiState.asStateFlow()

    fun updateDeviceId(deviceId: String) {
        _uiState.update { it.copy(deviceId = deviceId) }
    }

    private fun createTrustAllSSLContext(): SSLContext {
        return SSLContext.getInstance("TLS").apply {
            init(null, arrayOf(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            }), null)
        }
    }

    fun connect(context: Context, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val address = uiState.value.deviceId

            if (isValidAddress(address)) {
                val finalUrl = when {
                    address.startsWith("https://") -> address
                    address.startsWith("http://") -> address.replace("http://", "https://")
                    else -> "https://$address"
                }

                try {
                    withContext(Dispatchers.IO) {
                        val url = URL("$finalUrl/api/identify")
                        val connection = url.openConnection() as HttpsURLConnection
                        connection.sslSocketFactory = createTrustAllSSLContext().socketFactory
                        connection.hostnameVerifier = HostnameVerifier { _, _ -> true }
                        connection.connectTimeout = 5000
                        connection.readTimeout = 5000

                        try {
                            connection.connect()
                            val responseCode = connection.responseCode
                            if (responseCode == 200) {
                                val response = connection.inputStream.bufferedReader().use { it.readText() }
                                val jsonResponse = JSONObject(response)
                                if (jsonResponse.getString("identifier") == "dm_server_v1" &&
                                    jsonResponse.getString("server") == "device_manager") {
                                    _uiState.update { it.copy(deviceId = finalUrl) }
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "Server found! Connecting...", Toast.LENGTH_SHORT).show()
                                        delay(1000)
                                        onSuccess()
                                    }
                                } else {
                                    throw IOException("Invalid server identifier")
                                }
                            } else {
                                throw IOException("Server returned $responseCode")
                            }
                        } finally {
                            connection.disconnect()
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(error = "Could not connect to server") }
                    Toast.makeText(context, "Failed to connect to server", Toast.LENGTH_SHORT).show()
                }
            } else {
                _uiState.update { it.copy(error = "Invalid server address!") }
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun isValidAddress(address: String): Boolean {
        val ipPattern = """^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$""".toRegex()
        return address.matches(ipPattern) || URLUtil.isValidUrl("https://$address")
    }
}
