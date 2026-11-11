package dev.nearchess
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable internal fun PromotionChoice(moves:List<Move>,white:Boolean,set:String,onDismiss:()->Unit,onChoose:(Move)->Unit){
 val context=LocalContext.current
 val graphics by produceState<PieceGraphics?>(null,set){value=withContext(Dispatchers.IO){runCatching{PieceGraphics(context,set)}.getOrNull()}}
 AlertDialog(onDismissRequest=onDismiss,title={Text("Выбери фигуру")},text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){
  moves.chunked(2).forEach{pair->Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
   pair.forEach{move->
    val label=when(move.promotion){'q'->"Ферзь";'r'->"Ладья";'b'->"Слон";else->"Конь"}
    FilledTonalButton(onClick={onChoose(move)},enabled=graphics!=null,modifier=Modifier.weight(1f),contentPadding=PaddingValues(8.dp)){
     Column(horizontalAlignment=androidx.compose.ui.Alignment.CenterHorizontally){
      Canvas(Modifier.size(52.dp)){graphics?.draw(drawContext.canvas.nativeCanvas,if(white)move.promotion.uppercaseChar()else move.promotion.lowercaseChar(),size.width/2,size.height/2,size.minDimension)}
      Text(label,style=MaterialTheme.typography.labelMedium)
     }
    }
   }
  }}
  if(graphics==null)Text("Загружаю фигуры…",style=MaterialTheme.typography.bodySmall)
 }},confirmButton={},dismissButton={TextButton(onClick=onDismiss){Text("Отмена")}})
}
