package dev.nearchess

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.json.JSONObject

class NearbyActivity: ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) {super.onCreate(savedInstanceState); setContent{MaterialTheme(colorScheme=darkColorScheme()){NearbyScreen()}}}
}
class NearbyModel: ViewModel() {
 var position by mutableStateOf(Position()); private set
 var connected by mutableStateOf(false); private set
 var hosting by mutableStateOf(false); private set
 var busy by mutableStateOf(false); private set
 var status by mutableStateOf("Создай игру или выбери сопряжённый телефон"); private set
 var peer by mutableStateOf("Друг"); private set
 var result by mutableStateOf(""); private set
 var messages by mutableStateOf(emptyList<String>()); private set
 var lastMove by mutableStateOf<Move?>(null); private set
 var sequence by mutableStateOf(0); private set
 var pending by mutableStateOf(false); private set
 private var transport: BluetoothTransport?=null
 private var task: Job?=null
 private var session=MatchSession()
 private var epoch=0
 private var lastChat=0L
 fun disconnect() {
  epoch++; task?.cancel(); transport?.close(); transport=null; task=null
  connected=false; busy=false; pending=false; status="Соединение закрыто. Подключитесь снова для новой партии."
 }
 fun begin(adapter: BluetoothAdapter,address: String?,name: String) {
  disconnect(); val id=epoch
  hosting=address==null; busy=true; result=""; position=Position(); sequence=0; lastMove=null; session=MatchSession(); messages=emptyList()
  val link=BluetoothTransport(adapter); transport=link
  status=if(hosting) "Ожидаю друга (до 2 минут)…" else "Подключаюсь…"
  task=viewModelScope.launch {
   try {
    if(hosting) link.host() else link.connect(requireNotNull(address))
    link.send(WireMessage(Wire.HELLO,0,JSONObject().put("name",name.take(24)).put("host",hosting).toString()))
    val hello=link.receive(); require(hello.type==Wire.HELLO)
    if(id!=epoch) return@launch
    val profile=JSONObject(hello.payload); require(profile.getBoolean("host")!=hosting){"Одинаковая роль на двух устройствах"}
    peer=profile.optString("name","Друг").take(24); connected=true; busy=false
    status=if(hosting) "Ты за белых" else "Ты за чёрных"
    if(hosting) sendState(link)
    while(id==epoch) {
     val msg=link.receive()
     if(id!=epoch) break
     when(msg.type) {
      Wire.MOVE -> {
       require(hosting)
       val move=Uci.parse(msg.payload)
       if(session.acceptMove(msg.sequence,false,move.uci())) {lastMove=move; syncHost()}
       sendState(link)
      }
      Wire.STATE -> {
       require(!hosting)
       val json=JSONObject(msg.payload); require(msg.sequence>=sequence)
       val restored=Position.fromFen(json.getString("fen"))
       val end=json.optString("result"); require(end in listOf("","1-0","0-1","1/2-1/2"))
       val last=json.optString("last").takeIf{it.isNotEmpty()}?.let{Uci.parse(it)}
       position=restored; sequence=msg.sequence; result=end; pending=false; lastMove=last
      }
      Wire.CHAT,Wire.REACTION -> {
       require(msg.payload.length<=500)
       messages=(messages+"$peer: ${msg.payload}").takeLast(100)
      }
      Wire.RESIGN -> {require(hosting); session.resign(false); syncHost(); sendState(link)}
      else -> error("Неожиданное сообщение протокола")
     }
    }
   } catch(e: CancellationException) {throw e}
   catch(e: Exception) {if(id==epoch) {status="Связь прервана: ${e.message?:"ошибка Bluetooth"}"; connected=false; busy=false; pending=false}}
   finally {link.close()}
  }
 }
 private fun syncHost(){position=session.position; sequence=session.sequence; result=session.result}
 private suspend fun sendState(link: BluetoothTransport) {
  link.send(WireMessage(Wire.STATE,session.sequence,JSONObject().put("fen",session.position.fen()).put("result",session.result).put("last",lastMove?.uci()?:"").toString()))
 }
 private fun launchSend(block: suspend (BluetoothTransport)->Unit) {
  val link=transport?:return; val id=epoch
  viewModelScope.launch {
   try {block(link)} catch(e: CancellationException){throw e}
   catch(e: Exception){if(id==epoch){disconnect(); status="Ошибка передачи: ${e.message}"}}
  }
 }
 fun move(m: Move) {
  if(!connected || pending || result.isNotEmpty() || position.white!=hosting || m !in position.legal()) return
  pending=true
  if(hosting) {
   if(session.acceptMove(sequence,true,m.uci())) {lastMove=m; syncHost(); launchSend{sendState(it)}}
   pending=false
  } else launchSend{it.send(WireMessage(Wire.MOVE,sequence,m.uci()))}
 }
 fun chat(text: String,reaction: Boolean=false): Boolean {
  if(!connected || text.isBlank()) return false
  val now=android.os.SystemClock.elapsedRealtime(); if(now-lastChat<1000) return false
  lastChat=now; val clean=text.trim().take(500)
  messages=(messages+"Я: $clean").takeLast(100)
  launchSend{it.send(WireMessage(if(reaction) Wire.REACTION else Wire.CHAT,sequence,clean))}
  return true
 }
 fun resign() {
  if(!connected || pending || result.isNotEmpty()) return
  if(hosting){session.resign(true); syncHost(); launchSend{sendState(it)}}
  else {pending=true; launchSend{it.send(WireMessage(Wire.RESIGN,sequence,""))}}
 }
 override fun onCleared(){transport?.close(); super.onCleared()}
}

