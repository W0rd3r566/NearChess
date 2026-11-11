package dev.nearchess

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class BoardPalette(val name:String,val light:Long,val dark:Long)
val boardPalettes=listOf(BoardPalette("Мята",0xFFE7E8D5,0xFF56877C),BoardPalette("Орех",0xFFF0D9B5,0xFFB58863),BoardPalette("Океан",0xFFE4EDF2,0xFF668FA8),BoardPalette("Сланец",0xFFE0E1E3,0xFF777F89),BoardPalette("Сирень",0xFFEDE3F2,0xFF9681AD))

@Composable fun ChessBoard(position:Position,flipped:Boolean,enabled:Boolean,palette:BoardPalette,lastMove:Move?,onMove:(Move)->Unit) {
 val context=LocalContext.current
 val prefs=remember{context.getSharedPreferences("board",0)}
 var set by remember{mutableStateOf(prefs.getString("pieces","cburnett")?:"cburnett")}
 var animEnabled by remember{mutableStateOf(prefs.getBoolean("animations",true))}
 var settings by remember{mutableStateOf(false)}
 var width by remember{mutableStateOf(0)}
 var selected by remember(position,flipped,enabled){mutableStateOf<Int?>(null)}
 var dragging by remember(position,flipped,enabled){mutableStateOf<Int?>(null)}
 var finger by remember{mutableStateOf(Offset.Zero)}
 var promotion by remember(position){mutableStateOf(emptyList<Move>())}
 var graphicsError by remember{mutableStateOf("")}
 val graphics by produceState<PieceGraphics?>(null,set) {
  value=null;graphicsError=""
  val loaded=withContext(Dispatchers.IO){runCatching{PieceGraphics(context,set)}}
  loaded.onSuccess{value=it}.onFailure{graphicsError="Ошибка загрузки фигур: ${it.message}"}
 }
 val moves=remember(position){position.legal()}
 val currentMove by rememberUpdatedState(onMove)
 val paint=remember{Paint(Paint.ANTI_ALIAS_FLAG).apply{textAlign=Paint.Align.CENTER}}
 val progress=remember{Animatable(1f)}
 var previous by remember{mutableStateOf(position)}
 var animated by remember{mutableStateOf<Move?>(null)}
 var captured by remember{mutableStateOf<Pair<Int,Char>?>(null)}
 LaunchedEffect(position,lastMove,animEnabled) {
  val old=previous;previous=position
  val m=lastMove
  val valid=m!=null && m in old.legal() && old.play(m)==position
  if(valid && animEnabled && m!=null){
   val epCapture=old.board[m.from].uppercaseChar()=='P' && m.to==old.ep
   val captureSquare=if(epCapture)m.to+if(old.white)-8 else 8 else m.to
   captured=old.board[captureSquare].takeIf{it!='.'}?.let{captureSquare to it}
   animated=m;progress.snapTo(0f);progress.animateTo(1f,tween(170));animated=null;captured=null
  } else {animated=null;captured=null;progress.snapTo(1f)}
 }
 fun squareAt(o:Offset):Int? {
  if(width<=0 || o.x<0 || o.y<0 || o.x>=width || o.y>=width)return null
  val c=(o.x/(width/8f)).toInt();val r=(o.y/(width/8f)).toInt()
  return if(flipped)r*8+7-c else (7-r)*8+c
 }
 fun submit(from:Int,to:Int){val list=moves.filter{it.from==from && it.to==to};when(list.size){0->Unit;1->{selected=null;currentMove(list.single())};else->promotion=list}}
 val check=remember(position){position.check()}
 val king=position.board.indexOf(if(position.white)'K' else 'k')
 val activity=context as? android.app.Activity
 val viewerWhite=when(activity){is NearbyActivity->!flipped;is MainActivity->if(context.getSharedPreferences("game",0).getBoolean("bot",false))context.getSharedPreferences("game",0).getBoolean("playerWhite",true) else null;else->null}
 Column {
  Box(Modifier.fillMaxWidth().aspectRatio(1f)) {
   Canvas(Modifier.matchParentSize().onSizeChanged{width=it.width}
    .pointerInput(position,flipped,enabled,width){detectTapGestures{o->if(enabled && graphics!=null){squareAt(o)?.let{sq->val from=selected;if(from!=null && moves.any{it.from==from && it.to==sq})submit(from,sq)else selected=if(position.own(position.board[sq]) && selected!=sq)sq else null}}}}
    .pointerInput(position,flipped,enabled,width){detectDragGestures(onDragStart={o->if(enabled && graphics!=null){squareAt(o)?.let{sq->if(position.own(position.board[sq])){dragging=sq;selected=sq;finger=o}}}},onDragCancel={dragging=null},onDragEnd={val from=dragging;val to=squareAt(finger);dragging=null;if(from!=null && to!=null)submit(from,to)},onDrag={change,_->if(dragging!=null){change.consume();finger=change.position}})}) {
    val cell=size.width/8f
    fun center(sq:Int):Offset{val c=if(flipped)7-sq%8 else sq%8;val r=if(flipped)sq/8 else 7-sq/8;return Offset((c+.5f)*cell,(r+.5f)*cell)}
    fun piece(sq:Int,at:Offset,scale:Float=1f){graphics?.draw(drawContext.canvas.nativeCanvas,position.board[sq],at.x,at.y,cell*.92f*scale)}
    val m=animated
    val rookTo=if(m!=null && position.board[m.to].uppercaseChar()=='K' && kotlin.math.abs(m.to-m.from)==2)if(m.to>m.from)m.from+1 else m.from-1 else -1
    for(sq in 0..63){
     val c=center(sq);val top=c-Offset(cell/2,cell/2);val sizeCell=androidx.compose.ui.geometry.Size(cell,cell)
     drawRect(if((sq%8+sq/8)%2==0)Color(palette.dark)else Color(palette.light),top,sizeCell)
     if(sq==lastMove?.from || sq==lastMove?.to)drawRect(Color(0xFFE9C96B).copy(alpha=.4f),top,sizeCell)
     if(sq==selected)drawRect(Color(0xFF71D8BC).copy(alpha=.65f),top,sizeCell)
     if(check && sq==king)drawRect(Color(0xFFE45E65).copy(alpha=.7f),top,sizeCell)
     if(moves.any{it.from==selected && it.to==sq}){if(position.board[sq]=='.')drawCircle(Color(0xFF17372E).copy(alpha=.33f),cell*.12f,c)else drawCircle(Color(0xFF17372E).copy(alpha=.65f),cell*.44f,c,style=Stroke(cell*.055f))}
    }
    captured?.let{(sq,p)->val c=center(sq);val canvas=drawContext.canvas.nativeCanvas;val saved=canvas.saveLayerAlpha(c.x-cell/2,c.y-cell/2,c.x+cell/2,c.y+cell/2,((1-progress.value)*255).toInt());graphics?.draw(canvas,p,c.x,c.y,cell*.92f*(1-progress.value*.15f));canvas.restoreToCount(saved)}
    for(sq in 0..63)if(sq!=dragging && sq!=m?.to && sq!=rookTo)piece(sq,center(sq))
    if(m!=null){val from=center(m.from);val to=center(m.to);piece(m.to,from+(to-from)*progress.value);if(rookTo>=0){val rookFrom=if(m.to>m.from)m.from+3 else m.from-4;val a=center(rookFrom);val b=center(rookTo);piece(rookTo,a+(b-a)*progress.value)}}
    for(sq in 0..63){val c=center(sq);val top=c-Offset(cell/2,cell/2);val row=if(flipped)sq/8 else 7-sq/8;val col=if(flipped)7-sq%8 else sq%8;paint.textSize=cell*.16f;paint.color=android.graphics.Color.rgb(36,54,47);if(col==0)drawContext.canvas.nativeCanvas.drawText("${sq/8+1}",top.x+cell*.09f,top.y+cell*.18f,paint);if(row==7)drawContext.canvas.nativeCanvas.drawText("${('A'.code+sq%8).toChar()}",top.x+cell*.89f,top.y+cell*.93f,paint)}
    dragging?.let{drawCircle(Color.Black.copy(alpha=.16f),cell*.43f,finger+Offset(0f,cell*.06f));piece(it,finger,1.12f)}
   }
   BoardOutcome(position,flipped,animEnabled,viewerWhite,Modifier.matchParentSize())
  }
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.End){TextButton(onClick={settings=true}){Text("Фигуры · ${set.replaceFirstChar{it.uppercase()}}")}}
  if(graphics==null && graphicsError.isEmpty())LinearProgressIndicator(Modifier.fillMaxWidth())
  if(graphicsError.isNotEmpty())Text(graphicsError,color=MaterialTheme.colorScheme.error)
 }
 if(settings)AlertDialog(onDismissRequest={settings=false},title={Text("Фигуры и движение")},text={Column{
  listOf("cburnett","chessnut","spatial").forEach{s->TextButton(onClick={set=s;prefs.edit().putString("pieces",s).apply()}){Text((if(set==s)"✓ " else "")+s.replaceFirstChar{it.uppercase()})}}
  Row{Text("Анимации и эффекты",Modifier.weight(1f));Switch(checked=animEnabled,onCheckedChange={animEnabled=it;prefs.edit().putBoolean("animations",it).apply()})}
 }},confirmButton={TextButton(onClick={settings=false}){Text("Готово")}})
 if(promotion.isNotEmpty())PromotionChoice(promotion,position.white,set,onDismiss={promotion=emptyList()},onChoose={m->if(enabled && m in moves){promotion=emptyList();selected=null;currentMove(m)}})
}
@Composable fun PlayerAvatar(index:Int,modifier:Modifier=Modifier){
 val colors=listOf(Color(0xFF71D8BC),Color(0xFFE9C96B),Color(0xFFADA0E8),Color(0xFF82B9ED))
 Canvas(modifier.size(48.dp)){val c=colors[index.mod(colors.size)];val r=size.minDimension/2;drawCircle(c.copy(alpha=.18f),r);drawCircle(c,r*.66f,Offset(size.width/2,size.height*.49f));for(x in listOf(.37f,.63f))drawCircle(Color(0xFF121A1C),r*.075f,Offset(size.width*x,size.height*.43f));drawLine(Color(0xFF121A1C),Offset(size.width*.39f,size.height*.62f),Offset(size.width*.61f,size.height*(if(index%2==0).62f else .57f)),r*.07f)}
}
