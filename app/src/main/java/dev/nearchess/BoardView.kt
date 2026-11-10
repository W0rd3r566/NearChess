package dev.nearchess

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp

data class BoardPalette(val name: String, val light: Long, val dark: Long)
val boardPalettes=listOf(
 BoardPalette("Мята",0xFFE7E8D5,0xFF56877C),
 BoardPalette("Орех",0xFFF0D9B5,0xFFB58863),
 BoardPalette("Океан",0xFFE4EDF2,0xFF668FA8),
 BoardPalette("Сланец",0xFFE0E1E3,0xFF777F89),
 BoardPalette("Сирень",0xFFEDE3F2,0xFF9681AD)
)

/** Board-wide gesture ownership keeps dragging from scrolling the parent. */
@Composable fun ChessBoard(
 position: Position, flipped: Boolean, enabled: Boolean,
 palette: BoardPalette, lastMove: Move?, onMove: (Move)->Unit
) {
 var width by remember { mutableStateOf(0) }
 var selected by remember(position,flipped,enabled) { mutableStateOf<Int?>(null) }
 var dragging by remember(position,flipped,enabled) { mutableStateOf<Int?>(null) }
 var finger by remember { mutableStateOf(Offset.Zero) }
 var promotion by remember(position) { mutableStateOf(emptyList<Move>()) }
 val moves=remember(position){position.legal()}
 val currentMove by rememberUpdatedState(onMove)
 val paint=remember { Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign=Paint.Align.CENTER; typeface=Typeface.create("sans-serif",Typeface.NORMAL) } }
 fun squareAt(o: Offset): Int? {
  if(width<=0 || o.x<0 || o.y<0 || o.x>=width || o.y>=width) return null
  val cell=width/8f; val col=(o.x/cell).toInt(); val row=(o.y/cell).toInt()
  return if(flipped) row*8+7-col else (7-row)*8+col
 }
 fun submit(from: Int,to: Int) {
  val candidates=moves.filter{it.from==from && it.to==to}
  when(candidates.size) { 0 -> Unit; 1 -> {selected=null; currentMove(candidates.single())}; else -> promotion=candidates }
 }
 val king=position.board.indexOf(if(position.white) 'K' else 'k')
 val check=remember(position){position.check()}
 Canvas(Modifier.fillMaxWidth().aspectRatio(1f).onSizeChanged{width=it.width}
  .pointerInput(position,flipped,enabled,width) {
   detectTapGestures { o -> if(enabled) { val sq=squareAt(o); if(sq!=null) {
    val from=selected
    if(from!=null && moves.any{it.from==from && it.to==sq}) submit(from,sq)
    else selected=if(position.own(position.board[sq]) && selected!=sq) sq else null
   } } }
  }
  .pointerInput(position,flipped,enabled,width) {
   detectDragGestures(
    onDragStart={o -> if(enabled) { val sq=squareAt(o); if(sq!=null && position.own(position.board[sq])) {dragging=sq; selected=sq; finger=o} }},
    onDragCancel={dragging=null},
    onDragEnd={ val from=dragging; val to=squareAt(finger); dragging=null; if(from!=null && to!=null) submit(from,to) },
    onDrag={change,amount -> if(dragging!=null) {change.consume(); finger+=amount} }
   )
  }) {
  val cell=size.width/8f
  fun center(sq: Int): Offset {val col=if(flipped) 7-sq%8 else sq%8; val row=if(flipped) sq/8 else 7-sq/8; return Offset((col+.5f)*cell,(row+.5f)*cell)}
  fun piece(sq: Int,at: Offset,scale: Float=1f) {
   val p=position.board[sq]; if(p=='.') return
   val glyph=when(p.uppercaseChar()) {'K'->"♚";'Q'->"♛";'R'->"♜";'B'->"♝";'N'->"♞";else->"♟"}
   paint.textSize=cell*.82f*scale
   val baseline=at.y-(paint.ascent()+paint.descent())/2
   val white=p.isUpperCase()
   paint.style=Paint.Style.STROKE; paint.strokeWidth=cell*.028f
   paint.color=if(white) android.graphics.Color.rgb(36,45,47) else android.graphics.Color.rgb(202,214,208)
   drawContext.canvas.nativeCanvas.drawText(glyph,at.x,baseline,paint)
   paint.style=Paint.Style.FILL; paint.color=if(white) android.graphics.Color.rgb(249,246,231) else android.graphics.Color.rgb(27,37,41)
   drawContext.canvas.nativeCanvas.drawText(glyph,at.x,baseline,paint)
  }
  for(sq in 0..63) {
   val c=center(sq); val top=c-Offset(cell/2,cell/2)
   drawRect(if((sq%8+sq/8)%2==0) Color(palette.dark) else Color(palette.light),top,androidx.compose.ui.geometry.Size(cell,cell))
   if(sq==lastMove?.from || sq==lastMove?.to) drawRect(Color(0xFFE9C96B).copy(alpha=.4f),top,androidx.compose.ui.geometry.Size(cell,cell))
   if(sq==selected) drawRect(Color(0xFF71D8BC).copy(alpha=.65f),top,androidx.compose.ui.geometry.Size(cell,cell))
   if(check && sq==king) drawRect(Color(0xFFE45E65).copy(alpha=.7f),top,androidx.compose.ui.geometry.Size(cell,cell))
   if(moves.any{it.from==selected && it.to==sq}) {
    if(position.board[sq]=='.') drawCircle(Color(0xFF17372E).copy(alpha=.33f),cell*.12f,c)
    else drawCircle(Color(0xFF17372E).copy(alpha=.65f),cell*.44f,c,style=Stroke(cell*.055f))
   }
   if(sq!=dragging) piece(sq,c)
   val row=if(flipped) sq/8 else 7-sq/8; val col=if(flipped) 7-sq%8 else sq%8
   paint.textSize=cell*.17f; paint.style=Paint.Style.FILL; paint.color=android.graphics.Color.rgb(36,54,47)
   if(col==0) drawContext.canvas.nativeCanvas.drawText("${sq/8+1}",top.x+cell*.09f,top.y+cell*.18f,paint)
   if(row==7) drawContext.canvas.nativeCanvas.drawText("${('a'.code+sq%8).toChar()}",top.x+cell*.89f,top.y+cell*.93f,paint)
  }
  dragging?.let{drawCircle(Color.Black.copy(alpha=.16f),cell*.43f,finger+Offset(0f,cell*.06f)); piece(it,finger,1.15f)}
 }
 if(promotion.isNotEmpty()) AlertDialog(onDismissRequest={promotion=emptyList()},title={Text("Превратить пешку")},text={Row {promotion.forEach {m -> TextButton(onClick={promotion=emptyList(); selected=null; currentMove(m)}) {Text(when(m.promotion){'q'->"Ферзь";'r'->"Ладья";'b'->"Слон";else->"Конь"})}}}},confirmButton={})
}

/** Original geometric avatar artwork. No third-party image assets. */
@Composable fun PlayerAvatar(index: Int, modifier: Modifier=Modifier) {
 val colors=listOf(Color(0xFF71D8BC),Color(0xFFE9C96B),Color(0xFFADA0E8),Color(0xFF82B9ED))
 Canvas(modifier.size(48.dp)) {
  val c=colors[index.mod(colors.size)]; val r=size.minDimension/2
  drawCircle(c.copy(alpha=.18f),r)
  drawCircle(c,r*.66f,Offset(size.width/2,size.height*.49f))
  val eyeY=size.height*.43f
  for(x in listOf(.37f,.63f)) drawCircle(Color(0xFF121A1C),r*.075f,Offset(size.width*x,eyeY))
  drawLine(Color(0xFF121A1C),Offset(size.width*.39f,size.height*.62f),Offset(size.width*.61f,size.height*(if(index%2==0) .62f else .57f)),r*.07f)
 }
}
