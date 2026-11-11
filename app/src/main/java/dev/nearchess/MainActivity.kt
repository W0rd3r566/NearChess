package dev.nearchess

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

class MainActivity:ComponentActivity(){override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{NearTheme{NearChess()}}}}
@Composable fun NearChess(){
 val context=LocalContext.current
 val prefs=remember{context.getSharedPreferences("game",0)}
 var history by remember{mutableStateOf(listOf(Position()))}
 var notes by remember{mutableStateOf(emptyList<String>())}
 var bot by remember{mutableStateOf(false)}
 var playing by remember{mutableStateOf(false)}
 var last by remember{mutableStateOf<Move?>(null)}
 var result by remember{mutableStateOf("")}
 var fen by remember{mutableStateOf<String?>(null)}
 var error by remember{mutableStateOf("")}
 var restart by remember{mutableStateOf(false)}
 val clip=LocalClipboardManager.current
 val p=history.last()
 val legal=remember(p){p.legal()}
 val repeats=remember(history){history.count{it.key()==p.key()}}
 val outcome=when{result.isNotEmpty()->result;legal.isEmpty()->if(p.check())"Мат · ${if(p.white)"чёрные" else "белые"} победили" else "Ничья · пат";p.insufficient()->"Ничья · недостаточно материала";p.half>=150 || repeats>=5->"Ничья";else->""}
 fun save(){val a=JSONArray();history.forEach{a.put(it.fen())};prefs.edit().putString("positions",a.toString()).putString("san",JSONArray(notes).toString()).putBoolean("bot",bot).putString("result",result).apply()}
 fun start(training:Boolean){bot=training;history=listOf(Position());notes=emptyList();last=null;result="";playing=true;save()}
 fun move(m:Move){if(m !in legal || outcome.isNotEmpty())return;notes=notes+p.san(m);history=history+p.play(m);last=m;save()}
 LaunchedEffect(p,bot,playing,outcome){if(playing && bot && !p.white && outcome.isEmpty()){val m=withContext(Dispatchers.Default){chooseTrainingMove(p)};if(m!=null)move(m)}}
 if(playing) MatchScreen(
  title=if(bot)"Тренировка" else "Локальная партия",history=history,notation=notes,lastMove=last,
  enabled=outcome.isEmpty() && !(bot && !p.white),whiteName=context.getSharedPreferences("profile",0).getString("nickname","Белые")?:"Белые",blackName=if(bot)"Бот" else "Чёрные",
  status=outcome.ifEmpty{if(bot && !p.white)"Бот думает…" else if(p.white)"Ход белых" else "Ход чёрных"},onBack={playing=false},onMove={move(it)},menuContent={
   TextButton(onClick={restart=true}){Text(if(outcome.isEmpty())"Новая партия" else "Реванш")}
   TextButton(enabled=history.size>1 && !(bot && !p.white),onClick={val n=if(bot && history.size>=3)2 else 1;history=history.dropLast(n);notes=notes.dropLast(n);last=null;result="";save()}){Text("Отменить ход")}
   if(outcome.isEmpty() && (p.half>=100 || repeats>=3))TextButton(onClick={result="Ничья по требованию";save()}){Text("Заявить ничью")}
   TextButton(onClick={clip.setText(AnnotatedString(p.fen()))}){Text("Копировать FEN")}
   TextButton(onClick={fen=""}){Text("Загрузить FEN")}
   TextButton(onClick={val end=if(outcome.startsWith("Мат"))if(p.white)"0-1" else "1-0" else if(outcome.startsWith("Ничья"))"1/2-1/2" else "*";val body=notes.mapIndexed{i,s->(if(history[i].white)"${history[i].full}. " else if(i==0)"${history[i].full}... " else "")+s}.joinToString(" ");val tags="[Event \"NearChess\"]\n[Result \"$end\"]\n"+(if(history.first()!=Position())"[SetUp \"1\"]\n[FEN \"${history.first().fen()}\"]\n" else "");clip.setText(AnnotatedString("$tags\n$body $end"))}){Text("Экспорт PGN")}
  }
 ) else Surface(Modifier.fillMaxSize()){Column(Modifier.systemBarsPadding().padding(24.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){
  Text("Выбери партию",style=MaterialTheme.typography.headlineMedium)
  Button(onClick={start(false)},modifier=Modifier.fillMaxWidth()){Text("Вдвоём на одном телефоне")}
  FilledTonalButton(onClick={start(true)},modifier=Modifier.fillMaxWidth()){Text("Против компьютера")}
  OutlinedButton(onClick={runCatching{val a=JSONArray(prefs.getString("positions","[]"));require(a.length()>0);val h=(0 until a.length()).map{Position.fromFen(a.getString(it))};val b=JSONArray(prefs.getString("san","[]"));val n=(0 until b.length()).map{b.getString(it)};require(h.size==n.size+1);history=h;notes=n;bot=prefs.getBoolean("bot",false);result=prefs.getString("result","")?:"";last=null;playing=true}.onFailure{error="Сохранение отсутствует или повреждено"}},modifier=Modifier.fillMaxWidth()){Text("Продолжить сохранённую")}
  TextButton(onClick={(context as? android.app.Activity)?.finish()}){Text("Главная")}
  if(error.isNotEmpty())Text(error,color=MaterialTheme.colorScheme.error)
 }}
 if(restart)AlertDialog(onDismissRequest={restart=false},title={Text("Начать новую партию?")},text={Text("Текущее сохранение будет заменено.")},confirmButton={TextButton(onClick={restart=false;start(bot)}){Text("Начать")}},dismissButton={TextButton(onClick={restart=false}){Text("Отмена")}})
 if(fen!=null)AlertDialog(onDismissRequest={fen=null},title={Text("Позиция FEN")},text={Column{OutlinedTextField(value=fen?:"",onValueChange={fen=it});if(error.isNotEmpty())Text(error)}},confirmButton={TextButton(onClick={runCatching{Position.fromFen(fen?:"")}.onSuccess{history=listOf(it);notes=emptyList();last=null;result="";fen=null;error="";save()}.onFailure{error="Некорректный FEN"}}){Text("Загрузить")}},dismissButton={TextButton(onClick={fen=null}){Text("Отмена")}})
}
