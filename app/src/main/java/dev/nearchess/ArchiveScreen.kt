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

@Composable fun ArchiveScreen(onBack:()->Unit){
 val context=LocalContext.current
 val games=remember{GameArchive(context).all()}
 var selected by remember{mutableStateOf<ArchivedGame?>(null)}
 var mode by remember{mutableStateOf("all")}
 var result by remember{mutableStateOf("all")}
 val g=selected
 if(g!=null){MatchScreen(title="Архив · ${g.opponent}",history=g.positions,notation=g.notation,lastMove=null,enabled=false,whiteName="Белые",blackName="Чёрные",status=g.result,onBack={selected=null},onMove={},menuContent={Text("Результат ${g.result}. Просмотр не изменяет партию.")});return}
 val filtered=games.filter{(mode=="all" || it.mode==mode)&&(result=="all" || it.result==result)}
 Surface(Modifier.fillMaxSize()){
  LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
   item{TextButton(onClick=onBack){Text("Назад")};Text("Твои партии",style=MaterialTheme.typography.headlineSmall);Text("Сохранено ${games.size} · показано ${filtered.size}",color=MaterialTheme.colorScheme.onSurfaceVariant)}
   item{Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){listOf("all" to "Все","bot" to "Бот","local" to "Локально").forEach{(id,label)->FilterChip(selected=mode==id,onClick={mode=id},label={Text(label)})}}}
   item{Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){listOf("all" to "Любой исход","1-0" to "1–0","0-1" to "0–1","1/2-1/2" to "½–½").forEach{(id,label)->FilterChip(selected=result==id,onClick={result=id},label={Text(label)})}}}
   if(filtered.isEmpty())item{Surface(shape=MaterialTheme.shapes.large,color=MaterialTheme.colorScheme.surfaceContainerLow){Column(Modifier.fillMaxWidth().padding(24.dp)){Text(if(games.isEmpty())"Первая партия ещё впереди" else "Нет подходящих партий",style=MaterialTheme.typography.titleMedium);Text("Здесь появятся реальные завершённые локальные игры и тренировки.",style=MaterialTheme.typography.bodyMedium)}}}
   items(filtered,key={it.id}){game->
    Surface(onClick={selected=game},shape=MaterialTheme.shapes.medium,color=MaterialTheme.colorScheme.surfaceContainerLow){
     ListItem(headlineContent={Text(game.opponent)},supportingContent={Column{
      Text(DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT).format(Date(game.finished)))
      val seconds=game.duration/1000
      Text("${if(game.mode=="bot")"Тренировка" else "Локально"} · ${seconds/60} мин ${seconds%60} с · ${game.notation.size} полуходов")
      if(game.level in BotLevel.entries.indices)Text(BotLevel.entries[game.level].title)
     }},trailingContent={Surface(shape=MaterialTheme.shapes.small,color=if(game.result=="1/2-1/2")MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer){Text(game.result,Modifier.padding(8.dp),style=MaterialTheme.typography.labelLarge)}})
    }
   }
  }
 }
}
