package dev.nearchess
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

class LaunchActivity: ComponentActivity(){
 override fun onCreate(savedInstanceState: Bundle?) {super.onCreate(savedInstanceState);setContent{NearTheme{LaunchScreen()}}}
}
@Composable private fun LaunchScreen() {
 val context=LocalContext.current
 Scaffold(containerColor=MaterialTheme.colorScheme.background) { padding ->
  Column(Modifier.fillMaxSize().padding(padding).padding(horizontal=24.dp,vertical=32.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(16.dp)) {
   Spacer(Modifier.height(12.dp)); NearLogo()
   Text("NearChess",style=MaterialTheme.typography.displaySmall,textAlign=TextAlign.Center)
   Text("Шахматы рядом",style=MaterialTheme.typography.titleLarge,color=MaterialTheme.colorScheme.onSurfaceVariant)
   Text("Играй рядом с другом — без аккаунта и интернета.",style=MaterialTheme.typography.bodyLarge,textAlign=TextAlign.Center,color=MaterialTheme.colorScheme.onSurfaceVariant)
   Spacer(Modifier.height(16.dp))
   Button(onClick={context.startActivity(Intent(context,NearbyActivity::class.java))},modifier=Modifier.fillMaxWidth().height(56.dp),shape=MaterialTheme.shapes.extraLarge){Text("С другом по Bluetooth",style=MaterialTheme.typography.labelLarge)}
   ElevatedButton(onClick={context.startActivity(Intent(context,MainActivity::class.java))},modifier=Modifier.fillMaxWidth().height(56.dp),shape=MaterialTheme.shapes.extraLarge){Text("Локальная игра и тренировка")}
   ElevatedButton(onClick={context.startActivity(Intent(context,TimedActivity::class.java))},modifier=Modifier.fillMaxWidth().height(56.dp),shape=MaterialTheme.shapes.extraLarge){Text("Одна доска · 3+2 / 5+0 / 10+5")}
   OutlinedButton(onClick={context.startActivity(Intent(context,ProfileActivity::class.java))},modifier=Modifier.fillMaxWidth().height(56.dp),shape=MaterialTheme.shapes.extraLarge){Text("Профиль и аватар")}
   Spacer(Modifier.weight(1f))
   Text("NearChess · офлайн-прототип",style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.outline)
  }
 }
}
