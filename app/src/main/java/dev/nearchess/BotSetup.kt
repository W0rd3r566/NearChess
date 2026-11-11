package dev.nearchess
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable fun BotSetup(onBack:()->Unit,onStart:(BotLevel,Boolean)->Unit) {
 val context=LocalContext.current
 val prefs=remember{context.getSharedPreferences("bot_setup",0)}
 var chosen by remember{mutableStateOf(BotLevel.entries[prefs.getInt("level",2).coerceIn(0..7)])}
 var white by remember{mutableStateOf(prefs.getBoolean("white",true))}
 var archive by remember{mutableStateOf(false)}
 if(archive){ArchiveScreen(onBack={archive=false});return}
 val labels=listOf("400","700","1000","1300","1600","1900","2200","2500+")
 val styles=listOf("Простые взятия","Расчёт ответа соперника","Развитие и центр","Больше тактических вариантов","Глубже оценивает позицию","Увеличенный поиск","Длительный расчёт","Максимальный бюджет встроенного поиска")
 Scaffold(bottomBar={Surface(tonalElevation=3.dp){Button(onClick={prefs.edit().putInt("level",chosen.ordinal).putBoolean("white",white).apply();onStart(chosen,white)},modifier=Modifier.navigationBarsPadding().fillMaxWidth().padding(16.dp)){Text("Начать · ${chosen.title}")}}}){padding->
  LazyColumn(Modifier.fillMaxSize().padding(padding),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
   item{Row{TextButton(onClick=onBack){Text("Назад")};TextButton(onClick={archive=true}){Text("История и статистика")}};Text("Выбери соперника",style=MaterialTheme.typography.headlineMedium);Text("Числа — ориентиры, не измеренный Elo. Встроенный поиск не гарантирует силу мастера.",style=MaterialTheme.typography.bodySmall)}
   item{Row{FilterChip(selected=white,onClick={white=true},label={Text("За белых")});Spacer(Modifier.width(8.dp));FilterChip(selected=!white,onClick={white=false},label={Text("За чёрных")})};Text("Без часов · тренировочный режим",style=MaterialTheme.typography.labelMedium)}
   itemsIndexed(BotLevel.entries){i,level->
    Card(onClick={chosen=level},colors=CardDefaults.cardColors(containerColor=if(level==chosen)MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer)) {
     ListItem(headlineContent={Text(if(i==7)"Гроссмейстер · максимум" else level.title)},supportingContent={Text(styles[i])},leadingContent={Text("${i+1}",style=MaterialTheme.typography.headlineSmall)},trailingContent={RadioButton(selected=level==chosen,onClick={chosen=level})})
     Text("Ориентир ${labels[i]} · глубина до ${level.depth}",Modifier.padding(start=16.dp,bottom=12.dp),style=MaterialTheme.typography.labelSmall)
    }
   }
  }
 }
}
