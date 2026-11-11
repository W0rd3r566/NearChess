package dev.nearchess

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@SuppressLint("MissingPermission")
@Composable internal fun BluetoothLobby(vm:NearbyModel,name:String){
 val context=LocalContext.current
 val adapter=remember{context.getSystemService(BluetoothManager::class.java)?.adapter}
 var devices by remember{mutableStateOf(emptyList<Pair<String,String>>())}
 var error by remember{mutableStateOf("")}
 var pending by remember{mutableStateOf<String?>(null)}
 fun permitted()=Build.VERSION.SDK_INT<31 || context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)==PackageManager.PERMISSION_GRANTED
 fun refresh(){if(permitted())runCatching{devices=adapter?.bondedDevices?.map{(it.name?:"Телефон") to it.address}?:emptyList()}.onFailure{error="Разрешите доступ к Bluetooth"}}
 fun startPending(){val role=pending?:return;pending=null;runCatching{val a=requireNotNull(adapter){"Bluetooth не поддерживается"};check(a.isEnabled){"Включите Bluetooth"};vm.begin(a,if(role=="HOST")null else role,name)}.onFailure{error=it.message?:"Ошибка подключения"}}
 val enable=rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()){if(permitted() && adapter?.isEnabled==true)startPending()else{pending=null;error="Bluetooth выключен"};refresh()}
 fun enabledOrRequest(){if(adapter?.isEnabled==true)startPending()else runCatching{enable.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))}.onFailure{pending=null;error="Откройте настройки Bluetooth"}}
 val permission=rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){if(permitted()){refresh();enabledOrRequest()}else{pending=null;error="Без разрешения подключение невозможно"}}
 fun request(role:String){error="";pending=role;if(adapter==null){error="Bluetooth не поддерживается";pending=null}else if(!permitted()){permission.launch(arrayOf(Manifest.permission.BLUETOOTH_CONNECT))}else enabledOrRequest()}
 val settings=rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()){refresh()}
 LaunchedEffect(Unit){refresh()}
 Column(verticalArrangement=Arrangement.spacedBy(12.dp)){
  Text("Первый телефон создаёт партию. Второй подключается к нему.",style=MaterialTheme.typography.bodyMedium)
  Button(enabled=adapter!=null&&!vm.busy,onClick={request("HOST")},modifier=Modifier.fillMaxWidth().heightIn(min=52.dp)){Text("Создать партию")}
  OutlinedButton(onClick={settings.launch(Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS))},modifier=Modifier.fillMaxWidth()){Text("Сопряжение телефонов в настройках")}
  Text("Подключиться к другу",style=MaterialTheme.typography.titleMedium)
  TextButton(onClick={refresh()}){Text("Обновить сопряжённые устройства")}
  if(devices.isEmpty())Text("Список пуст. Сначала выполните сопряжение в настройках обоих телефонов. Затем вернитесь сюда.",style=MaterialTheme.typography.bodySmall)
  devices.forEach{(label,address)->OutlinedButton(enabled=!vm.busy,onClick={request(address)},modifier=Modifier.fillMaxWidth()){Text(label)}}
  if(error.isNotEmpty())Text(error,color=MaterialTheme.colorScheme.error)
 }
}
