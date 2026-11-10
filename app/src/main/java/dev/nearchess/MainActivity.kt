package dev.nearchess

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
private val glyphs=mapOf('K' to "♔",'Q' to "♕",'R' to "♖",'B' to "♗",'N' to "♘",'P' to "♙",'k' to "♚",'q' to "♛",'r' to "♜",'b' to "♝",'n' to "♞",'p' to "♟")

@Composable fun NearChess() {
 val context=LocalContext.current
 val prefs=remember{context.getSharedPreferences("game",0)}
 var history by remember { mutableStateOf(listOf(Position())) }
 var notation by remember { mutableStateOf(listOf<String>()) }
 var screen by remember { mutableStateOf("home") }
 var bot by remember { mutableStateOf(false) }
 var flipped by remember { mutableStateOf(false) }
 var selected by remember { mutableStateOf<Int?>(null) }
 var promotion by remember { mutableStateOf(listOf<Move>()) }
 var message by remember { mutableStateOf("") }
 var result by remember { mutableStateOf("") }
 var confirmNew by remember { mutableStateOf(false) }
 var fenInput by remember { mutableStateOf<String?>(null) }
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
  bot=training; history=listOf(Position()); notation=emptyList(); result=""; selected=null; promotion=emptyList(); screen="game"; save()
 }
 fun applyMove(m: Move) {
  if(m !in legal || terminal.isNotEmpty()) return
  notation=notation+p.san(m); history=history+p.play(m); selected=null; promotion=emptyList(); save()
 }
 LaunchedEffect(p,bot,screen,terminal) {
  if(screen=="game" && bot && !p.white && terminal.isEmpty()) {
   val move=withContext(Dispatchers.Default){chooseBotMove(p)}
   if(move!=null) applyMove(move)
  }
 }
 MaterialTheme(colorScheme=darkColorScheme(primary=mint,background=Color(0xFF121A1C),surface=Color(0xFF1E292C))) {
  Surface(Modifier.fillMaxSize()) {
   Column(Modifier.systemBarsPadding().padding(16.dp).verticalScroll(rememberScrollState()),horizontalAlignment=Alignment.CenterHorizontally) {
    Text("NearChess",fontSize=30.sp,color=mint)
    Text("Шахматы рядом",color=Color(0xFFA8B8B2))
    Spacer(Modifier.height(20.dp))
    if(screen=="home") {
     Button(onClick={start(false)},modifier=Modifier.fillMaxWidth()){Text("Одна доска — вдвоём")}
     Button(onClick={start(true)},modifier=Modifier.fillMaxWidth()){Text("Тренировка с простым ботом")}
     OutlinedButton(onClick={
      runCatching {
       val arr=JSONArray(prefs.getString("positions","[]")); require(arr.length()>0)
       val restored=(0 until arr.length()).map{Position.fromFen(arr.getString(it))}
       val san=JSONArray(prefs.getString("san","[]")); val notes=(0 until san.length()).map{san.getString(it)}
       require(notes.size==restored.size-1)
       history=restored; notation=notes; bot=prefs.getBoolean("bot",false); result=prefs.getString("result","")?:""; selected=null; screen="game"
      }.onFailure{message="Нет сохранённой партии или файл повреждён"}
     },modifier=Modifier.fillMaxWidth()){Text("Продолжить сохранённую партию")}
     Text("Bluetooth, чат и реакции — в разработке. Эта сборка: локальная игра без часов.",modifier=Modifier.padding(top=20.dp))
    } else {
     Text(terminal.ifEmpty{if(bot && !p.white) "Бот думает…" else "Ход ${if(p.white) "белых" else "чёрных"}${if(p.check()) " · шах" else ""}"},fontSize=20.sp)
     Spacer(Modifier.height(12.dp))
     Column(Modifier.fillMaxWidth().aspectRatio(1f)) {
      for(row in 0..7) Row(Modifier.weight(1f)) {
       for(col in 0..7) {
        val s=if(flipped) row*8+7-col else (7-row)*8+col
        val piece=p.board[s]
        val target=legal.any{it.from==selected && it.to==s}
        val color=when { selected==s -> mint; target -> Color(0xFFE9C96B); else -> if((s%8+s/8)%2==0) Color(0xFF56877C) else Color(0xFFE7E8D5) }
        Box(Modifier.weight(1f).fillMaxHeight().background(color).clickable {
         if(terminal.isEmpty() && !(bot && !p.white)) {
          val candidates=legal.filter{it.from==selected && it.to==s}
          if(candidates.size>1) promotion=candidates
          else if(candidates.size==1) applyMove(candidates.single())
          else selected=if(p.own(piece)) s else null
         }
        },contentAlignment=Alignment.Center) {
         Text(glyphs[piece]?:"",fontSize=36.sp,color=Color(0xFF111B1D))
         if(col==0 || row==7) Text(Move.square(s),fontSize=9.sp,color=Color(0xFF243D35),modifier=Modifier.align(Alignment.BottomStart))
        }
       }
      }
     }
     Spacer(Modifier.height(12.dp))
     Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
      OutlinedButton(onClick={flipped=!flipped}){Text("Перевернуть")}
      OutlinedButton(onClick={screen="home"}){Text("Меню")}
     }
     Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
      OutlinedButton(enabled=history.size>1 && !(bot && !p.white),onClick={
       val count=if(bot && history.size>=3) 2 else 1
       history=history.dropLast(count); notation=notation.dropLast(count); selected=null; result=""; save()
      }){Text("Отменить ход")}
      OutlinedButton(onClick={confirmNew=true}){Text("Новая партия")}
     }
     if(terminal.isEmpty() && (p.half>=100 || repetitions>=3)) Button(onClick={result="Ничья по требованию игрока"; save()}){Text("Заявить ничью")}
     Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
      TextButton(onClick={clipboard.setText(AnnotatedString(p.fen())); message="FEN скопирован"}){Text("FEN")}
      TextButton(onClick={fenInput=""}){Text("Загрузить FEN")}
      TextButton(onClick={
       val start=history.first(); val end=if(terminal.startsWith("Мат")) if(p.white) "0-1" else "1-0" else if(terminal.startsWith("Ничья")) "1/2-1/2" else "*"
       val turns=buildString { notation.forEachIndexed{i,s -> val pos=history[i]; if(pos.white) append("${pos.full}. ") else if(i==0) append("${pos.full}... "); append("$s ") }; append(end) }
       val tags="[Event \"NearChess local\"]\n[Result \"$end\"]\n"+if(start!=Position()) "[SetUp \"1\"]\n[FEN \"${start.fen()}\"]\n" else ""
       clipboard.setText(AnnotatedString("$tags\n$turns")); message="PGN скопирован"
      }){Text("PGN")}
     }
     Text(notation.mapIndexed{i,s -> if(history[i].white) "${history[i].full}. $s" else s}.joinToString(" "),modifier=Modifier.fillMaxWidth())
    }
    if(message.isNotEmpty()) TextButton(onClick={message=""}){Text(message)}
   }
  }
  if(promotion.isNotEmpty()) AlertDialog(onDismissRequest={promotion=emptyList()},title={Text("Превращение пешки")},text={Row{promotion.forEach{m -> TextButton(onClick={applyMove(m)}){Text(m.promotion.uppercase())}}}},confirmButton={})
  if(confirmNew) AlertDialog(onDismissRequest={confirmNew=false},title={Text("Начать заново?")},text={Text("Текущее сохранение будет заменено.")},confirmButton={TextButton(onClick={confirmNew=false; start(bot)}){Text("Начать")}},dismissButton={TextButton(onClick={confirmNew=false}){Text("Отмена")}})
  if(fenInput!=null) AlertDialog(onDismissRequest={fenInput=null},title={Text("Позиция FEN")},text={OutlinedTextField(value=fenInput?:"",onValueChange={fenInput=it})},confirmButton={TextButton(onClick={
   runCatching{Position.fromFen(fenInput?:"")}.onSuccess{history=listOf(it); notation=emptyList(); result=""; selected=null; fenInput=null; save()}.onFailure{message="Некорректный FEN"}
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
   val score=-search(pos.play(m),depth-1,-beta,-alpha)
   if(score>=beta) return beta
   if(score>alpha) alpha=score
  }
  return alpha
 }
 return p.legal().maxByOrNull{-search(p.play(it),2,-200000,200000)}
}
