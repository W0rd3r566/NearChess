package dev.nearchess

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlin.math.abs

internal fun reviewMaterial(p:Position):Int=p.board.sumOf{c->
 val v=when(c.uppercaseChar()){'P'->1;'N','B'->3;'R'->5;'Q'->9;else->0}
 if(c.isUpperCase())v else -v
}
@Composable internal fun MaterialReview(game:ArchivedGame,onBack:()->Unit){
 val history=game.positions
 if(history.isEmpty()){TextButton(onClick=onBack){Text("Нет сохранённых позиций · назад")};return}
 BackHandler(onBack=onBack)
 var index by remember(game.id){mutableStateOf(0)}
 var flipped by remember{mutableStateOf(false)}
 val values=remember(game.id){history.map{reviewMaterial(it)}}
 val motion=rememberNearMotionEnabled()
 val value by animateFloatAsState(values[index].toFloat(),tween(if(motion)180 else 0),label="material balance")
 val p=history[index]
 val legal=remember(p){p.legal()}
 val event=when{legal.isEmpty() && p.check()->"Мат";legal.isEmpty()->"Пат";p.check()->"Шах";index>0 && p.board.count{it!='.'}<history[index-1].board.count{it!='.'}->"Взятие";else->"Позиция"}
 val context=LocalContext.current
 val palette=context.getSharedPreferences("game",0).getInt("theme",0).coerceIn(boardPalettes.indices)
 val accent=MaterialTheme.colorScheme.primary
 val outline=MaterialTheme.colorScheme.outlineVariant
 Surface(Modifier.fillMaxSize()){
  Column(Modifier.fillMaxSize().systemBarsPadding().padding(horizontal=12.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){
   Row(verticalAlignment=Alignment.CenterVertically){TextButton(onClick=onBack){Text("Назад")};Column(Modifier.weight(1f)){Text("Разбор материала",style=MaterialTheme.typography.titleMedium);Text("${game.opponent} · ${game.result}",style=MaterialTheme.typography.labelSmall)}}
   Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
    Surface(shape=CircleShape,color=MaterialTheme.colorScheme.primaryContainer){Text("Баланс ${if(value>0)"+" else ""}${"%.1f".format(value)}",Modifier.padding(horizontal=14.dp,vertical=8.dp),style=MaterialTheme.typography.labelLarge)}
    Surface(shape=CircleShape,color=MaterialTheme.colorScheme.surfaceContainerHigh){Text(event,Modifier.padding(horizontal=14.dp,vertical=8.dp),style=MaterialTheme.typography.labelLarge)}
   }
   BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()){
    val side=minOf(maxWidth,maxHeight-48.dp).coerceAtLeast(80.dp)
    Box(Modifier.width(side).align(Alignment.Center)){ChessBoard(p,flipped,false,boardPalettes[palette],null){}}
   }
   Text("Материальный баланс, НЕ оценка движка. Плюс — в пользу белых.",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
   Canvas(Modifier.fillMaxWidth().height(76.dp).pointerInput(values){detectTapGestures{o->index=((o.x/size.width)*(history.lastIndex)).toInt().coerceIn(history.indices)}}){
    val range=maxOf(3,values.maxOf{abs(it)})+1f
    val mid=size.height/2
    drawLine(outline,Offset(0f,mid),Offset(size.width,mid),1.dp.toPx())
    val path=Path()
    values.forEachIndexed{i,v->val x=if(values.size==1)0f else size.width*i/(values.size-1);val y=mid-v/range*mid*.85f;if(i==0)path.moveTo(x,y)else path.lineTo(x,y)}
    drawPath(path,accent,style=Stroke(2.dp.toPx()))
    val x=if(values.size==1)0f else size.width*index/(values.size-1)
    drawLine(accent.copy(alpha=.4f),Offset(x,0f),Offset(x,size.height),1.dp.toPx())
    drawCircle(accent,4.dp.toPx(),Offset(x,mid-values[index]/range*mid*.85f))
   }
   LazyRow(horizontalArrangement=Arrangement.spacedBy(6.dp)){
    item{FilterChip(selected=index==0,onClick={index=0},label={Text("Старт")})}
    itemsIndexed(game.notation){i,s->FilterChip(selected=index==i+1,onClick={index=(i+1).coerceAtMost(history.lastIndex)},label={Text("${i+1}. $s")})}
   }
   Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly,verticalAlignment=Alignment.CenterVertically){
    FilledTonalButton(onClick={flipped=!flipped},shape=CircleShape){Text("Повернуть")}
    FilledTonalButton(enabled=index>0,onClick={index--},shape=CircleShape){Text("Назад")}
    FilledTonalButton(enabled=index<history.lastIndex,onClick={index++},shape=CircleShape){Text("Далее")}
   }
   Text("Неточности, точность и лучшие альтернативы требуют движкового анализа. Здесь показаны только факты сохранённой партии.",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
   Spacer(Modifier.height(4.dp))
  }
 }
}
