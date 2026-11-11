package dev.nearchess
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.text.DateFormat
import java.util.Date

@Composable fun ArchiveScreen(onBack:()->Unit){
 val context=LocalContext.current;val games=remember{GameArchive(context).all()}
 var selected by remember{mutableStateOf<ArchivedGame?>(null)}
 var mode by remember{mutableStateOf("all")};var result by remember{mutableStateOf("all")}
 val g=selected
 if(g!=null){MaterialReview(g,onBack={selected=null});return}
 val filtered=games.filter{(mode=="all" || it.mode==mode)&&(result=="all" || it.result==result)}
 Surface(Modifier.fillMaxSize()){
  LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
   item{TextButton(onClick=onBack){Text("Назад")};Text("Твои партии",style=MaterialTheme.typography.headlineSmall);Text("${games.size} сохранено · ${filtered.size} показано",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
   item{LazyRow(horizontalArrangement=Arrangement.spacedBy(6.dp)){item{listOf("all" to "Все","bot" to "Бот","local" to "Локально").forEach{(id,label)->FilterChip(selected=mode==id,onClick={mode=id},shape=CircleShape,label={Text(label)})}}}}
   item{LazyRow(horizontalArrangement=Arrangement.spacedBy(6.dp)){item{listOf("all" to "Любой исход","1-0" to "1–0","0-1" to "0–1","1/2-1/2" to "½–½").forEach{(id,label)->FilterChip(selected=result==id,onClick={result=id},shape=CircleShape,label={Text(label)})}}}}
   if(filtered.isEmpty())item{Surface(shape=MaterialTheme.shapes.extraLarge,color=MaterialTheme.colorScheme.surfaceContainerLow){Column(Modifier.fillMaxWidth().padding(24.dp)){Text(if(games.isEmpty())"Первая партия ещё впереди" else "Ничего не найдено",style=MaterialTheme.typography.titleMedium);Text("Завершённые партии появятся здесь вместе с разбором материала.",style=MaterialTheme.typography.bodyMedium)}}}
   items(filtered,key={it.id}){game->Surface(onClick={selected=game},shape=MaterialTheme.shapes.large,color=MaterialTheme.colorScheme.surfaceContainerLow){
    ListItem(colors=ListItemDefaults.colors(containerColor=MaterialTheme.colorScheme.surfaceContainerLow),headlineContent={Text(game.opponent)},supportingContent={Column{
     Text(DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT).format(Date(game.finished)))
     val seconds=game.duration/1000
     Text("${if(game.mode=="bot")"Бот" else "Локально"} · ${seconds/60} мин ${seconds%60} с")
     Text("${game.notation.size} полуходов · открыть разбор")
    }},trailingContent={Surface(shape=CircleShape,color=MaterialTheme.colorScheme.secondaryContainer){Text(game.result,Modifier.padding(horizontal=12.dp,vertical=8.dp),style=MaterialTheme.typography.labelLarge)}})
   }}
  }
 }
}
