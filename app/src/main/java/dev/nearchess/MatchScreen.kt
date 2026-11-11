package dev.nearchess

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/** A presentation layer only. History browsing never calls onMove or modifies the live game. */
@Composable fun MatchScreen(
 title:String, history:List<Position>, notation:List<String>, lastMove:Move?,
 enabled:Boolean, whiteName:String, blackName:String,
 whiteClock:String="Без часов", blackClock:String="Без часов",
 status:String="", onBack:()->Unit, onMove:(Move)->Unit,
 menuContent:@Composable ColumnScope.()->Unit
) {
 require(history.isNotEmpty())
 var flipped by remember{mutableStateOf(false)}
 var browsing by remember{mutableStateOf<Int?>(null)}
 var menu by remember{mutableStateOf(false)}
 val live=history.last()
 val index=browsing?.coerceIn(history.indices)?:history.lastIndex
 val shown=history[index]
 val local=androidx.compose.ui.platform.LocalContext.current
 val prefs=remember{local.getSharedPreferences("game",0)}
 var theme by remember{mutableStateOf(prefs.getInt("theme",0).coerceIn(boardPalettes.indices))}
 BackHandler{onBack()}
 Surface(color=MaterialTheme.colorScheme.background,modifier=Modifier.fillMaxSize()) {
  Column(Modifier.fillMaxSize().systemBarsPadding()) {
   Row(Modifier.fillMaxWidth().height(52.dp).padding(horizontal=8.dp),verticalAlignment=Alignment.CenterVertically) {
    IconButton(onClick=onBack){Icon(Icons.AutoMirrored.Filled.ArrowBack,"Главная")}
    Column(Modifier.weight(1f)){Text(title,style=MaterialTheme.typography.titleMedium);Text(if(browsing!=null) "Просмотр · позиция $index" else status.ifEmpty{if(live.white)"Ход белых" else "Ход чёрных"},style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant,maxLines=1,overflow=TextOverflow.Ellipsis)}
    IconButton(onClick={menu=true}){Icon(Icons.Default.MoreVert,"Меню партии")}
   }
   BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
    // ChessBoard currently includes a 48dp appearance action below its square.
    val boardSide=minOf(maxWidth,maxHeight-164.dp).coerceAtLeast(96.dp)
    Column(Modifier.width(boardSide).align(Alignment.Center),horizontalAlignment=Alignment.CenterHorizontally) {
     PlayerPanel(if(flipped)whiteName else blackName,if(flipped)whiteClock else blackClock,if(flipped)shown.white else !shown.white,if(flipped)"БЕЛЫЕ" else "ЧЁРНЫЕ")
     ChessBoard(shown,flipped,enabled && browsing==null,boardPalettes[theme],if(browsing==null)lastMove else null,onMove)
     PlayerPanel(if(flipped)blackName else whiteName,if(flipped)blackClock else whiteClock,if(flipped)!shown.white else shown.white,if(flipped)"ЧЁРНЫЕ" else "БЕЛЫЕ")
    }
   }
   LazyRow(Modifier.fillMaxWidth().height(44.dp).background(MaterialTheme.colorScheme.surfaceContainerLow),contentPadding=PaddingValues(horizontal=12.dp),verticalAlignment=Alignment.CenterVertically) {
    itemsIndexed(notation){i,san ->
     Text((if(history.getOrNull(i)?.white==true) "${history[i].full}. " else "")+san,style=MaterialTheme.typography.labelLarge,color=if(index==i+1)MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,modifier=Modifier.padding(horizontal=4.dp).clickable{browsing=i+1}.padding(8.dp))
    }
   }
   Row(Modifier.fillMaxWidth().height(56.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceEvenly) {
    IconButton(onClick={flipped=!flipped}){Icon(Icons.Default.SwapVert,"Перевернуть доску")}
    IconButton(enabled=index>0,onClick={browsing=index-1}){Icon(Icons.Default.ChevronLeft,"Предыдущая позиция")}
    IconButton(enabled=index<history.lastIndex,onClick={browsing=if(index+1==history.lastIndex)null else index+1}){Icon(Icons.Default.ChevronRight,"Следующая позиция")}
    IconButton(onClick={browsing=null}){Icon(Icons.Default.History,"К текущей позиции")}
   }
  }
 }
 if(menu) AlertDialog(onDismissRequest={menu=false},title={Text("Партия и оформление")},text={Column{
  Text("Цвет доски",style=MaterialTheme.typography.labelMedium)
  boardPalettes.forEachIndexed{i,p->TextButton(onClick={theme=i;prefs.edit().putInt("theme",i).apply()}){Text((if(theme==i)"✓ " else "")+p.name)}}
  menuContent()
 }},confirmButton={TextButton(onClick={menu=false}){Text("К доске")}})
}
@Composable private fun PlayerPanel(name:String,time:String,active:Boolean,side:String) {
 val colors=MaterialTheme.colorScheme
 Row(Modifier.fillMaxWidth().height(58.dp).padding(horizontal=12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
  Surface(shape=RoundedCornerShape(12.dp),color=colors.surfaceContainerHigh,modifier=Modifier.size(36.dp)) {
   Box(contentAlignment=Alignment.Center){Text(name.firstOrNull()?.uppercase()?:"?",style=MaterialTheme.typography.titleMedium)}
  }
  Column(Modifier.weight(1f)){Text(name,maxLines=1,overflow=TextOverflow.Ellipsis,style=MaterialTheme.typography.titleSmall);Text(side,style=MaterialTheme.typography.labelSmall,color=if(active)colors.primary else colors.onSurfaceVariant)}
  Surface(shape=RoundedCornerShape(10.dp),color=if(active)colors.primaryContainer else colors.surfaceContainerHigh) {
   Text(time,Modifier.padding(horizontal=12.dp,vertical=8.dp),fontFamily=FontFamily.Monospace,fontWeight=FontWeight.SemiBold,style=MaterialTheme.typography.titleMedium)
  }
 }
}
