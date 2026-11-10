package dev.nearchess

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

class MainActivity: ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { NearChess() } }
}
private val mint=Color(0xFF71D8BC)
@Composable fun NearChess() {
 val context=LocalContext.current
 val prefs=remember{context.getSharedPreferences("game",0)}
 var history by remember { mutableStateOf(listOf(Position())) }
 var notation by remember { mutableStateOf(listOf<String>()) }
 var screen by remember { mutableStateOf("home") }
 var bot by remember { mutableStateOf(false) }
 var flipped by remember { mutableStateOf(false) }
 var lastMove by remember { mutableStateOf<Move?>(null) }
 var message by remember { mutableStateOf("") }
 var result by remember { mutableStateOf("") }
 var confirmNew by remember { mutableStateOf(false) }
 var fenInput by remember { mutableStateOf<String?>(null) }
 var appearance by remember { mutableStateOf(false) }
 var theme by remember { mutableStateOf(prefs.getInt("theme",0).coerceIn(boardPalettes.indices)) }
 var avatar by remember { mutableStateOf(prefs.getInt("avatar",0).coerceIn(0..3)) }
 val clipboard=LocalClipboardManager.current
 val p=history.last()
 val legal=remember(p){p.legal()}
 val repetitions=remember(history){history.count{it.key()==p.key()}}
 val terminal=when {
  result.isNotEmpty() -> result
  legal.isEmpty() -> if(p.check()) "Мат. ${if(p.white) "Чёрные" else "Белые"} победили" else "Ничья: пат"
  p.insufficient() -> "Ничья: недостаточно материала"
  p.half>=150 -> "Ничья: 75 ходов"
  repetitions>=5 -> "Ничья: пятикратное повторение"
  else -> ""
 }
 fun save() {
  val json=JSONArray(); history.forEach{json.put(it.fen())}
  prefs.edit().putString("positions",json.toString()).putString("san",JSONArray(notation).toString()).putBoolean("bot",bot).putString("result",result).apply()
 }
 fun start(training: Boolean) {
  bot=training; history=listOf(Position()); notation=emptyList(); result=""; lastMove=null; screen="game"; save()
 }
 fun applyMove(m: Move) {
  if(m !in legal || terminal.isNotEmpty()) return
  notation=notation+p.san(m); history=history+p.play(m); lastMove=m; save()
 }
 LaunchedEffect(p,bot,screen,terminal) {
  if(screen=="game" && bot && !p.white && terminal.isEmpty()) {
   val move=withContext(Dispatchers.Default){chooseBotMove(p)}
   if(move!=null) applyMove(move)
  }
 }
 MaterialTheme(colorScheme=darkColorScheme(primary=mint,background=Color(0xFF121A1C),surface=Color(0xFF1E292C))) {
  Surface(Modifier.fillMaxSize()) {
   Column(Modifier.systemBarsPadding().padding(12.dp).verticalScroll(rememberScrollState()),horizontalAlignment=Alignment.CenterHorizontally) {
    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween) {
     Text("NearChess",fontSize=26.sp,color=mint)
     TextButton(onClick={appearance=true}){Text("Оформление")}
    }
    Spacer(Modifier.height(12.dp))
    if(screen=="home") {
     PlayerAvatar(avatar,Modifier.size(88.dp))
     Spacer(Modifier.height(16.dp))
     Text("Хорошая партия начинается рядом",fontSize=20.sp)
     Spacer(Modifier.height(24.dp))
     Button(onClick={start(false)},modifier=Modifier.fillMaxWidth()){Text("Одна доска — вдвоём")}
     Button(onClick={start(true)},modifier=Modifier.fillMaxWidth()){Text("Тренировка с простым ботом")}
     OutlinedButton(onClick={
      runCatching {
       val arr=JSONArray(prefs.getString("positions","[]")); require(arr.length()>0)
       val restored=(0 until arr.length()).map{Position.fromFen(arr.getString(it))}
       val san=JSONArray(prefs.getString("san","[]")); val notes=(0 until san.length()).map{san.getString(it)}
       require(notes.size==restored.size-1)
       history=restored; notation=notes; bot=prefs.getBoolean("bot",false); result=prefs.getString("result","")?:""; lastMove=null; screen="game"
      }.onFailure{message="Нет сохранённой партии или файл повреждён"}
     },modifier=Modifier.fillMaxWidth()){Text("Продолжить партию")}
     Text("Bluetooth и чат пока в разработке. Локальная игра — без часов.",modifier=Modifier.padding(top=20.dp),color=Color(0xFFA8B8B2))
    } else {
     Row(Modifier.fillMaxWidth().padding(bottom=12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
      PlayerAvatar(if(flipped) avatar else 3)
      Column {Text(if(flipped) "Белые" else if(bot) "Тренировочный бот" else "Чёрные"); Text("${if(p.white) "Белые" else "Чёрные"} ходят",color=mint)}
     }
     ChessBoard(p,flipped,terminal.isEmpty() && !(bot && !p.white),boardPalettes[theme],lastMove){applyMove(it)}
     Row(Modifier.fillMaxWidth().padding(top=12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
      PlayerAvatar(if(flipped) 3 else avatar)
      Column {Text(if(flipped) "Чёрные" else "Белые"); Text(terminal.ifEmpty{if(bot && !p.white) "Бот думает…" else if(p.check()) "Шах" else "Перетащи фигуру или нажми на две клетки"},color=Color(0xFFA8B8B2),fontSize=13.sp)}
     }
     Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
      TextButton(onClick={flipped=!flipped}){Text("Перевернуть")}
      TextButton(onClick={screen="home"}){Text("Главная")}
      TextButton(enabled=history.size>1 && !(bot && !p.white),onClick={
       val count=if(bot && history.size>=3) 2 else 1
       history=history.dropLast(count); notation=notation.dropLast(count); lastMove=null; result=""; save()
      }){Text("Назад")}
     }
     Card(Modifier.fillMaxWidth()) { Text(notation.mapIndexed{i,s -> if(history[i].white) "${history[i].full}. $s" else s}.joinToString(" ").ifEmpty{"Сделайте первый ход"},modifier=Modifier.padding(12.dp)) }
     if(terminal.isEmpty() && (p.half>=100 || repetitions>=3)) Button(onClick={result="Ничья по требованию игрока"; save()}){Text("Заявить ничью")}
     Row {
      TextButton(onClick={clipboard.setText(AnnotatedString(p.fen())); message="FEN скопирован"}){Text("FEN")}
      TextButton(onClick={fenInput=""}){Text("Позиция")}
      TextButton(onClick={
       val start=history.first(); val end=if(terminal.startsWith("Мат")) if(p.white) "0-1" else "1-0" else if(terminal.startsWith("Ничья")) "1/2-1/2" else "*"
       val turns=buildString { notation.forEachIndexed{i,s -> val pos=history[i]; if(pos.white) append("${pos.full}. ") else if(i==0) append("${pos.full}... "); append("$s ") }; append(end) }
       val tags="[Event \"NearChess local\"]\n[Result \"$end\"]\n"+if(start!=Position()) "[SetUp \"1\"]\n[FEN \"${start.fen()}\"]\n" else ""
       clipboard.setText(AnnotatedString("$tags\n$turns")); message="PGN скопирован"
      }){Text("PGN")}
      TextButton(onClick={confirmNew=true}){Text("Заново")}
     }
    }
    if(message.isNotEmpty()) TextButton(onClick={message=""}){Text(message)}
   }
  }
  if(appearance) AlertDialog(onDismissRequest={appearance=false},title={Text("Твой NearChess")},text={Column {
   Text("Доска")
   boardPalettes.forEachIndexed{i,v -> TextButton(onClick={theme=i; prefs.edit().putInt("theme",i).apply()}){Text((if(i==theme) "✓ " else "")+v.name)}}
   Text("Аватар")
   Row {for(i in 0..3) TextButton(onClick={avatar=i; prefs.edit().putInt("avatar",i).apply()}){PlayerAvatar(i,Modifier.size(36.dp))}}
  }},confirmButton={TextButton(onClick={appearance=false}){Text("Готово")}})
  if(confirmNew) AlertDialog(onDismissRequest={confirmNew=false},title={Text("Начать заново?")},text={Text("Текущее сохранение будет заменено.")},confirmButton={TextButton(onClick={confirmNew=false; start(bot)}){Text("Начать")}},dismissButton={TextButton(onClick={confirmNew=false}){Text("Отмена")}})
  if(fenInput!=null) AlertDialog(onDismissRequest={fenInput=null},title={Text("Позиция FEN")},text={OutlinedTextField(value=fenInput?:"",onValueChange={fenInput=it})},confirmButton={TextButton(onClick={
   runCatching{Position.fromFen(fenInput?:"")}.onSuccess{history=listOf(it); notation=emptyList(); result=""; lastMove=null; fenInput=null; save()}.onFailure{message="Некорректный FEN"}
  }){Text("Загрузить")}},dismissButton={TextButton(onClick={fenInput=null}){Text("Отмена")}})
 }
}
private fun chooseBotMove(p: Position): Move? {
 fun value(c: Char)=when(c.uppercaseChar()){'P'->100;'N'->320;'B'->330;'R'->500;'Q'->900;else->0}
 fun evaluate(pos: Position): Int = pos.board.sumOf{if(it=='.') 0 else value(it)*if(it.isUpperCase()==pos.white) 1 else -1}
 fun search(pos: Position,depth: Int,alphaIn: Int,beta: Int): Int {
  val moves=pos.legal(); if(moves.isEmpty()) return if(pos.check()) -100000-depth else 0
  if(pos.insufficient()) return 0
  if(depth==0) return evaluate(pos)
  var alpha=alphaIn
  for(m in moves.sortedByDescending{value(pos.board[it.to])}) {
   val score=-search(pos.play(it=m),depth-1,-beta,-alpha)
   if(score>=beta) return beta
   if(score>alpha) alpha=score
  }
  return alpha
 }
 return p.legal().maxByOrNull{-search(p.play(it),2,-200000,200000)}
}
