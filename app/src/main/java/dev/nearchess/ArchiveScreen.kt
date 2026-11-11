package dev.nearchess
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable fun ArchiveScreen(onBack:()->Unit){
 val context=LocalContext.current;val archive=remember{GameArchive(context)}
 var games by remember{mutableStateOf(archive.all())}
 var selected by remember{mutableStateOf<ArchivedGame?>(null)}
 var mode by remember{mutableStateOf("all")};var result by remember{mutableStateOf("all")}
 var busy by remember{mutableStateOf(false)};var message by remember{mutableStateOf("")}
 val scope=rememberCoroutineScope()
 val picker=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri->if(uri!=null)scope.launch{
  busy=true
  val imported=withContext(Dispatchers.IO){runCatching{
   val text=context.contentResolver.openInputStream(uri)?.bufferedReader()?.use{reader->
    val out=StringBuilder();val buffer=CharArray(8192)
    while(true){val n=reader.read(buffer);if(n<0)break;require(out.length+n<=2_000_000){"PGN превышает 2 МБ текста"};out.append(buffer,0,n)}
    out.toString()
   }?:error("Не удалось открыть файл")
   val parsed=PgnImport.parse(text)
   val id=java.util.UUID.nameUUIDFromBytes(text.toByteArray(Charsets.UTF_8)).toString()
   val game=ArchivedGame(id,"import",listOf(parsed.tags["White"]?:"Белые",parsed.tags["Black"]?:"Чёрные").joinToString(" — "),parsed.result,System.currentTimeMillis(),0,-1,parsed.positions,parsed.notation)
   archive.record(game);game
  }}
  imported.onSuccess{games=archive.all();message="PGN импортирован";selected=it}.onFailure{message=it.message?:"Ошибка импорта"};busy=false
 }}
 val g=selected
 if(g!=null){MaterialReview(g,onBack={selected=null});return}
 val filtered=games.filter{(mode=="all"||it.mode==mode)&&(result=="all"||it.result==result)}
 Surface(Modifier.fillMaxSize()){
  LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
   item{Row{TextButton(onClick=onBack){Text("Назад")};TextButton(enabled=!busy,onClick={picker.launch(arrayOf("*/*"))}){Text("Импорт PGN")}};Text("Твои партии",style=MaterialTheme.typography.headlineSmall);Text("${games.size} сохранено · ${filtered.size} показано",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant);if(busy)LinearProgressIndicator(Modifier.fillMaxWidth());if(message.isNotEmpty())Text(message,style=MaterialTheme.typography.bodySmall)}
   item{LazyRow(horizontalArrangement=Arrangement.spacedBy(6.dp)){items(listOf("all" to "Все","bot" to "Бот","local" to "Локально","import" to "Импорт")){(id,label)->FilterChip(selected=mode==id,onClick={mode=id},shape=CircleShape,label={Text(label)})}}}
   item{LazyRow(horizontalArrangement=Arrangement.spacedBy(6.dp)){items(listOf("all" to "Любой исход","1-0" to "1–0","0-1" to "0–1","1/2-1/2" to "½–½")){(id,label)->FilterChip(selected=result==id,onClick={result=id},shape=CircleShape,label={Text(label)})}}}
   if(filtered.isEmpty())item{Surface(shape=MaterialTheme.shapes.extraLarge,color=MaterialTheme.colorScheme.surfaceContainerLow){Column(Modifier.fillMaxWidth().padding(24.dp)){Text(if(games.isEmpty())"Первая партия ещё впереди" else "Ничего не найдено",style=MaterialTheme.typography.titleMedium);Text("Закончи партию или импортируй PGN для просмотра и разбора материала.")}}}
   items(filtered,key={it.id}){game->Surface(onClick={selected=game},shape=MaterialTheme.shapes.large,color=MaterialTheme.colorScheme.surfaceContainerLow){
    ListItem(colors=ListItemDefaults.colors(containerColor=MaterialTheme.colorScheme.surfaceContainerLow),headlineContent={Text(game.opponent)},supportingContent={Column{
     Text((if(game.mode=="import")"Импортировано: " else "")+DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT).format(Date(game.finished)))
     if(game.mode=="import")Text("PGN · длительность неизвестна")else{val seconds=game.duration/1000;Text("${if(game.mode=="bot")"Бот" else "Локально"} · ${seconds/60} мин ${seconds%60} с")}
     Text("${game.notation.size} полуходов · открыть разбор")
    }},trailingContent={Surface(shape=CircleShape,color=MaterialTheme.colorScheme.secondaryContainer){Text(game.result,Modifier.padding(horizontal=12.dp,vertical=8.dp),style=MaterialTheme.typography.labelLarge)}})
   }}
  }
 }
}
