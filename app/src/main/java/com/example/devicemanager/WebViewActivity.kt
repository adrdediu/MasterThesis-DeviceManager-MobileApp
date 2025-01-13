package com.example.devicemanager
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.SslErrorHandler
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import com.guru.fontawesomecomposelib.FaIcon
import com.guru.fontawesomecomposelib.FaIcons

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.devicemanager.ui.theme.DeviceManagerTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class WebViewActivity : ComponentActivity() {
    private lateinit var webView: WebView

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val url = intent.getStringExtra("url") ?: return finish()

        onBackPressedDispatcher.addCallback(this) {
            when {
                webView.canGoBack() -> webView.goBack()
                else -> finish()
            }
        }

        setContent {
            var loadingProgress by remember { mutableStateOf(0) }
            var showMenu by remember { mutableStateOf(false) }
            DeviceManagerTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { },
                            navigationIcon = {
                                Row {
                                    IconButton(onClick = {
                                        if (webView.canGoBack()) webView.goBack()
                                    }) {
                                        FaIcon(
                                            faIcon = FaIcons.ArrowLeft,
                                            tint = Color.White,
                                            size = 24.dp
                                        )
                                    }
                                    val rotation by rememberInfiniteTransition().animateFloat(
                                        initialValue = 0f,
                                        targetValue = 360f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(1000, easing = LinearEasing)
                                        )
                                    )

                                    IconButton(onClick = { webView.reload() }) {
                                        FaIcon(
                                            faIcon = FaIcons.Redo,
                                            tint = Color.White,
                                            size = 24.dp,
                                            modifier = Modifier.graphicsLayer(
                                                rotationZ = if (loadingProgress in 1..99) rotation else 0f
                                            )
                                        )
                                    }
                                }
                            },
                            actions = {
                                IconButton(onClick = { startQRScanner() }) {
                                    FaIcon(
                                        faIcon = FaIcons.Qrcode,
                                        tint = Color.White,
                                        size = 24.dp
                                    )
                                }
                                Box {
                                    IconButton(onClick = { showMenu = true }) {
                                        FaIcon(
                                            faIcon = FaIcons.EllipsisV,
                                            tint = Color.White,
                                            size = 24.dp
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = showMenu,
                                        onDismissRequest = { showMenu = false },
                                        modifier = Modifier.background(Color.White)
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Reset Connection", color = Color.Black) },
                                            onClick = {
                                                showMenu = false
                                                resetConnection()
                                            },
                                            leadingIcon = {
                                                FaIcon(
                                                    faIcon = FaIcons.PowerOff,
                                                    tint = Color.Black,
                                                    size = 24.dp
                                                )
                                            }
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Black
                            ),

                        )

                    }

                )
                { paddingValues ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        WebContent(
                            url = url,
                            onWebViewCreated = { webView = it },
                            onProgressChanged = { loadingProgress = it },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                        )


                    }
                }
            }
        }
    }
    private fun startQRScanner() {
        startActivity(Intent(this, QRScannerActivity::class.java))
    }

    private fun resetConnection() {
        getSharedPreferences("device_manager", MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }



}
private fun createTrustAllCerts(): SSLSocketFactory {
    val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    })

    val sslContext = SSLContext.getInstance("SSL")
    sslContext.init(null, trustAllCerts, SecureRandom())
    return sslContext.socketFactory
}
@SuppressLint("ServiceCast", "NewApi")
@Composable
private fun WebContent(
    url: String,
    onWebViewCreated: (WebView) -> Unit,
    onProgressChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    setSupportZoom(true)
                }

                setDownloadListener { downloadUrl, userAgent, contentDisposition, mimetype, contentLength ->
                    val scope = CoroutineScope(Dispatchers.IO)

                    scope.launch {
                        val notificationHelper = NotificationHelper(context)
                        var downloadNotification: NotificationHelper.DownloadNotificationBuilder? = null


                        try {
                            val url = URL(downloadUrl)
                            val connection = url.openConnection() as HttpsURLConnection
                            connection.sslSocketFactory = createTrustAllCerts()
                            connection.hostnameVerifier = HostnameVerifier { _, _ -> true }

                            connection.requestMethod = "GET"
                            connection.setRequestProperty("User-Agent", userAgent)

                            val cookies = CookieManager.getInstance().getCookie(downloadUrl)
                            cookies?.let { connection.setRequestProperty("Cookie", it) }

                            connection.connect()

                            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                                throw IOException("Server returned HTTP ${connection.responseCode}")
                            }

                            val filename = URLUtil.guessFileName(downloadUrl, contentDisposition, mimetype)

                            // New code starts here
                            val contentResolver = context.contentResolver
                            val contentValues = ContentValues().apply {
                                put(MediaStore.Downloads.DISPLAY_NAME, filename)
                                put(MediaStore.Downloads.MIME_TYPE, mimetype)
                                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                            }

                            downloadNotification = notificationHelper.createDownloadNotification(filename)


                            val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                            uri?.let { downloadUri ->
                                contentResolver.openOutputStream(downloadUri)?.use { outputStream ->
                                    val input = BufferedInputStream(connection.inputStream)
                                    val data = ByteArray(1024)
                                    var count: Int
                                    var bytesDownloaded: Long = 0
                                    val totalBytes = connection.contentLength.toLong()

                                    while (input.read(data).also { count = it } != -1) {
                                        outputStream.write(data, 0, count)
                                        bytesDownloaded += count
                                        downloadNotification.updateProgress(bytesDownloaded, totalBytes)
                                    }
                                    input.close()
                                }
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Download completed: $filename", Toast.LENGTH_SHORT).show()
                                    downloadNotification.completeDownload(
                                        uri = downloadUri,
                                        mimeType = contentValues.getAsString(MediaStore.Downloads.MIME_TYPE) // or your specific MIME type
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("Download", "Error: ${e.message}")
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                if (downloadNotification != null) {
                                    downloadNotification.failDownload(e.message ?: "Unknown error")
                                }
                            }
                        }
                    }
                }

                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        onProgressChanged(newProgress)
                    }

                    override fun onReceivedTitle(view: WebView?, title: String?) {
                        // Handle title changes
                    }
                }

                webViewClient = object : WebViewClient() {
                    override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                        handler?.proceed()
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        url?.let {
                            context.getSharedPreferences("device_manager", Context.MODE_PRIVATE)
                                .edit()
                                .putString("saved_url", it)
                                .apply()
                        }
                    }
                }

                loadUrl(url)
                onWebViewCreated(this)
            }
        },
        modifier = modifier
    )
}
