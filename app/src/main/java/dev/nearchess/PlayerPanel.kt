package dev.nearchess

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class PieceType(val symbol:Char,val points:Int){PAWN('P',1),KNIGHT('N',3),BISHOP('B',3),ROOK('R',5),QUEEN('Q',9)}
data class PlayerUiModel(val name:String,val avatarUri:String?=null,val isTurn:Boolean=false,val capturedPieces:List<PieceType> = emptyList(),val materialAdvantage:Int=0,val white:Boolean=true,val isBot:Boolean=false)
internal fun materialFor(p:Position,white:Boolean):Int=p.board.filter{it!='.' && it.isUpperCase()==white}.sumOf{c->PieceType.entries.firstOrNull{it.symbol==c.uppercaseChar()}?.points?:0}
internal fun capturesBy(history:List<Position>,white:Boolean):List<PieceType>{
 val out=mutableListOf<PieceType>()
 history.zipWithNext().forEach{(before,after)->
  if(before.white==white){
   // A captured enemy disappears from its square. Castling and promotion of the
   // moving side do not remove an enemy; en passant removes it on another square.
   before.board.forEachIndexed{sq,c->if(c!='.' && c.isUpperCase()!=white && after.board[sq]!=c){PieceType.entries.firstOrNull{it.symbol==c.uppercaseChar()}?.let{out.add(it)}}}
  }
 }
 return out.sortedBy{it.ordinal}
}
@Composable fun PlayerPanel(player:PlayerUiModel,modifier:Modifier=Modifier,clockContent:(@Composable ()->Unit)?=null){
 val colors=MaterialTheme.colorScheme
 val context=LocalContext.current
 val bitmap by produceState<android.graphics.Bitmap?>(null,player.avatarUri){
  value=null
  value=withContext(Dispatchers.IO){runCatching{
   player.avatarUri?.let{source->
    val options=BitmapFactory.Options().apply{inJustDecodeBounds=true}
    context.contentResolver.openInputStream(Uri.parse(source))?.use{BitmapFactory.decodeStream(it,null,options)}
    var sample=1;while(maxOf(options.outWidth,options.outHeight)/sample>256)sample*=2
    context.contentResolver.openInputStream(Uri.parse(source))?.use{BitmapFactory.decodeStream(it,null,BitmapFactory.Options().apply{inSampleSize=sample})}
   }
  }.getOrNull()}
 }
 Surface(modifier.fillMaxWidth().height(52.dp).padding(horizontal=16.dp,vertical=2.dp),shape=RoundedCornerShape(16.dp),color=if(player.isTurn)colors.surfaceContainerHigh else colors.surfaceContainer,border=if(player.isTurn)BorderStroke(1.dp,colors.primary.copy(alpha=.5f))else null){
  Row(Modifier.fillMaxSize().padding(horizontal=10.dp),verticalAlignment=Alignment.CenterVertically){
   Box(Modifier.size(36.dp)){
    Box(Modifier.fillMaxSize().clip(CircleShape).background(colors.secondaryContainer),contentAlignment=Alignment.Center){
     val image=bitmap
     if(image!=null)Image(image.asImageBitmap(),"Аватар ${player.name}",Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
     else if(player.isBot)Icon(Icons.Outlined.SmartToy,"Компьютер",tint=colors.onSecondaryContainer,modifier=Modifier.size(24.dp))
     else Text(player.name.take(1).uppercase(),style=MaterialTheme.typography.titleSmall,color=colors.onSecondaryContainer)
    }
    if(player.isTurn)Box(Modifier.align(Alignment.BottomEnd).size(8.dp).clip(CircleShape).background(colors.primary))
   }
   Spacer(Modifier.width(10.dp))
   Column(Modifier.weight(1f)){
    Text(player.name,style=MaterialTheme.typography.bodyMedium,fontWeight=if(player.isTurn)FontWeight.Bold else FontWeight.Medium,maxLines=1,overflow=TextOverflow.Ellipsis)
    if(player.capturedPieces.isNotEmpty() || player.materialAdvantage>0)Row(verticalAlignment=Alignment.CenterVertically){
     CapturedPiecesRow(player.capturedPieces,!player.white,Modifier.weight(1f,fill=false))
     if(player.materialAdvantage>0)Text("+${player.materialAdvantage}",Modifier.padding(start=4.dp),style=MaterialTheme.typography.labelSmall,fontWeight=FontWeight.Bold,color=colors.onSurfaceVariant)
    }
   }
   if(clockContent!=null)Box(Modifier.padding(start=8.dp)){clockContent()}
  }
 }
}
@Composable fun PlayerClock(text:String,active:Boolean){
 Surface(shape=RoundedCornerShape(8.dp),color=if(active)MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest,modifier=Modifier.width(68.dp).height(32.dp)){
  Box(contentAlignment=Alignment.Center){Text(text,style=MaterialTheme.typography.titleMedium,fontFamily=FontFamily.Monospace,textAlign=TextAlign.Center,maxLines=1)}
 }
}
@Composable internal fun CapturedPiecesRow(pieces:List<PieceType>,white:Boolean,modifier:Modifier=Modifier){
 val context=LocalContext.current
 val prefs=remember{context.getSharedPreferences("board",0)}
 var set by remember{mutableStateOf(prefs.getString("pieces","cburnett")?:"cburnett")}
 DisposableEffect(prefs){val listener=android.content.SharedPreferences.OnSharedPreferenceChangeListener{_,key->if(key=="pieces")set=prefs.getString("pieces","cburnett")?:"cburnett"};prefs.registerOnSharedPreferenceChangeListener(listener);onDispose{prefs.unregisterOnSharedPreferenceChangeListener(listener)}}
 val graphics by produceState<PieceGraphics?>(null,set){value=withContext(Dispatchers.IO){runCatching{PieceGraphics(context,set)}.getOrNull()}}
 Canvas(modifier.widthIn(max=180.dp).width((if(pieces.isEmpty())0 else 16+(pieces.size-1)*12).dp).height(16.dp)){
  val pieceSize=16.dp.toPx();val step=if(pieces.size<=1)0f else minOf(12.dp.toPx(),((size.width-pieceSize)/(pieces.size-1)).coerceAtLeast(0f))
  pieces.forEachIndexed{i,p->graphics?.draw(drawContext.canvas.nativeCanvas,if(white)p.symbol else p.symbol.lowercaseChar(),pieceSize/2+i*step,size.height/2,pieceSize)}
 }
}
