package ca.benoitjdumont.simpletv

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
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

private val Navy = Color(0xFF102B46)
private val PaleBlue = Color(0xFFDCEEFF)
private val AccentBlue = Color(0xFF2678B8)
private val CardBlue = Color(0xFF1B4265)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { SimpleTvHome() } }
    }
}

@Composable
private fun SimpleTvHome() {
    val context = LocalContext.current
    Surface(Modifier.fillMaxSize(), color = Navy) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 56.dp, vertical = 36.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Simple TV", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                    Text("Votre télé. Rien de plus.", color = PaleBlue, fontSize = 17.sp)
                }
                FocusCard("⚙  Paramètres", "Réglages Android") {
                    context.startActivity(Intent(Settings.ACTION_SETTINGS))
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Text("Vos applications", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    FocusCard("Applications", "Choisir vos applications", Modifier.weight(1f)) {
                        context.startActivity(Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS))
                    }
                    FocusCard("Personnaliser", "Langue et apparence", Modifier.weight(1f)) {
                        context.startActivity(Intent(Settings.ACTION_SETTINGS))
                    }
                    FocusCard("Compte", "Comptes de l'appareil", Modifier.weight(1f)) {
                        context.startActivity(Intent(Settings.ACTION_SYNC_SETTINGS))
                    }
                }
            }
            Text("Simple, rapide et à votre image.", color = PaleBlue.copy(alpha = 0.8f), fontSize = 15.sp)
        }
    }
}

@Composable
private fun FocusCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    Column(
        modifier.height(118.dp)
            .background(if (focused) AccentBlue else CardBlue, RoundedCornerShape(22.dp))
            .onFocusChanged { focused = it.isFocused }
            .clickable(onClick = onClick)
            .focusable()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(subtitle, color = PaleBlue, fontSize = 13.sp)
    }
}
