import android.graphics.Color
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext


import androidx.browser.customtabs.CustomTabColorSchemeParams


@Composable
fun WebViewScreen(url: String) {
    val context = LocalContext.current
    val colorSchemeParams = CustomTabColorSchemeParams.Builder()
        .setToolbarColor(Color.TRANSPARENT)
        .build()
        
    val customTabsIntent = CustomTabsIntent.Builder()
        .setShowTitle(false)
        .setDefaultColorSchemeParams(colorSchemeParams)
        .build()
    
    val finalUrl = when {
        url.startsWith("http://") || url.startsWith("https://") -> url
        else -> "https://$url"
    }
    
    customTabsIntent.launchUrl(context, Uri.parse(finalUrl))
}
