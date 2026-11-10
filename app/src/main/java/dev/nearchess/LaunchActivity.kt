package dev.nearchess
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class LaunchActivity: ComponentActivity(){
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  setContent{MaterialTheme(colorScheme=darkColorScheme()) {Surface(Modifier.fillMaxSize()) {
   Column(Modifier.systemBarsPadding().padding(24.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
    Text("NearChess",style=MaterialTheme.typography.displaySmall)
    Text("Шахматы рядом. Интернет не нужен.",style=MaterialTheme.typography.bodyLarge)
    Button(onClick={startActivity(Intent(this@LaunchActivity,NearbyActivity::class.java))},modifier=Modifier.fillMaxWidth()){Text("С другом по Bluetooth")}
    FilledTonalButton(onClick={startActivity(Intent(this@LaunchActivity,MainActivity::class.java))},modifier=Modifier.fillMaxWidth()){Text("Локальная игра и тренировка")}
    OutlinedButton(onClick={startActivity(Intent(this@LaunchActivity,ProfileActivity::class.java))},modifier=Modifier.fillMaxWidth()){Text("Ник и аватар")}
    Text("Тестовая версия. Bluetooth: первая пара через системные настройки. Онлайн-сервера нет. Часы и переподключение ещё не реализованы.",style=MaterialTheme.typography.bodySmall)
   }
  }}}
 }
}
