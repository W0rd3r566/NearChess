package dev.nearchess
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.text.DateFormat
import java.util.Date

@Composable fun ArchiveScreen(onBack:()->Unit) {
 val context=LocalContext.current
 val games=remember{GameArchive(context).all()}
 var selected by remember{mutableStateOf<ArchivedGame?>(null)}
 val g=selected
 if(g!=null) {
  MatchScreen(title="Архив · ${g.opponent}",history=g.positions,notation=g.notation,lastMove=null,enabled=false,whiteName="Белые",blackName="Чёрные",status=g.result,onBack={selected=null},onMove={},menuContent={Text("Только просмотр. Результат ${g.result}")})
  return
 }
 Surface(Modifier.fillMaxSize()) {
  LazyColumn(Modifier.systemBarsPadding().fillMaxSize(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
   item{TextButton(onClick=onBack){Text("Назад")};Text("История и статистика",style=MaterialTheme.typography.headlineMedium);Text("Завершено партий: ${games.size}")}
   if(games.isEmpty())item{Text("Пока нет завершённых партий. Закончи локальную игру или тренировку — она появится здесь.")}
   games.groupBy{it.mode}.forEach{(mode,list)->item{Text("${if(mode=="bot")"Против бота" else "Локально"}: ${list.size} · ничьих ${list.count{it.result=="1/2-1/2"}}",style=MaterialTheme.typography.titleSmall)}}
   items(games,key={it.id}){game->
    OutlinedCard(onClick={selected=game},modifier=Modifier.fillMaxWidth()){
     ListItem(headlineContent={Text(game.opponent)},supportingContent={Text("${DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT).format(Date(game.finished))} · ${game.notation.size} полуходов")},trailingContent={Text(game.result,style=MaterialTheme.typography.titleMedium)})
    }
   }
  }
 }
}
