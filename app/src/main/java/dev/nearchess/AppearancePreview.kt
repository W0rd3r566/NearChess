package dev.nearchess

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Non-interactive real SVG preview; selection does not start a game or play audio. */
@Composable internal fun AppearancePreview(set:String,palette:BoardPalette){
 val context=LocalContext.current
 var error by remember{mutableStateOf(false)}
 val graphics by produceState<PieceGraphics?>(null,set){
  value=null;error=false
  val loaded=withContext(Dispatchers.IO){runCatching{PieceGraphics(context,set)}}
  loaded.onSuccess{value=it}.onFailure{error=true}
 }
 Column {
  Canvas(Modifier.fillMaxWidth().height(132.dp)){
   val cell=minOf(size.width/6f,size.height/2f)
   val left=(size.width-cell*6)/2
   val top=(size.height-cell*2)/2
   val pieces="kqrbnpKQRBNP"
   for(row in 0..1)for(col in 0..5){
    val x=left+col*cell;val y=top+row*cell
    drawRect(if((col+row)%2==0)Color(palette.light)else Color(palette.dark),Offset(x,y),Size(cell,cell))
    graphics?.draw(drawContext.canvas.nativeCanvas,pieces[row*6+col],x+cell/2,y+cell/2,cell*.9f)
   }
  }
  if(graphics==null && !error)LinearProgressIndicator(Modifier.fillMaxWidth())
  if(error)Text("Не удалось загрузить SVG-превью",color=MaterialTheme.colorScheme.error,style=MaterialTheme.typography.bodySmall)
 }
}
