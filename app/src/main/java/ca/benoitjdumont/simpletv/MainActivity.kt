package ca.benoitjdumont.simpletv

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.net.Uri
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.provider.Settings
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

private val Navy = Color(0xFF102B46)
private val PaleBlue = Color(0xFFDCEEFF)
private val AccentBlue = Color(0xFF2678B8)
private val CardBlue = Color(0xFF1B4265)

private data class TvApp(
    val packageName: String,
    val activityName: String,
    val label: String,
    val icon: android.graphics.drawable.Drawable
) {
    val id: String get() = "$packageName/$activityName"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { SimpleTvHome() } }
    }
}

@Composable
private fun SimpleTvHome() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("simple_tv", Context.MODE_PRIVATE) }
    var language by remember { mutableStateOf(prefs.getString("language", "fr") ?: "fr") }
    var favoritesOnly by remember { mutableStateOf(false) }
    var apps by remember { mutableStateOf(emptyList<TvApp>()) }
    var updateMessage by remember { mutableStateOf("Vérifier les mises à jour") }
    var releaseUrl by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val favorites = remember {
        mutableStateListOf<String>().apply {
            addAll(prefs.getStringSet("favorites", emptySet()) ?: emptySet())
        }
    }

    fun loadApps() {
        val pm = context.packageManager
        val found = linkedMapOf<String, TvApp>()
        val categories = listOf("android.intent.category.LEANBACK_LAUNCHER", Intent.CATEGORY_LAUNCHER)
        categories.forEach { category ->
            val query = Intent(Intent.ACTION_MAIN).addCategory(category)
            @Suppress("DEPRECATION")
            val results: List<ResolveInfo> = pm.queryIntentActivities(query, 0)
            results.forEach resultLoop@{ info ->
                val activity = info.activityInfo ?: return@resultLoop
                if (activity.packageName == context.packageName) return@resultLoop
                val item = TvApp(activity.packageName, activity.name, info.loadLabel(pm)?.toString() ?: activity.packageName, info.loadIcon(pm))
                found.putIfAbsent(item.id, item)
            }
        }
        apps = found.values.sortedBy { it.label.lowercase() }
    }

    LaunchedEffect(Unit) { loadApps() }
    val visibleApps = apps.filter { !favoritesOnly || favorites.contains(it.id) }
    val labels = when (language) {
        "en" -> listOf("Your apps", "Favorites", "All apps", "Settings", "No apps found", "No favorites yet — select ☆ on an app", "Choose the apps shown on your home screen", "Check for updates")
        "es" -> listOf("Tus aplicaciones", "Favoritos", "Todas", "Ajustes", "No se encontraron aplicaciones", "Aún no hay favoritos — selecciona ☆", "Elige las aplicaciones de tu pantalla de inicio", "Buscar actualizaciones")
        else -> listOf("Vos applications", "Favoris", "Toutes", "Paramètres", "Aucune application trouvée", "Aucun favori — sélectionnez ☆ sur une application", "Choisissez les applications affichées sur votre écran d’accueil", "Vérifier les mises à jour")
    }

    Surface(Modifier.fillMaxSize(), color = Navy) {
        Column(Modifier.fillMaxSize().padding(horizontal = 48.dp, vertical = 28.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Simple TV", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                    Text("Votre télé. Rien de plus.", color = PaleBlue, fontSize = 16.sp)
                }
                FocusCard(language.uppercase(), "FR · EN · ES", Modifier.width(104.dp).height(68.dp)) {
                    language = when (language) { "fr" -> "en"; "en" -> "es"; else -> "fr" }
                    prefs.edit().putString("language", language).apply()
                }
                Spacer(Modifier.width(12.dp))
                FocusCard("↻", labels[7], Modifier.width(156.dp).height(68.dp)) {
                    scope.launch {
                        updateMessage = if (language == "en") "Checking GitHub…" else if (language == "es") "Consultando GitHub…" else "Vérification de GitHub…"
                        val result = withContext(Dispatchers.IO) { checkLatestRelease(context.packageName, context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0") }
                        updateMessage = result.first
                        releaseUrl = result.second
                        if (result.second != null) {
                            try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(result.second))) } catch (_: Exception) { }
                        }
                    }
                }
                Spacer(Modifier.width(10.dp))
                FocusCard("⚙", labels[3], Modifier.width(110.dp).height(68.dp)) {
                    context.startActivity(Intent(Settings.ACTION_SETTINGS))
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(labels[0], color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                FocusCard(labels[2], "Afficher tout", Modifier.width(132.dp).height(58.dp)) { favoritesOnly = false }
                FocusCard(labels[1], "★ ${favorites.size}", Modifier.width(142.dp).height(58.dp)) { favoritesOnly = true }
            }
            if (visibleApps.isEmpty()) {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text(if (apps.isEmpty()) labels[4] else labels[5], color = PaleBlue, fontSize = 20.sp)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 176.dp),
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(visibleApps, key = { it.id }) { app ->
                        AppCard(app, favorites.contains(app.id), onLaunch = {
                            try {
                                val intent = Intent(Intent.ACTION_MAIN).apply {
                                    component = ComponentName(app.packageName, app.activityName)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) { }
                        }, onFavorite = {
                            if (favorites.contains(app.id)) favorites.remove(app.id) else favorites.add(app.id)
                            prefs.edit().putStringSet("favorites", favorites.toSet()).apply()
                        })
                    }
                }
            }
            Text(updateMessage, color = PaleBlue.copy(alpha = 0.78f), fontSize = 13.sp)
            Text(labels[6], color = PaleBlue.copy(alpha = 0.72f), fontSize = 14.sp)
        }
    }
}

