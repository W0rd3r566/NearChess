package dev.nearchess

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

class LaunchActivity: ComponentActivity(){
 override fun onCreate(savedInstanceState: Bundle?) {super.onCreate(savedInstanceState);setContent{NearTheme{LaunchScreen()}}}
}

@Composable private fun LaunchScreen() {
 val context=LocalContext.current
 var tab by remember{mutableStateOf(0)}
 Scaffold(
  containerColor=MaterialTheme.colorScheme.surface,
  bottomBar={
   NavigationBar(containerColor=MaterialTheme.colorScheme.surfaceContainer) {
    NavigationBarItem(selected=tab==0,onClick={tab=0},icon={Text("♟",style=MaterialTheme.typography.titleLarge)},label={Text("Играть")})
    NavigationBarItem(selected=tab==1,onClick={tab=1},icon={Text("♞",style=MaterialTheme.typography.titleLarge)},label={Text("Тренировка")})
    NavigationBarItem(selected=tab==2,onClick={
     tab=2; context.startActivity(Intent(context,ProfileActivity::class.java))
    },icon={Text("●",style=MaterialTheme.typography.titleLarge)},label={Text("Профиль")})
   }
  }
 ) { padding ->
  Column(Modifier.fillMaxSize().padding(padding).padding(horizontal=16.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
   Spacer(Modifier.height(8.dp))
   Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
    NearLogo(Modifier.size(52.dp))
    Spacer(Modifier.width(12.dp))
    Column(Modifier.weight(1f)) {
     Text("NearChess",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)
     Text("Шахматы рядом",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
    }
    FilledTonalIconButton(onClick={context.startActivity(Intent(context,ProfileActivity::class.java))}){Text("●")}
   }
   if(tab==0) {
    Text("Играть",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold)
    Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(28.dp),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.primaryContainer)) {
     Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
      Text("С другом рядом",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold,color=MaterialTheme.colorScheme.onPrimaryContainer)
      Text("Bluetooth · без интернета · без аккаунта",color=MaterialTheme.colorScheme.onPrimaryContainer)
      Button(onClick={context.startActivity(Intent(context,NearbyActivity::class.java))},modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(20.dp)) {Text("Создать или присоединиться")}
     }
    }
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
     HomeActionCard("На одном телефоне","Передавайте ход друг другу","♟",Modifier.weight(1f)){context.startActivity(Intent(context,MainActivity::class.java))}
     HomeActionCard("Блиц","3+2 · 5+0 · 10+5","◷",Modifier.weight(1f)){context.startActivity(Intent(context,TimedActivity::class.java))}
    }
    Text("Недавнее",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)
    Card(Modifier.fillMaxWidth(),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surfaceContainerLow)) {
     Row(Modifier.padding(16.dp),verticalAlignment=Alignment.CenterVertically) {
      Text("♜",style=MaterialTheme.typography.headlineMedium,color=MaterialTheme.colorScheme.primary)
      Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)){Text("Офлайн-партия");Text("Продолжить сохранённую игру",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
      TextButton(onClick={context.startActivity(Intent(context,MainActivity::class.java))}){Text("Открыть")}
     }
    }
   } else {
    Text("Тренировка",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold)
    HomeActionCard("Против бота","Простая тренировка без рейтинга","♞",Modifier.fillMaxWidth()){context.startActivity(Intent(context,MainActivity::class.java))}
    Text("Скоро: задачи, подсказки и Stockfish",color=MaterialTheme.colorScheme.onSurfaceVariant)
   }
   Spacer(Modifier.height(4.dp))
  }
 }
}

@Composable private fun HomeActionCard(title:String,subtitle:String,symbol:String,modifier:Modifier,onClick:()->Unit) {
 Card(modifier, onClick=onClick, colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surfaceContainerHigh), shape=RoundedCornerShape(22.dp)) {
  Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
   Text(symbol,style=MaterialTheme.typography.headlineMedium,color=MaterialTheme.colorScheme.primary)
   Text(title,style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold)
   Text(subtitle,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
  }
 }
}
