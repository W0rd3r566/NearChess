package dev.nearchess
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable fun BotSetup(onBack:()->Unit,onStart:(BotLevel,Boolean)->Unit){
 val context=LocalContext.current
 val prefs=remember{context.getSharedPreferences("bot_setup",0)}
 var chosen by remember{mutableStateOf(BotLevel.entries[prefs.getInt("level",2).coerceIn(0..7)])}
 var white by remember{mutableStateOf(prefs.getBoolean("white",true))}
 var page by remember{mutableStateOf(0)}
 if(page==1){PuzzleTrainer(onBack={page=0});return}
 if(page==2){ArchiveScreen(onBack={page=0});return}
 BackHandler(onBack=onBack)
 Scaffold(bottomBar={Surface(tonalElevation=2.dp){Button(onClick={prefs.edit().putInt("level",chosen.ordinal).putBoolean("white",white).apply();onStart(chosen,white)},modifier=Modifier.navigationBarsPadding().fillMaxWidth().padding(16.dp)){Text("Начать · ${chosen.title}")}}}){padding->
  LazyColumn(Modifier.fillMaxSize().padding(padding),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
   item{TextButton(onClick=onBack){Text("Назад")};Text("Тренировка",style=MaterialTheme.typography.headlineSmall)}
   item{FilledTonalButton(onClick={page=1},modifier=Modifier.fillMaxWidth()){Text("Шахматные задачи · офлайн CSV")};TextButton(onClick={page=2}){Text("История партий")}}
   item{Text("Цвет фигур",style=MaterialTheme.typography.titleMedium);Row{FilterChip(selected=white,onClick={white=true},label={Text("Белые")});Spacer(Modifier.width(8.dp));FilterChip(selected=!white,onClick={white=false},label={Text("Чёрные")})}}
   itemsIndexed(BotLevel.entries){i,level->
    Card(onClick={chosen=level},colors=CardDefaults.cardColors(containerColor=if(chosen==level)MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow)){
     ListItem(headlineContent={Text(level.title)},supportingContent={Text("Уровень ${i+1} · глубина до ${level.depth}")},trailingContent={RadioButton(selected=chosen==level,onClick=null)})
    }
   }
   item{Text("Темп ответа: естественный. Встроенный поиск не имеет измеренного Elo. Задачи импортируются отдельно; учебные уроки пока не добавлены.",style=MaterialTheme.typography.bodySmall)}
  }
 }
}
