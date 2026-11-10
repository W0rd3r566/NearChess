package dev.nearchess

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import java.io.Closeable
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Blocking RFCOMM operations are isolated on IO. Caller requests CONNECT permission first.
 * close() must be called on disconnect/lifecycle teardown to unblock accept/read/connect.
 * No automatic retries: session synchronization must precede resuming play.
 */
@SuppressLint("MissingPermission")
class BluetoothTransport(private val adapter: BluetoothAdapter): Closeable {
 companion object { val SERVICE: UUID=UUID.fromString("8af155ef-927c-42a9-a49c-2ea7ed82e186") }
 @Volatile private var socket: BluetoothSocket?=null
 @Volatile private var server: BluetoothServerSocket?=null
 @Volatile private var closed=false
 suspend fun host() = withContext(Dispatchers.IO) {
  check(!closed && socket==null && server==null)
  val listener=adapter.listenUsingRfcommWithServiceRecord("NearChess",SERVICE)
  server=listener
  try {
   if(closed) error("Transport closed")
   val accepted=listener.accept(120000)
   socket=accepted
   if(closed) {accepted.close(); error("Transport closed")}
  } finally {listener.close(); server=null}
 }
 suspend fun connect(address: String) = withContext(Dispatchers.IO) {
  check(!closed && socket==null)
  require(BluetoothAdapter.checkBluetoothAddress(address))
  val pending=adapter.getRemoteDevice(address).createRfcommSocketToServiceRecord(SERVICE)
  socket=pending
  try {if(closed) error("Transport closed"); pending.connect(); if(closed) error("Transport closed")}
  catch(e: Exception) {pending.close(); socket=null; throw e}
 }
 suspend fun send(message: WireMessage)=withContext(Dispatchers.IO) {Wire.write(checkNotNull(socket){"Not connected"}.outputStream,message)}
 suspend fun receive(): WireMessage=withContext(Dispatchers.IO) {Wire.read(checkNotNull(socket){"Not connected"}.inputStream)}
 override fun close() {
  closed=true
  runCatching{server?.close()}; runCatching{socket?.close()}
  server=null; socket=null
 }
}
