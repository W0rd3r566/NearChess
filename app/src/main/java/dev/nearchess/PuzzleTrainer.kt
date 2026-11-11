package dev.nearchess

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class Puzzle(val id:String,val fen:String,val line:List<String>,val rating:Int)
private class PuzzleDb(context:Context):SQLiteOpenHelper(context,"puzzles.db",null,1){
 override fun onCreate(db:SQLiteDatabase){db.execSQL("CREATE TABLE puzzle(id TEXT PRIMARY KEY, fen TEXT NOT NULL, moves TEXT NOT NULL, rating INTEGER NOT NULL)")}
 override fun onUpgrade(db:SQLiteDatabase,old:Int,new:Int){}
 fun count():Long=readableDatabase.rawQuery("SELECT COUNT(*) FROM puzzle",null).use{it.moveToFirst();it.getLong(0)}
 fun next():Puzzle?=readableDatabase.rawQuery("SELECT id,fen,moves,rating FROM puzzle WHERE rowid >= (SELECT IFNULL(MIN(rowid),1) + ABS(RANDOM() % MAX(1,IFNULL(MAX(rowid)-MIN(rowid)+1,1))) FROM puzzle) ORDER BY rowid LIMIT 1",null).use{if(it.moveToFirst())Puzzle(it.getString(0),it.getString(1),it.getString(2).split(' '),it.getInt(3))else null}
 fun importCsv(input:java.io.InputStream):Int{
  val db=writableDatabase;var imported=0
  db.beginTransaction()
  try{
   val stmt=db.compileStatement("INSERT OR IGNORE INTO puzzle(id,fen,moves,rating) VALUES(?,?,?,?)")
   stmt.use{statement->input.bufferedReader().useLines{lines->lines.forEach{row->
    val fields=row.split(',')
    if(fields.size>=4 && fields[0]!="PuzzleId"){
     val rating=fields[3].toIntOrNull()
     if(rating!=null && fields[0].length<=32 && fields[1].length<=128 && fields[2].length<=1024){
      statement.clearBindings();statement.bindString(1,fields[0]);statement.bindString(2,fields[1]);statement.bindString(3,fields[2]);statement.bindLong(4,rating.toLong())
      if(statement.executeInsert()!=-1L)imported++
     }
    }
   }}}
   db.setTransactionSuccessful()
  }finally{db.endTransaction()}
  return imported
 }
}
/** Lichess CSV FEN is BEFORE the opponent setup move (first UCI token). */
internal fun preparePuzzle(puzzle:Puzzle):Position{
 require(puzzle.line.size>=2)
 var p=Position.fromFen(puzzle.fen)
 for(token in puzzle.line){val m=Uci.parse(token);require(m in p.legal());p=p.play(m)}
 val initial=Position.fromFen(puzzle.fen)
 return initial.play(Uci.parse(puzzle.line.first()))
}
@Composable fun PuzzleTrainer(onBack:()->Unit){
 val context=LocalContext.current;val scope=rememberCoroutineScope()
 val db=remember{PuzzleDb(context)}
 var count by remember{mutableStateOf(0L)}
 var busy by remember{mutableStateOf(false)}
 var message by remember{mutableStateOf("")}
 var puzzle by remember{mutableStateOf<Puzzle?>(null)}
 var position by remember{mutableStateOf(Position())}
 var ply by remember{mutableStateOf(1)}
 var last by remember{mutableStateOf<Move?>(null)}
 var turn by remember{mutableStateOf(true)}
 var generation by remember{mutableStateOf(0)}
 fun load(){scope.launch{
  generation++;busy=true
  val loaded=withContext(Dispatchers.IO){runCatching{val q=db.next()?:error("База пуста");q to preparePuzzle(q)}}
  loaded.onSuccess{(q,p)->puzzle=q;position=p;turn=p.white;ply=1;last=null;message="Найди лучшее продолжение"}.onFailure{message="Задача отсутствует или содержит недопустимую линию";puzzle=null}
  busy=false
 }}
 val picker=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri->if(uri!=null)scope.launch{
  busy=true
  val answer=withContext(Dispatchers.IO){runCatching{context.contentResolver.openInputStream(uri)?.use{db.importCsv(it)}?:error("Не удалось открыть CSV")}}
  answer.onSuccess{message="Добавлено задач: $it"}.onFailure{message="Ошибка импорта: ${it.message}"}
  count=withContext(Dispatchers.IO){db.count()};busy=false
 }}
 LaunchedEffect(Unit){count=withContext(Dispatchers.IO){db.count()}}
 DisposableEffect(db){onDispose{db.close()}}
 Surface(Modifier.fillMaxSize()){
  Column(Modifier.fillMaxSize().systemBarsPadding().padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
   Row{TextButton(onClick=onBack){Text("Назад")};Text("Задачи",Modifier.weight(1f),style=MaterialTheme.typography.titleLarge)}
   Text("Офлайн-база: $count задач",style=MaterialTheme.typography.labelMedium)
   if(busy)LinearProgressIndicator(Modifier.fillMaxWidth())
   val q=puzzle
   if(q!=null){
    Text("${q.id} · рейтинг задачи ${q.rating}",style=MaterialTheme.typography.labelMedium)
    ChessBoard(position,!turn,!busy && ply<q.line.size && position.white==turn,boardPalettes[0],last){move->
     if(move.uci()!=q.line.getOrNull(ply)){message="Попробуй другое продолжение"}
     else{
      position=position.play(move);last=move;ply++
      if(ply>=q.line.size){message="Решено!";val prefs=context.getSharedPreferences("puzzles",0);prefs.edit().putBoolean(q.id,true).apply()}
      else{busy=true;val id=generation;scope.launch{delay(650);if(id==generation){val reply=Uci.parse(q.line[ply]);if(reply in position.legal()){position=position.play(reply);last=reply;ply++;message=if(ply>=q.line.size)"Решено!" else "Твой ход"};busy=false}}}
     }
    }
   }else Text("Импортируй распакованный CSV базы Lichess. Задачи сохраняются на телефоне; после импорта интернет не нужен.")
   Text(message,style=MaterialTheme.typography.bodyMedium)
   Row{Button(enabled=!busy && count>0,onClick={load()}){Text("Следующая")};TextButton(enabled=!busy,onClick={picker.launch(arrayOf("text/*","application/octet-stream"))}){Text("Импорт CSV")}}
   Text("Источник: database.lichess.org · CC0. ZIP/ZST необходимо распаковать перед импортом. Миллион задач не встроен в APK.",style=MaterialTheme.typography.bodySmall)
  }
 }
}
