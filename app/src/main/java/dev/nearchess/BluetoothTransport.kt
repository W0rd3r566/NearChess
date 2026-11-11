package dev.nearchess

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import java.io.Closeable
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.CancellationException

@SuppressLint("MissingPermission")
class BluetoothTransport(private val adapter:BluetoothAdapter):Closeable {
 companion object {val SERVICE:UUID=UUID.fromString("8af155ef-927c-42a9-a49c-2ea7ed82e186")}
 @Volatile private var socket:BluetoothSocket?=null
 @Volatile private var server:BluetoothServerSocket?=null
 @Volatile private var closed=false
 suspend fun host()=withContext(Dispatchers.IO){
  check(!closed && socket==null && server==null)
  check(adapter.isEnabled){"Включите Bluetooth перед созданием партии"}
  val listener=adapter.listenUsingRfcommWithServiceRecord("NearChess",SERVICE)
  server=listener
  try{
   if(closed)error("Подключение отменено")
   val accepted=listener.accept(120000)
   socket=accepted
   if(closed){accepted.close();error("Подключение отменено")}
  }catch(e:IOException){throw IOException("Не удалось дождаться подключения. Создайте партию снова и подключите второй телефон в течение двух минут.",e)}
  finally{runCatching{listener.close()};server=null}
 }
 suspend fun connect(address:String)=coroutineScope {
  check(!closed && socket==null)
  require(BluetoothAdapter.checkBluetoothAddress(address))
  check(adapter.isEnabled){"Включите Bluetooth перед подключением"}
  // Discovery interferes with RFCOMM. On Android 12+ this is best-effort:
  // cancelDiscovery requires SCAN permission, which this paired-device UI does not request.
  runCatching{adapter.cancelDiscovery()}
  val pending=adapter.getRemoteDevice(address).createRfcommSocketToServiceRecord(SERVICE)
  socket=pending
  val watchdog=launch(Dispatchers.IO){delay(20000);runCatching{pending.close()}}
  try{
   withContext(Dispatchers.IO){
    if(closed)error("Подключение отменено")
    try{pending.connect()}
    catch(e:IOException){throw IOException("Не удалось соединиться с NearChess. На другом телефоне сначала нажмите «Создать партию» и оставьте экран ожидания открытым. Проверьте сопряжение и одинаковую версию приложения.",e)}
    if(closed)error("Подключение отменено")
   }
  }catch(e:Exception){runCatching{pending.close()};socket=null;throw e}
  finally{watchdog.cancel()}
 }
 suspend fun send(message:WireMessage)=withContext(Dispatchers.IO){Wire.write(checkNotNull(socket){"Нет Bluetooth-соединения"}.outputStream,message)}
 suspend fun receive():WireMessage=withContext(Dispatchers.IO){
  try{Wire.read(checkNotNull(socket){"Нет Bluetooth-соединения"}.inputStream)}
  catch(e:IOException){throw IOException("Второй телефон закрыл соединение или связь потеряна. Проверьте, что NearChess остаётся открытым на обоих устройствах.",e)}
 }
 override fun close(){closed=true;runCatching{server?.close()};runCatching{socket?.close()};server=null;socket=null}
}
