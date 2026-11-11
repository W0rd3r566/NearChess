package dev.nearchess
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal data class ChatEntry(val id:Long,val text:String,val mine:Boolean,val receivedAt:Long)
internal class ChatTimeline {
 private var nextId=0L
 private var totalIncoming=0L
 private var readIncoming=0L
 var entries by mutableStateOf(emptyList<ChatEntry>());private set
 var unread by mutableStateOf(0);private set
 fun add(text:String,mine:Boolean,now:Long=System.currentTimeMillis()){
  if(text.isBlank()||text.length>500)return
  entries=(entries+ChatEntry(nextId++,text,mine,now)).takeLast(100)
  if(!mine){totalIncoming++;unread=(totalIncoming-readIncoming).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()}
 }
 fun markRead(){readIncoming=totalIncoming;unread=0}
 fun clear(){entries=emptyList();totalIncoming=0;readIncoming=0;unread=0}
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable internal fun LiveChat(timeline:ChatTimeline,connected:Boolean,peer:String,onSend:(String,Boolean)->Boolean){
 var open by remember{mutableStateOf(false)}
 var draft by remember{mutableStateOf("")}
 var error by remember{mutableStateOf("")}
 val prefs=LocalContext.current.getSharedPreferences("chat",0)
 var previews by remember{mutableStateOf(prefs.getBoolean("previews",true))}
 var sounds by remember{mutableStateOf(prefs.getBoolean("sound",false))}
 val motion=rememberNearMotionEnabled()
 ChatPreview(timeline,open,peer,previews,sounds){open=true;timeline.markRead()}
 TextButton(onClick={open=true;timeline.markRead()}){Text(if(timeline.unread>0)"Чат · ${timeline.unread} новых" else "Чат")}
 if(open)ModalBottomSheet(onDismissRequest={open=false},sheetState=rememberModalBottomSheetState(skipPartiallyExpanded=true)){
  val scroll=rememberLazyListState()
  val formatter=remember{SimpleDateFormat("HH:mm",Locale.getDefault())}
  LaunchedEffect(timeline.entries.lastOrNull()?.id){timeline.markRead();if(timeline.entries.isNotEmpty()){if(motion)scroll.animateScrollToItem(timeline.entries.lastIndex)else scroll.scrollToItem(timeline.entries.lastIndex)}}
  Column(Modifier.fillMaxWidth().imePadding().padding(horizontal=16.dp).padding(bottom=16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
   Text("Чат · $peer",style=MaterialTheme.typography.titleLarge)
   if(!connected)Text("Связь потеряна. Отправка недоступна.",color=MaterialTheme.colorScheme.error)
   Row{Text("Плашки в партии",Modifier.weight(1f));Switch(checked=previews,onCheckedChange={previews=it;prefs.edit().putBoolean("previews",it).apply()})}
   Row{Text("Звук плашки",Modifier.weight(1f));Switch(checked=sounds,onCheckedChange={sounds=it;prefs.edit().putBoolean("sound",it).apply()})}
   LazyColumn(Modifier.fillMaxWidth().heightIn(min=60.dp,max=240.dp),state=scroll,verticalArrangement=Arrangement.spacedBy(8.dp)){
    if(timeline.entries.isEmpty())item{Text("Поздоровайся с соперником")}
    items(timeline.entries,key={it.id}){entry->Row(Modifier.fillMaxWidth(),horizontalArrangement=if(entry.mine)Arrangement.End else Arrangement.Start){Surface(shape=MaterialTheme.shapes.medium,color=if(entry.mine)MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,modifier=Modifier.widthIn(max=280.dp)){Column(Modifier.padding(12.dp)){Text(entry.text);Text(formatter.format(Date(entry.receivedAt)),style=MaterialTheme.typography.labelSmall)}}}}
   }
   Row{listOf("Удачной игры!","Хороший ход!","Спасибо!").forEach{text->TextButton(enabled=connected,onClick={if(!onSend(text,false))error="Подожди секунду" else error=""},modifier=Modifier.weight(1f)){Text(text,style=MaterialTheme.typography.labelSmall)}}}
   Row{listOf("🤝","🤔","👏","😂").forEach{emoji->TextButton(enabled=connected,onClick={onSend(emoji,true)}){Text(emoji)}}}
   OutlinedTextField(value=draft,onValueChange={draft=it.take(500)},label={Text("Сообщение")},supportingText={Text("${draft.length}/500")},modifier=Modifier.fillMaxWidth(),maxLines=3)
   if(error.isNotEmpty())Text(error,color=MaterialTheme.colorScheme.error)
   Button(enabled=connected&&draft.isNotBlank(),onClick={if(onSend(draft,false)){draft="";error=""}else error="Проверь соединение или подожди секунду"},modifier=Modifier.fillMaxWidth()){Text("Отправить")}
  }
 }
}
