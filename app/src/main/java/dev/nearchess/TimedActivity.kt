package dev.nearchess

import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay

class TimedActivity: ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) {super.onCreate(savedInstanceState); setContent{MaterialTheme(colorScheme=darkColorScheme()){TimedScreen()}}}
}
class TimedModel: ViewModel() {
 var position by mutableStateOf(Position()); private set
 var clock: ChessClock?=null; private set
 var white by mutableStateOf(0L); private set
 var black by mutableStateOf(0L); private set
 var paused by mutableStateOf(true); private set
 var outcome by mutableStateOf(""); private set
 var moves by mutableStateOf(emptyList<String>()); private set
 var last by mutableStateOf<Move?>(null); private set
 private var keys=mutableListOf(Position().key())
 fun start(minutes: Int, increment: Int) {
  position=Position(); keys=mutableListOf(position.key()); outcome=""; moves=emptyList(); last=null
  clock=ChessClock(minutes*60000L,increment*1000L); clock!!.start(SystemClock.elapsedRealtime()); tick()
 }
 fun tick() {
  val c=clock?:return
  c.update(SystemClock.elapsedRealtime()); white=c.whiteMillis; black=c.blackMillis; paused=!c.running
  if(c.flaggedWhite!=null && outcome.isEmpty()) {
   val winningWhite=!c.flaggedWhite!!
   val onlyKing=position.board.none{it!='.' && it.isUpperCase()==winningWhite && it.uppercaseChar()!='K'}
   outcome=if(onlyKing) "Ничья: у соперника только король" else "${if(winningWhite) "Белые" else "Чёрные"} победили по времени"
  }
 }
 fun pause(){clock?.pause(SystemClock.elapsedRealtime()); tick()}
 fun resume(){if(outcome.isEmpty()){clock?.start(SystemClock.elapsedRealtime()); tick()}}
 fun move(m: Move) {
  val c=clock?:return
  if(paused || outcome.isNotEmpty() || m !in position.legal()) return
  val san=position.san(m)
  if(!c.completeMove(position.white,SystemClock.elapsedRealtime())) {tick(); return}
  moves=moves+san; position=position.play(m); last=m; keys.add(position.key())
  outcome=when {
   position.legal().isEmpty() -> if(position.check()) "Мат. ${if(position.white) "Чёрные" else "Белые"} победили" else "Ничья: пат"
   position.insufficient() -> "Ничья: недостаточно материала"
   position.half>=150 || keys.count{it==position.key()}>=5 -> "Ничья по правилу повторений / 75 ходов"
   else -> ""
  }
  if(outcome.isNotEmpty()) c.pause(SystemClock.elapsedRealtime())
  tick()
 }
 fun canClaim()=position.half>=100 || keys.count{it==position.key()}>=3
 fun claim(){if(outcome.isEmpty() && canClaim()){outcome="Ничья по требованию"; pause()}}
}
private fun clockText(ms: Long): String {val s=(ms+999)/1000; return "%d:%02d".format(s/60,s%60)}
@Composable private fun TimedScreen(vm: TimedModel=viewModel()) {
 val context=LocalContext.current
 val owner=LocalLifecycleOwner.current
 var flipped by remember{mutableStateOf(false)}
 var restart by remember{mutableStateOf<Pair<Int,Int>?>(null)}
 val palette=context.getSharedPreferences("game",0).getInt("theme",0).coerceIn(boardPalettes.indices)
 DisposableEffect(owner) {
  val observer=LifecycleEventObserver{_,event -> if(event==Lifecycle.Event.ON_STOP) vm.pause()}
  owner.lifecycle.addObserver(observer)
  onDispose{owner.lifecycle.removeObserver(observer); vm.pause()}
 }
 LaunchedEffect(vm){while(true){vm.tick(); delay(100)}}
 Surface(Modifier.fillMaxSize()) {
  Column(Modifier.systemBarsPadding().padding(16.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(8.dp)) {
   Text("Одна доска · с часами",style=MaterialTheme.typography.headlineSmall)
   Row {for((m,i) in listOf(3 to 2,5 to 0,10 to 5)) TextButton(onClick={if(vm.clock==null) vm.start(m,i) else restart=m to i}){Text("$m+$i")}}
   if(vm.clock==null) Text("Выберите контроль времени. Белые начинают.") else {
    Text("${if(flipped) "Белые" else "Чёрные"} · ${clockText(if(flipped) vm.white else vm.black)}",style=MaterialTheme.typography.headlineLarge)
    ChessBoard(vm.position,flipped,!vm.paused && vm.outcome.isEmpty(),boardPalettes[palette],vm.last){vm.move(it)}
    Text("${if(flipped) "Чёрные" else "Белые"} · ${clockText(if(flipped) vm.black else vm.white)}",style=MaterialTheme.typography.headlineLarge)
    Text(vm.outcome.ifEmpty{if(vm.paused) "Пауза" else "Ход ${if(vm.position.white) "белых" else "чёрных"}"})
    Row {
     TextButton(onClick={flipped=!flipped}){Text("Перевернуть")}
     TextButton(enabled=vm.outcome.isEmpty(),onClick={if(vm.paused) vm.resume() else vm.pause()}){Text(if(vm.paused) "Продолжить" else "Пауза")}
    }
    if(vm.canClaim() && vm.outcome.isEmpty()) TextButton(onClick={vm.claim()}){Text("Заявить ничью")}
    Text(vm.moves.mapIndexed{i,s -> if(i%2==0) "${i/2+1}. $s" else s}.joinToString(" "))
    Text("Только локальная дружеская игра: при сворачивании часы ставятся на паузу. После завершения процесса эта партия не восстанавливается.",style=MaterialTheme.typography.bodySmall)
   }
  }
 }
 restart?.let{preset -> AlertDialog(onDismissRequest={restart=null},title={Text("Начать новую партию?")},text={Text("Текущая партия с часами будет заменена.")},confirmButton={TextButton(onClick={restart=null; vm.start(preset.first,preset.second)}){Text("Начать")}},dismissButton={TextButton(onClick={restart=null}){Text("Отмена")}})}
}
