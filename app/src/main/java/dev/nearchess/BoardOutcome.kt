package dev.nearchess

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.math.PI
import kotlin.random.Random

private data class Ribbon(val x:Float,val speed:Float,val delay:Float,val sway:Float,val rotation:Float,val size:Float)

/** Presentation only. No transport, game mutations, clocks or touch interception. */
@Composable internal fun BoardOutcome(position:Position,flipped:Boolean,enabled:Boolean,viewerWhite:Boolean?,modifier:Modifier=Modifier){
 val legal=remember(position){position.legal()}
 val checked=remember(position){position.check()}
 val mate=legal.isEmpty() && checked
 val draw=(legal.isEmpty() && !mate)||position.insufficient()
 val end=mate||draw
 val key=position.fen()
 val winnerWhite=if(mate)!position.white else null
 val victory=mate && (viewerWhite==null || viewerWhite==winnerWhite)
 var seen by rememberSaveable{mutableStateOf(listOf<String>())}
 var checksSeen by rememberSaveable{mutableStateOf(listOf<String>())}
 var dismissed by rememberSaveable{mutableStateOf(listOf<String>())}
 var showKey by rememberSaveable{mutableStateOf("")}
 val celebration=remember{Animatable(1f)}
 val checkPulse=remember{Animatable(1f)}
 val seal=remember{Animatable(1f)}
 val surface=MaterialTheme.colorScheme.surface
 val foreground=MaterialTheme.colorScheme.onSurface
 val accent=MaterialTheme.colorScheme.primary
 val gold=Color(0xFFE9C96B)
 val coral=Color(0xFFE8797D)
 val particles=remember{val rng=Random(1927);List(64){Ribbon(rng.nextFloat(),.85f+rng.nextFloat()*.65f,rng.nextFloat()*.15f,rng.nextFloat(),rng.nextFloat()*360f,.6f+rng.nextFloat()*.7f)}}
 LaunchedEffect(key,checked,end,enabled){
  if(!checked || end || !enabled || key in checksSeen){checkPulse.snapTo(1f);return@LaunchedEffect}
  checksSeen=(checksSeen+key).takeLast(128)
  delay(175)
  checkPulse.snapTo(0f)
  checkPulse.animateTo(1f,tween(850,easing=FastOutSlowInEasing))
 }
 LaunchedEffect(key,end,enabled){
  if(!end){showKey="";return@LaunchedEffect}
  if(key in seen)return@LaunchedEffect
  seen=(seen+key).takeLast(128)
  showKey=""
  if(enabled){celebration.snapTo(0f);seal.snapTo(0f);delay(185)}else{celebration.snapTo(1f);seal.snapTo(1f)}
  showKey=key
  if(enabled){seal.animateTo(1f,tween(340,easing=FastOutSlowInEasing));celebration.animateTo(1f,tween(2600,easing=LinearEasing))}
 }
 val king=position.board.indexOf(if(position.white)'K' else 'k')
 val title=when{draw->"Ничья";viewerWhite==null->if(winnerWhite==true)"Белые победили" else "Чёрные победили";victory->"Победа";else->"Поражение"}
 Box(modifier){
  Canvas(Modifier.matchParentSize()){
   val cell=size.width/8f
   fun centerOf(sq:Int):Offset{val col=if(flipped)7-sq%8 else sq%8;val row=if(flipped)sq/8 else 7-sq/8;return Offset((col+.5f)*cell,(row+.5f)*cell)}
   if(checked && king>=0){
    val c=centerOf(king)
    if(!mate && enabled && checkPulse.value<1f){
     val t=checkPulse.value
     drawCircle(Brush.radialGradient(listOf(coral.copy(alpha=.28f*(1-t)),Color.Transparent),c,cell*.95f),cell*.95f,c)
     drawCircle(coral.copy(alpha=(1-t)*.8f),cell*(.32f+t*.55f),c,style=Stroke(cell*.025f))
    }
    if(mate){
     val a=if(enabled)seal.value else 1f
     val r=cell*(.34f+.1f*a)
     drawCircle(Brush.radialGradient(listOf(coral.copy(alpha=.14f),Color.Transparent),c,cell*.72f),cell*.72f,c)
     drawArc(coral,startAngle=-90f,sweepAngle=360f*a,useCenter=false,topLeft=c-Offset(r,r),size=Size(r*2,r*2),style=Stroke(cell*.045f))
     val badge=c+Offset(cell*.32f,-cell*.32f)
     drawCircle(surface,cell*.18f,badge)
     drawCircle(coral.copy(alpha=a),cell*.145f,badge)
     val d=cell*.05f*a
     drawLine(Color.White,badge-Offset(d,d),badge+Offset(d,d),cell*.025f)
     drawLine(Color.White,badge+Offset(d,-d),badge+Offset(-d,d),cell*.025f)
    }
   }
   if(enabled && end && showKey==key && celebration.value<1f){
    val t=celebration.value
    if(victory){
     val palette=listOf(accent,gold,foreground)
     particles.forEachIndexed{i,p->
      val travel=(t-p.delay)*p.speed
      if(travel>=0f){
       val x=p.x*size.width+sin(travel*8+p.sway*6)*cell*.45f
       val y=-cell+travel*(size.height+cell*3)
       val alpha=((1-t)*3).coerceIn(0f,.85f)
       if(y< size.height+cell){
        val w=cell*.075f*p.size
        val h=cell*.14f*p.size
        val flip=.25f+.75f*kotlin.math.abs(sin(travel*12+p.rotation))
        rotate(p.rotation+travel*(if(i%2==0)440 else -360),Offset(x,y)){
         drawRoundRect(palette[i%3].copy(alpha=alpha),Offset(x-w/2,y-h/2),Size(w*flip,h),cornerRadius=androidx.compose.ui.geometry.CornerRadius(w*.2f))
        }
       }
      }
     }
    }else if(draw){
     for(i in 0..1){val phase=(t-i*.12f).coerceIn(0f,1f);drawCircle(accent.copy(alpha=(1-phase)*.28f),size.minDimension*(.12f+phase*.48f),center,style=Stroke(2.dp.toPx()))}
    }
   }
  }
  AnimatedVisibility(visible=end && showKey==key && key !in dismissed,enter=fadeIn(tween(if(enabled)280 else 0))+slideInVertically(tween(if(enabled)320 else 0)){it/4}+scaleIn(tween(if(enabled)320 else 0),initialScale=.96f),exit=fadeOut(tween(if(enabled)150 else 0)),modifier=Modifier.align(Alignment.BottomCenter).padding(12.dp)){
   Surface(shape=MaterialTheme.shapes.large,color=surface.copy(alpha=.97f),tonalElevation=3.dp){
    Row(Modifier.padding(start=16.dp,end=8.dp,top=10.dp,bottom=10.dp),verticalAlignment=Alignment.CenterVertically){
     Column(Modifier.weight(1f)){
      Text(title,style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.SemiBold,color=if(victory)accent else foreground)
      Text(if(mate)"Шах и мат · финальная позиция" else "Партия завершена вничью",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
     }
     TextButton(onClick={dismissed=(dismissed+key).takeLast(128)}){Text("К доске")}
    }
   }
  }
 }
}
