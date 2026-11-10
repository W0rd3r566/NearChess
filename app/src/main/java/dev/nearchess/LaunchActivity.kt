package dev.nearchess

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

class LaunchActivity: ComponentActivity(){
 override fun onCreate(savedInstanceState: Bundle?) {super.onCreate(savedInstanceState);setContent{NearTheme{LaunchScreen()}}}
}
@Composable private fun LaunchScreen() {
 val context=LocalContext.current; var tab by remember{mutableStateOf(0)}; val wide=LocalConfiguration.current.screenWidthDp>=600
 Scaffold(containerColor=MaterialTheme.colorScheme.surface,bottomBar={NavigationBar {
  NavigationBarItem(tab==0,{tab=0},{Icon(Icons.Filled.SportsEsports,"Играть")},{Text("Играть")})
  NavigationBarItem(tab==1,{tab=1},{Icon(Icons.Filled.School,"Тренировка")},{Text("Тренировка")})
  NavigationBarItem(tab==2,{tab=2;context.startActivity(Intent(context,ProfileActivity::class.java))},{Icon(Icons.Filled.Person,"Профиль")},{Text("Профиль")})
 }}) { p ->
  BoxWithConstraints(Modifier.fillMaxSize().padding(p)) {
   val max=if(wide) 840.dp else 600.dp
   Column(Modifier.fillMaxWidth().widthIn(max=max).align(Alignment.TopCenter).padding(horizontal=NearTokens.Space4,vertical=NearTokens.Space3),verticalArrangement=Arrangement.spacedBy(NearTokens.Space4)) {
    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {NearLogo(Modifier.size(56.dp));Spacer(Modifier.width(NearTokens.Space3));Column(Modifier.weight(1f)){Text("NearChess",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);Text("Шахматы рядом",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)};IconButton(onClick={context.startActivity(Intent(context,ProfileActivity::class.java))}){Icon(Icons.Filled.Person,"Профиль")}}
    AnimatedContent(tab,transitionSpec={fadeIn() togetherWith fadeOut()},label="home content") { selected ->
     Column(verticalArrangement=Arrangement.spacedBy(NearTokens.Space4)) {
      Text(if(selected==0) "Играть" else "Тренировка",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold)
      if(selected==0) {
       Card(Modifier.fillMaxWidth(),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.primaryContainer),shape=MaterialTheme.shapes.extraLarge) {Column(Modifier.padding(NearTokens.Space5),verticalArrangement=Arrangement.spacedBy(NearTokens.Space3)){Text("С другом рядом",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Text("Bluetooth · без интернета · без аккаунта");Button(onClick={context.startActivity(Intent(context,NearbyActivity::class.java))},modifier=Modifier.fillMaxWidth()){Text("Создать или присоединиться")}}}
       Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(NearTokens.Space3)){FilledTonalButton(onClick={context.startActivity(Intent(context,MainActivity::class.java))},modifier=Modifier.weight(1f).height(72.dp)){Text("Один телефон",fontWeight=FontWeight.SemiBold)};FilledTonalButton(onClick={context.startActivity(Intent(context,TimedActivity::class.java))},modifier=Modifier.weight(1f).height(72.dp)){Text("Блиц\n3+2 · 5+0",fontWeight=FontWeight.SemiBold)}}
       Text("Недавнее",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);OutlinedCard(Modifier.fillMaxWidth()){ListItem(headlineContent={Text("Офлайн-партия")},supportingContent={Text("Продолжить сохранённую игру")},trailingContent={TextButton(onClick={context.startActivity(Intent(context,MainActivity::class.java))}){Text("Открыть")}})}
      } else {Card(Modifier.fillMaxWidth(),shape=MaterialTheme.shapes.extraLarge){ListItem(headlineContent={Text("Против бота")},supportingContent={Text("Тренировка без рейтинга")},trailingContent={Button(onClick={context.startActivity(Intent(context,MainActivity::class.java))}){Text("Играть")}})};Text("Скоро: задачи, подсказки и Stockfish",color=MaterialTheme.colorScheme.onSurfaceVariant)}
     }
    }
   }
  }
 }
}
