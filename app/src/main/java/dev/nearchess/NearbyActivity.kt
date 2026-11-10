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
  connected=false; busy=false; pending=false; status="Соединение закрыто. Для новой партии подключитесь снова."
 }
 fun begin(adapter: BluetoothAdapter,address: String?,name: String) {
  disconnect(); val id=epoch
  hosting=address==null; busy=true; result=""; position=Position(); sequence=0; lastMove=null; session=MatchSession(); messages=emptyList()
  val link=BluetoothTransport(adapter); transport=link
  status=if(hosting) "Ожидаю друга (до 2 минут)…" else "Подключаюсь…"
  task=viewModelScope.launch {
   try {
    if(hosting) link.host() else link.connect(address!!)
    link.send(WireMessage(Wire.HELLO,0,JSONObject().put("name",name.take(24)).put("host",hosting).toString()))
    val hello=link.receive(); require(hello.type==Wire.HELLO)
    val profile=JSONObject(hello.payload); require(profile.getBoolean("host")!=hosting){"Оба телефона выбрали одинаковую роль"}
    peer=profile.optString("name","Друг").take(24); connected=true; busy=false
    status=if(hosting) "Ты за белых" else "Ты за чёрных"
    if(hosting) sendState(link)
    while(id==epoch) {
     val msg=link.receive()
     when(msg.type) {
      Wire.MOVE -> if(hosting) {
       if(session.acceptMove(msg.sequence,false,msg.payload)) {lastMove=parseMove(msg.payload); syncHost()}
       sendState(link)
      }
      Wire.STATE -> if(!hosting) {
       val json=JSONObject(msg.payload); require(msg.sequence>=sequence)
       val restored=Position.fromFen(json.getString("fen"))
       position=restored; sequence=msg.sequence; result=json.optString("result"); pending=false
       lastMove=json.optString("last").takeIf{it.length in 4..5}?.let{parseMove(it)}
      }
      Wire.CHAT,Wire.REACTION -> {
       require(msg.payload.length<=500)
       messages=(messages+"$peer: ${msg.payload}").takeLast(100)
      }
      Wire.RESIGN -> if(hosting) {session.resign(false); syncHost(); sendState(link)}
      else -> error("Неожиданное сообщение протокола")
     }
    }
   } catch(e: Exception) {
    if(id==epoch) {status="Связь прервана: ${e.message?:"ошибка Bluetooth"}. Позиция оставлена на экране; ходить нельзя."; connected=false; busy=false; pending=false}
   } finally {link.close()}
  }
 }
 private fun parseMove(s: String): Move {
  require(s.length in 4..5 && s[0] in 'a'..'h' && s[2] in 'a'..'h' && s[1] in '1'..'8' && s[3] in '1'..'8')
  return Move((s[1]-'1')*8+s[0]-'a',(s[3]-'1')*8+s[2]-'a',if(s.length==5) s[4] else ' ')
 }
 private fun syncHost(){position=session.position; sequence=session.sequence; result=session.result}
 private suspend fun sendState(link: BluetoothTransport) {
  link.send(WireMessage(Wire.STATE,session.sequence,JSONObject().put("fen",session.position.fen()).put("result",session.result).put("last",lastMove?.uci()?:"").toString()))
 }
 fun move(m: Move) {
  if(!connected || pending || result.isNotEmpty() || position.white!=hosting || m !in position.legal()) return
  val link=transport?:return
  pending=true
  viewModelScope.launch {
   try {
    if(hosting) {
     if(session.acceptMove(sequence,true,m.uci())) {lastMove=m; syncHost(); sendState(link)}
     pending=false
    } else link.send(WireMessage(Wire.MOVE,sequence,m.uci()))
   } catch(e: Exception){disconnect(); status="Не удалось отправить ход"}
  }
 }
 fun chat(text: String,reaction: Boolean=false) {
  if(!connected || text.isBlank()) return
  val now=android.os.SystemClock.elapsedRealtime(); if(now-lastChat<1000) return
  lastChat=now; val clean=text.trim().take(500); val link=transport?:return
  viewModelScope.launch {try {link.send(WireMessage(if(reaction) Wire.REACTION else Wire.CHAT,sequence,clean)); messages=(messages+"Я: $clean").takeLast(100)} catch(e: Exception){disconnect(); status="Не удалось отправить сообщение"}}
 }
 fun resign() {
  if(!connected || result.isNotEmpty()) return
  val link=transport?:return
  viewModelScope.launch {try{if(hosting){session.resign(true); syncHost(); sendState(link)}else{pending=true; link.send(WireMessage(Wire.RESIGN,sequence,""))}}catch(e: Exception){disconnect()}}
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
     Button(onClick={runCatching{enable.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))}.onFailure{localError=it.message?:"Не удалось открыть Bluetooth"}}){Text("Включить Bluetooth")}
     FilledTonalButton(onClick={runCatching{discover.launch(Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,120))}.onFailure{localError="Разрешите обнаружение устройств в настройках"}}){Text("Сделать телефон видимым")}
     Button(onClick={runCatching{check(adapter.isEnabled){"Включите Bluetooth"}; vm.begin(adapter,null,name)}.onFailure{localError=it.message?:"Ошибка"}}){Text("Создать новую партию · белые")}
     Text("Для первого подключения выполните сопряжение телефонов в системных настройках Bluetooth. Друг создаёт партию, затем выберите его ниже.",style=MaterialTheme.typography.bodySmall)
     OutlinedButton(onClick={context.startActivity(Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS))}){Text("Настройки Bluetooth")}
     TextButton(onClick={refresh()}){Text("Обновить список")}
     devices.forEach{(label,address) -> OutlinedButton(onClick={vm.begin(adapter,address,name)},modifier=Modifier.fillMaxWidth()){Text("$label · подключиться за чёрных")}}
    }
   }
   if(vm.busy) {LinearProgressIndicator(Modifier.fillMaxWidth()); TextButton(onClick={vm.disconnect()}){Text("Отменить подключение")}}
   if(vm.connected || vm.sequence>0) {
    Text("$name — ${vm.peer}",style=MaterialTheme.typography.titleMedium)
    Text(vm.result.ifEmpty{if(vm.position.white) "Ход белых" else "Ход чёрных"})
    ChessBoard(vm.position,!vm.hosting,vm.connected && !vm.pending && vm.result.isEmpty() && vm.position.white==vm.hosting,boardPalettes[theme],vm.lastMove){vm.move(it)}
    Row {TextButton(onClick={chatVisible=!chatVisible}){Text("Чат (${vm.messages.size})")}; TextButton(enabled=vm.connected && vm.result.isEmpty(),onClick={confirmResign=true}){Text("Сдаться")}; TextButton(onClick={confirmDisconnect=true}){Text("Отключиться")}}
    if(chatVisible) {
     vm.messages.takeLast(20).forEach{Text(it,style=MaterialTheme.typography.bodyMedium)}
     Row {listOf("😂","🤔","😎","🤝").forEach{emoji -> TextButton(enabled=vm.connected,onClick={vm.chat(emoji,true)}){Text(emoji)}}}
     OutlinedTextField(value=draft,onValueChange={draft=it.take(500)},label={Text("Сообщение другу")},modifier=Modifier.fillMaxWidth())
     Button(enabled=vm.connected && draft.isNotBlank(),onClick={vm.chat(draft); draft=""}){Text("Отправить")}
    }
   }
  }
 }
 if(confirmResign) AlertDialog(onDismissRequest={confirmResign=false},title={Text("Сдаться?")},confirmButton={TextButton(onClick={vm.resign(); confirmResign=false}){Text("Сдаться")}},dismissButton={TextButton(onClick={confirmResign=false}){Text("Продолжить")}})
 if(confirmDisconnect) AlertDialog(onDismissRequest={confirmDisconnect=false},title={Text("Закрыть соединение?")},text={Text("Автовосстановление этой партии пока не реализовано.")},confirmButton={TextButton(onClick={vm.disconnect(); confirmDisconnect=false}){Text("Отключиться")}},dismissButton={TextButton(onClick={confirmDisconnect=false}){Text("Отмена")}})
}