@Composable
private fun AppCard(app: TvApp, isFavorite: Boolean, onLaunch: () -> Unit, onFavorite: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    Row(
        Modifier.fillMaxWidth().height(104.dp).background(if (focused) AccentBlue else CardBlue, RoundedCornerShape(22.dp))
            .onFocusChanged { focused = it.isFocused }.clickable(onClick = onLaunch).focusable()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AndroidView(factory = { ImageView(it).apply { scaleType = ImageView.ScaleType.FIT_CENTER } },
            update = { it.setImageDrawable(app.icon) }, modifier = Modifier.size(52.dp))
        Spacer(Modifier.width(14.dp))
        Text(app.label, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), maxLines = 2)
        Text(if (isFavorite) "★" else "☆", color = PaleBlue, fontSize = 25.sp,
            modifier = Modifier.clickable(onClick = onFavorite).padding(start = 8.dp))
    }
}

@Composable
private fun FocusCard(title: String, subtitle: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    Column(modifier.background(if (focused) AccentBlue else CardBlue, RoundedCornerShape(20.dp))
        .onFocusChanged { focused = it.isFocused }.clickable(onClick = onClick).focusable()
        .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        Text(subtitle, color = PaleBlue, fontSize = 11.sp, maxLines = 1)
    }
}
private suspend fun checkLatestRelease(packageName: String, installedVersion: String): Pair<String, String?> = withContext(Dispatchers.IO) {
    try {
        val connection = (URL("https://api.github.com/repos/benoitjdumont/Simple-TV/releases/latest").openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8000
            readTimeout = 8000
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "Simple-TV-Android")
        }
        val code = connection.responseCode
        if (code == 404) return@withContext "Aucune version publiée pour le moment." to null
        if (code !in 200..299) return@withContext "GitHub indisponible (HTTP $code)." to null
        val json = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
        val tag = json.optString("tag_name").removePrefix("v")
        val url = json.optString("html_url")
        val latestParts = tag.split(".").mapNotNull { it.toIntOrNull() }
        val installedParts = installedVersion.removePrefix("v").split(".").mapNotNull { it.toIntOrNull() }
        fun compare(a: List<Int>, b: List<Int>): Int {
            for (i in 0 until maxOf(a.size, b.size)) {
                val diff = (a.getOrElse(i) { 0 }).compareTo(b.getOrElse(i) { 0 })
                if (diff != 0) return diff
            }
            return 0
        }
        if (tag.isBlank() || url.isBlank()) return@withContext "Réponse GitHub invalide." to null
        if (compare(latestParts, installedParts) > 0) "Nouvelle version $tag disponible. Ouvrir la page GitHub pour installer." to url
        else "Simple TV est à jour (version $installedVersion)." to null
    } catch (_: Exception) {
        "Impossible de vérifier les mises à jour. Réessayez plus tard." to null
    }
}
