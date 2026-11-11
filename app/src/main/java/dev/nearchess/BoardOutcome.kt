package dev.nearchess

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

/** Pure presentation. Never mutates a game or sends a network message. */
@Composable internal fun BoardOutcome(
 position: Position, flipped: Boolean, enabled: Boolean,
 viewerWhite: Boolean?, modifier: Modifier = Modifier
) {
 val legal=remember(position){position.legal()}
 val mate=legal.isEmpty() && position.check()
 val draw=(legal.isEmpty() && !mate) || position.insufficient()
 val end=mate || draw
 // Scope lives for the lifetime of the board. Returning through history does not replay an outcome.
 var seen by rememberSaveable {mutableStateOf(listOf<String>())}
 var visible by rememberSaveable {mutableStateOf(false)}
 var banner by remember {mutableStateOf("")}
 val progress=remember{Animatable(1f)}
 val key=position.fen()
 val winnerWhite=if(mate) !position.white else null
 val victory=mate && (viewerWhite==null || viewerWhite==winnerWhite)
 val points=remember{val rng=Random(719);List(52){Triple(rng.nextFloat(),rng.nextFloat(),rng.nextFloat())}}
 LaunchedEffect(key,end,enabled) {
  visible=false
  if(!end || key in seen) return@LaunchedEffect
  seen=(seen+key).takeLast(64)
  banner=when {draw->"Ничья";viewerWhite==null->if(winnerWhite==true)"Белые победили" else "Чёрные победили";victory->"Победа";else->"Поражение"}
  if(enabled) delay(190)
  visible=true
  if(enabled) {progress.snapTo(0f);progress.animateTo(1f,tween(2400))} else progress.snapTo(1f)
 }
 Box(modifier) {
  Canvas(Modifier.matchParentSize()) {
   val cell=size.width/8f
   if(mate) {
    val square=position.board.indexOf(if(position.white)'K' else 'k')
    if(square>=0) {
     val col=if(flipped)7-square%8 else square%8
     val row=if(flipped)square/8 else 7-square/8
     val center=Offset((col+.5f)*cell,(row+.5f)*cell)
     val pulse=if(enabled && progress.value<1) sin(progress.value*6.28f)*.07f else 0f
     drawCircle(Color(0xFFFFB4AB).copy(alpha=.28f),cell*(.46f+pulse),center)
     drawCircle(Color(0xFFD94D58),cell*.43f,center,style=Stroke(cell*.055f))
     val badge=center+Offset(cell*.32f,-cell*.32f)
     drawCircle(Color(0xFFD94D58),cell*.14f,badge)
     drawLine(Color.White,badge-Offset(cell*.055f,cell*.055f),badge+Offset(cell*.055f,cell*.055f),cell*.035f)
     drawLine(Color.White,badge+Offset(cell*.055f,-cell*.055f),badge+Offset(-cell*.055f,cell*.055f),cell*.035f)
    }
   }
   if(enabled && end && progress.value<1f) {
    val t=progress.value
    if(victory) points.forEachIndexed {index,(x,speed,phase)->
     val life=(t*(.85f+speed*.65f)-phase*.18f).coerceAtLeast(0f)
     val px=x*size.width+sin(life*8f+index)*cell*.3f
     val py=-cell+life*(size.height+cell*2)
     if(py in -cell..size.height) {
      val colors=listOf(Color(0xFF71D8BC),Color(0xFFE9C96B),Color(0xFFEDF3EF))
      drawCircle(colors[index%3].copy(alpha=(1f-t).coerceIn(0f,1f)),cell*(.035f+phase*.025f),Offset(px,py))
     }
    } else if(draw) {
     drawCircle(Color(0xFFB5CCC3).copy(alpha=(1-t)*.5f),size.minDimension*(.1f+t*.55f),center,style=Stroke(3.dp.toPx()))
    }
   }
  }
  AnimatedVisibility(visible=visible && end,enter=fadeIn(tween(250))+slideInVertically{it/3},exit=fadeOut(),modifier=Modifier.align(Alignment.BottomCenter).padding(12.dp)) {
   Surface(color=MaterialTheme.colorScheme.surface.copy(alpha=.96f),shape=MaterialTheme.shapes.large,tonalElevation=3.dp) {
    Row(Modifier.padding(start=16.dp,end=8.dp,top=4.dp,bottom=4.dp),verticalAlignment=Alignment.CenterVertically) {
     Text(banner,style=MaterialTheme.typography.titleMedium)
     TextButton(onClick={visible=false}){Text("К доске")}
    }
   }
  }
 }
}
