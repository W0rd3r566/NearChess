package dev.nearchess
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun MatchScreen(title:String,history:List<Position>,notation:List<String>,lastMove:Move?,enabled:Boolean,whiteName:String,blackName:String,whiteClock:String="Без часов",blackClock:String="Без часов",status:String="",onBack:()->Unit,onMove:(Move)->Unit,menuContent:@Composable ColumnScope.()->Unit){
 require(history.isNotEmpty())
 var flipped by remember{mutableStateOf(false)}
 var browsing by remember{mutableStateOf<Int?>(null)}
 var menu by remember{mutableStateOf(false)}
 val live=history.last();val index=browsing?.coerceIn(history.indices)?:history.lastIndex;val shown=history[index]
 val context=androidx.compose.ui.platform.LocalContext.current
 val prefs=remember{context.getSharedPreferences("game",0)}
 var theme by remember{mutableStateOf(prefs.getInt("theme",0).coerceIn(boardPalettes.indices))}
 val finalStatus=if(live.legal().isEmpty())if(live.check())"Мат" else "Ничья" else if(live.insufficient())"Ничья" else ""
 GameFeedback(history,finalStatus)
 val takenWhite=remember(history,index){capturesBy(history.take(index+1),true)}
 val takenBlack=remember(history,index){capturesBy(history.take(index+1),false)}
 val balance=remember(shown){materialFor(shown,true)-materialFor(shown,false)}
 val localMatch=context is MainActivity
 val bot=localMatch && prefs.getBoolean("bot",false)
 val playerWhite=prefs.getBoolean("playerWhite",true)
 val photo=java.io.File(context.filesDir,"profile-avatar.jpg").takeIf{it.exists()}?.let{android.net.Uri.fromFile(it).toString()}
 val ended=shown.legal().isEmpty() || shown.insufficient() || finalStatus.isNotEmpty() || status.contains("Сдача") || status.startsWith("Ничья")
 val white=PlayerUiModel(whiteName,if(localMatch && (!bot||playerWhite))photo else null,!ended && shown.white,takenWhite,balance.coerceAtLeast(0),true,bot && !playerWhite)
 val black=PlayerUiModel(blackName,if(localMatch && bot && !playerWhite)photo else null,!ended && !shown.white,takenBlack,(-balance).coerceAtLeast(0),false,bot && playerWhite)
 BackHandler{onBack()}
 Surface(Modifier.fillMaxSize(),color=MaterialTheme.colorScheme.background){
  Column(Modifier.fillMaxSize().systemBarsPadding()){
   Row(Modifier.fillMaxWidth().height(52.dp),verticalAlignment=Alignment.CenterVertically){
    IconButton(onClick=onBack){Icon(Icons.AutoMirrored.Filled.ArrowBack,"Главная")}
    Column(Modifier.weight(1f)){Text(title,style=MaterialTheme.typography.titleMedium);Text(if(browsing!=null)"Просмотр · $index" else status.ifEmpty{if(live.white)"Ход белых" else "Ход чёрных"},style=MaterialTheme.typography.labelSmall,maxLines=1,overflow=TextOverflow.Ellipsis)}
    IconButton(onClick={menu=true}){Icon(Icons.Default.MoreVert,"Меню партии")}
   }
   BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()){
    val side=minOf(maxWidth,maxHeight-152.dp).coerceAtLeast(96.dp)
    Column(Modifier.width(side).align(Alignment.Center)){
     val top=if(flipped)white else black;val topClock=if(flipped)whiteClock else blackClock
     PlayerPanel(top,clockContent=if(topClock!="Без часов" && topClock.isNotBlank())({PlayerClock(topClock,top.isTurn)})else null)
     ChessBoard(shown,flipped,enabled && browsing==null,boardPalettes[theme],if(browsing==null)lastMove else null,onMove)
     val bottom=if(flipped)black else white;val bottomClock=if(flipped)blackClock else whiteClock
     PlayerPanel(bottom,clockContent=if(bottomClock!="Без часов" && bottomClock.isNotBlank())({PlayerClock(bottomClock,bottom.isTurn)})else null)
    }
   }
   LazyRow(Modifier.fillMaxWidth().height(44.dp).background(MaterialTheme.colorScheme.surfaceContainerLow),contentPadding=PaddingValues(horizontal=12.dp),verticalAlignment=Alignment.CenterVertically){itemsIndexed(notation){i,s->Text((if(history.getOrNull(i)?.white==true)"${history[i].full}. " else "")+s,color=if(index==i+1)MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,style=MaterialTheme.typography.labelLarge,modifier=Modifier.clickable{browsing=i+1}.padding(8.dp))}}
   Row(Modifier.fillMaxWidth().height(56.dp),horizontalArrangement=Arrangement.SpaceEvenly){
    IconButton(onClick={flipped=!flipped}){Icon(Icons.Default.SwapVert,"Повернуть")}
    IconButton(enabled=index>0,onClick={browsing=index-1}){Icon(Icons.Default.ChevronLeft,"Назад по истории")}
    IconButton(enabled=index<history.lastIndex,onClick={browsing=if(index+1==history.lastIndex)null else index+1}){Icon(Icons.Default.ChevronRight,"Вперёд по истории")}
    if(browsing!=null)FilledTonalButton(onClick={browsing=null}){Text("К игре")}else IconButton(onClick={browsing=null}){Icon(Icons.Default.History,"Текущая позиция")}
   }
  }
 }
 if(menu)ModalBottomSheet(onDismissRequest={menu=false},sheetState=rememberModalBottomSheetState(skipPartiallyExpanded=true)){
  Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal=20.dp).padding(bottom=24.dp)){
   Text("Партия и оформление",style=MaterialTheme.typography.titleLarge)
   Text("Цвет доски",style=MaterialTheme.typography.labelMedium)
   boardPalettes.forEachIndexed{i,p->TextButton(onClick={theme=i;prefs.edit().putInt("theme",i).apply()}){Text((if(theme==i)"✓ " else "")+p.name)}}
   HorizontalDivider();FeedbackControls();HorizontalDivider();menuContent()
  }
 }
}
