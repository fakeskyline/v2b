package com.v2b

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.net.URLEncoder

private val Dark = Color(0xFF202124)
private val Card = Color(0xFF2B2D31)
private val Blue = Color(0xFF72A7FF)
private val White = Color(0xFFF1F2F3)
private val Gray = Color(0xFF9AA0A6)
private enum class Screen { HOME, WEB, EXTENSIONS }
private data class TabItem(val id: Int, var url: String? = null, var title: String = "New tab")

class MainActivity : androidx.activity.ComponentActivity() {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(32, 33, 36)
        window.navigationBarColor = android.graphics.Color.rgb(32, 33, 36)
        setContent { V2B() }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun V2B() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val keyboard = LocalSoftwareKeyboardController.current
    val focus = LocalFocusManager.current
    var screen by remember { mutableStateOf(Screen.HOME) }
    var input by remember { mutableStateOf("") }
    var activeTab by remember { mutableIntStateOf(1) }
    var nextTab by remember { mutableIntStateOf(1) }
    var showTabs by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(1f) }
    val tabs = remember { mutableStateListOf(TabItem(1)) }
    val views = remember { mutableMapOf<Int, WebView>() }

    fun urlFor(raw: String): String {
        val s = raw.trim()
        if (s.isBlank()) return ""
        if (s.startsWith("http://") || s.startsWith("https://")) return s
        if (s.contains(" ") || !s.contains(".")) return "https://html.duckduckgo.com/html/?q=" + URLEncoder.encode(s, "UTF-8")
        return "https://$s"
    }
    fun go(raw: String) {
        val url = urlFor(raw)
        if (url.isBlank()) return
        input = url
        tabs.find { it.id == activeTab }?.url = url
        screen = Screen.WEB
        keyboard?.hide()
        focus.clearFocus()
        views[activeTab]?.loadUrl(url)
    }
    fun home() {
        screen = Screen.HOME
        input = ""
        tabs.find { it.id == activeTab }?.url = null
        keyboard?.hide()
        focus.clearFocus()
    }
    fun newTab() {
        nextTab++
        tabs.add(TabItem(nextTab))
        activeTab = nextTab
        input = ""
        screen = Screen.HOME
        showTabs = false
    }
    fun select(tab: TabItem) {
        activeTab = tab.id
        input = tab.url.orEmpty()
        screen = if (tab.url == null) Screen.HOME else Screen.WEB
        showTabs = false
    }
    fun createView(id: Int) = WebView(context).apply {
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.loadsImagesAutomatically = true
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.javaScriptCanOpenWindowsAutomatically = true
        webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, p: Int) { if (id == activeTab) progress = p / 100f }
            override fun onReceivedTitle(view: WebView, title: String?) { if (!title.isNullOrBlank()) tabs.find { it.id == id }?.title = title }
        }
        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: android.webkit.WebResourceRequest): Boolean =
                request.url.scheme !in listOf("https", "http")
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                if (id == activeTab) { input = url; tabs.find { it.id == id }?.url = url; progress = 0.05f }
            }
            override fun onPageFinished(view: WebView, url: String) {
                if (id == activeTab) { input = url; tabs.find { it.id == id }?.url = url; progress = 1f }
            }
        }
        views[id] = this
    }

    BackHandler(screen != Screen.HOME || showTabs) {
        if (showTabs) showTabs = false
        else if (screen == Screen.EXTENSIONS) home()
        else if (screen == Screen.WEB) {
            if (views[activeTab]?.canGoBack() == true) views[activeTab]?.goBack() else home()
        }
    }

    MaterialTheme(colorScheme = darkColorScheme(primary = Blue, background = Dark, surface = Card)) {
        Column(Modifier.fillMaxSize().background(Dark)) {
            when (screen) {
                Screen.HOME -> Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                    Spacer(Modifier.height(20.dp))
                    Column(Modifier.weight(1f).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Image(painterResource(R.drawable.logotype), "v2b", Modifier.fillMaxWidth(.62f).heightIn(max = 100.dp))
                        Spacer(Modifier.height(24.dp))
                        OutlinedTextField(
                            value = input, onValueChange = { input = it }, modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp),
                            singleLine = true, textStyle = androidx.compose.ui.text.TextStyle(color = White, fontSize = 16.sp),
                            placeholder = { Text("Search or enter address", color = Gray) },
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = Blue) },
                            trailingIcon = { if (input.isNotEmpty()) IconButton(onClick = { input = "" }) { Icon(Icons.Default.Close, null, tint = Gray) } },
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Card, unfocusedContainerColor = Card, focusedBorderColor = Blue, unfocusedBorderColor = Color.Transparent, cursorColor = Blue),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { keyboard?.hide(); focus.clearFocus(); go(input) })
                        )
                    }
                    Row(Modifier.fillMaxWidth().padding(18.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MenuButton(Icons.Default.Tab, "Tabs", { showTabs = true }, Modifier.weight(1f))
                        MenuButton(Icons.Default.Extension, "Extensions", { screen = Screen.EXTENSIONS }, Modifier.weight(1f))
                    }
                }
                Screen.EXTENSIONS -> Column(Modifier.fillMaxSize().padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { home() }) { Icon(Icons.Default.ArrowBack, null, tint = White) }
                        Text("Extensions", color = White, fontSize = 22.sp)
                    }
                    Spacer(Modifier.height(18.dp))
                    Surface(color = Card, shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Extension, null, tint = Blue, modifier = Modifier.size(46.dp))
                            Spacer(Modifier.height(12.dp))
                            Text("CRX support isn't available yet", color = White, fontSize = 18.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("This prototype uses Android's Chromium-based WebView. WebView does not include the Chrome Extensions runtime. Real CRX support needs a separate extension-capable Chromium integration.", color = Gray, fontSize = 14.sp, lineHeight = 20.sp)
                        }
                    }
                }
                Screen.WEB -> {
                    Row(Modifier.background(Dark).padding(horizontal = 5.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (views[activeTab]?.canGoBack() == true) views[activeTab]?.goBack() else home() }, modifier = Modifier.size(36.dp) { Icon(Icons.Default.ArrowBack, null, tint = White) }
                        IconButton(onClick = { views[activeTab]?.takeIf { it.canGoForward() }?.goForward() }, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.ArrowForward, null, tint = Gray) }
                        OutlinedTextField(value = input, onValueChange = { input = it }, modifier = Modifier.weight(1f).heightIn(min = 48.dp), singleLine = true,
                            placeholder = { Text("Address or search", color = Gray, fontSize = 12.sp) }, textStyle = androidx.compose.ui.text.TextStyle(color = White, fontSize = 12.sp),
                            shape = RoundedCornerShape(18.dp), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Card, unfocusedContainerColor = Card, focusedBorderColor = Blue, unfocusedBorderColor = Color.Transparent, cursorColor = Blue),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go), keyboardActions = KeyboardActions(onGo = { go(input) }),
                            leadingIcon = { Icon(Icons.Default.Lock, null, tint = Gray, modifier = Modifier.size(16.dp)) })
                        IconButton(onClick = { views[activeTab]?.reload() }, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Refresh, null, tint = White) }
                        IconButton(onClick = { showTabs = true }, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Tab, null, tint = White) }
                    }
                    if (progress < 1f) LinearProgressIndicator({ progress }, Modifier.fillMaxWidth().height(2.dp), color = Blue, trackColor = Dark)
                    key(activeTab) {
                        AndroidView(factory = { createView(activeTab).also { tabs.find { t -> t.id == activeTab }?.url?.let(it::loadUrl) } },
                            update = { wv -> tabs.find { it.id == activeTab }?.url?.let { if (wv.url != it) wv.loadUrl(it) } },
                            modifier = Modifier.weight(1f))
                    }
                    Row(Modifier.fillMaxWidth().background(Dark).padding(horizontal = 8.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { home() }) { Icon(Icons.Default.Home, null, tint = Blue) }
                        Spacer(Modifier.weight(1f))
                        Text("v2b", color = Blue, fontSize = 12.sp)
                    }
                }
            }
        }
        if (showTabs) Dialog(onDismissRequest = { showTabs = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Surface(color = Card, shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Tabs", color = White, fontSize = 20.sp)
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { newTab() }) { Icon(Icons.Default.Add, null, tint = Blue) }
                    }
                    LazyColumn(Modifier.heightIn(max = 340.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(tabs, key = { it.id }) { tab ->
                            Surface(onClick = { select(tab) }, color = if (tab.id == activeTab) Color(0xFF3B3D42) else Dark, shape = RoundedCornerShape(14.dp)) {
                                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Language, null, tint = Blue)
                                    Spacer(Modifier.width(10.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(tab.title, color = White, maxLines = 1)
                                        Text(tab.url ?: "New tab", color = Gray, fontSize = 12.sp, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuButton(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, action: () -> Unit, modifier: Modifier = Modifier) {
    Surface(onClick = action, color = Card, shape = RoundedCornerShape(20.dp), modifier = modifier) {
        Column(Modifier.padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = Blue, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(5.dp))
            Text(title, color = White, fontSize = 14.sp)
        }
    }
}