@SuppressLint("MissingPermission")
@Composable private fun NearbyScreen(vm: NearbyModel=viewModel()) {
 val context=LocalContext.current
 val adapter=remember{context.getSystemService(BluetoothManager::class.java)?.adapter}
 var devices by remember{mutableStateOf(emptyList<Pair<String,String>>())}
 var permission by remember{mutableStateOf(Build.VERSION.SDK_INT<31 || context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)==PackageManager.PERMISSION_GRANTED)}
 var localError by remember{mutableStateOf("")}
 var draft by remember{mutableStateOf("")}
 var confirmResign by remember{mutableStateOf(false)}
 var confirmDisconnect by remember{mutableStateOf(false)}
 var chatVisible by remember{mutableStateOf(false)}
 fun refresh(){runCatching{devices=adapter?.bondedDevices?.map{(it.name?:"Телефон") to it.address}?:emptyList()}.onFailure{localError="Нет разрешения Bluetooth"}}
 val request=rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){grants -> permission=grants[Manifest.permission.BLUETOOTH_CONNECT]==true; if(permission) refresh()}
 val enable=rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()){refresh()}
 val discover=rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()){refresh()}
 LaunchedEffect(permission){if(permission) refresh()}
 val name=context.getSharedPreferences("profile",0).getString("nickname","Игрок")?:"Игрок"
 val theme=context.getSharedPreferences("game",0).getInt("theme",0).coerceIn(boardPalettes.indices)
 Surface(Modifier.fillMaxSize()) {
  Column(Modifier.systemBarsPadding().imePadding().padding(16.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(8.dp)) {
   Text("С другом по Bluetooth",style=MaterialTheme.typography.headlineSmall)
   Text(vm.status,style=MaterialTheme.typography.bodyMedium)
   if(localError.isNotEmpty()) Text(localError,color=MaterialTheme.colorScheme.error)
   if(!vm.connected && !vm.busy) {
    if(adapter==null) Text("Устройство не поддерживает Bluetooth")
    else if(!permission) Button(onClick={if(Build.VERSION.SDK_INT>=31) request.launch(arrayOf(Manifest.permission.BLUETOOTH_CONNECT,Manifest.permission.BLUETOOTH_ADVERTISE))}){Text("Разрешить Bluetooth")}
    else {
     Button(onClick={runCatching{enable.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))}.onFailure{localError="Не удалось включить Bluetooth"}}){Text("Включить Bluetooth")}
     FilledTonalButton(onClick={runCatching{discover.launch(Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,120))}.onFailure{localError="Разрешите обнаружение в настройках"}}){Text("Сделать телефон видимым")}
     Button(onClick={runCatching{check(adapter.isEnabled){"Включите Bluetooth"}; vm.begin(adapter,null,name)}.onFailure{localError=it.message?:"Ошибка"}}){Text("Создать партию · белые")}
     Text("Сначала выполните сопряжение телефонов в настройках Android. Друг создаёт партию; затем выберите его ниже.",style=MaterialTheme.typography.bodySmall)
     OutlinedButton(onClick={context.startActivity(Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS))}){Text("Настройки Bluetooth")}
     TextButton(onClick={refresh()}){Text("Обновить список")}
     devices.forEach{(label,address) -> OutlinedButton(onClick={vm.begin(adapter,address,name)},modifier=Modifier.fillMaxWidth()){Text("$label · за чёрных")}}
    }
   }
   if(vm.busy) {LinearProgressIndicator(Modifier.fillMaxWidth()); TextButton(onClick={vm.disconnect()}){Text("Отменить")}}
   if(vm.connected || vm.sequence>0) {
    Text("$name — ${vm.peer}",style=MaterialTheme.typography.titleMedium)
    Text(vm.result.ifEmpty{if(vm.position.white) "Ход белых" else "Ход чёрных"})
    ChessBoard(vm.position,!vm.hosting,vm.connected && !vm.pending && vm.result.isEmpty() && vm.position.white==vm.hosting,boardPalettes[theme],vm.lastMove){vm.move(it)}
    Row {TextButton(onClick={chatVisible=!chatVisible}){Text("Чат (${vm.messages.size})")}; TextButton(enabled=vm.connected && vm.result.isEmpty(),onClick={confirmResign=true}){Text("Сдаться")}; TextButton(onClick={confirmDisconnect=true}){Text("Отключиться")}}
    if(chatVisible) {
     vm.messages.takeLast(20).forEach{Text(it,style=MaterialTheme.typography.bodyMedium)}
     Row {listOf("😂","🤔","😎","🤝").forEach{emoji -> TextButton(enabled=vm.connected,onClick={vm.chat(emoji,true)}){Text(emoji)}}}
     OutlinedTextField(value=draft,onValueChange={draft=it.take(500)},label={Text("Сообщение другу")},modifier=Modifier.fillMaxWidth())
     Button(enabled=vm.connected && draft.isNotBlank(),onClick={if(vm.chat(draft)) draft=""}){Text("Отправить")}
    }
   }
  }
 }
 if(confirmResign) AlertDialog(onDismissRequest={confirmResign=false},title={Text("Сдаться?")},confirmButton={TextButton(onClick={vm.resign(); confirmResign=false}){Text("Сдаться")}},dismissButton={TextButton(onClick={confirmResign=false}){Text("Продолжить")}})
 if(confirmDisconnect) AlertDialog(onDismissRequest={confirmDisconnect=false},title={Text("Закрыть соединение?")},text={Text("Автовосстановление партии пока не реализовано.")},confirmButton={TextButton(onClick={vm.disconnect(); confirmDisconnect=false}){Text("Отключиться")}},dismissButton={TextButton(onClick={confirmDisconnect=false}){Text("Отмена")}})
}
