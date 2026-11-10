package dev.nearchess

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction

/** Bounded binary framing shared by Bluetooth and protocol tests. */
data class WireMessage(val type: Int, val sequence: Int, val payload: String)
object Wire {
 const val VERSION=1
 const val HELLO=1
 const val MOVE=2
 const val CHAT=3
 const val REACTION=4
 const val STATE=5
 const val RESIGN=6
 const val MAX_BYTES=16384
 fun write(output: OutputStream, message: WireMessage) {
  require(message.type in HELLO..RESIGN && message.sequence>=0)
  val bytes=message.payload.toByteArray(Charsets.UTF_8)
  require(bytes.size<=MAX_BYTES)
  synchronized(output) {
   val out=DataOutputStream(output)
   out.writeInt(0x4E434853); out.writeByte(VERSION); out.writeByte(message.type)
   out.writeInt(message.sequence); out.writeInt(bytes.size); out.write(bytes); out.flush()
  }
 }
 fun read(input: InputStream): WireMessage {
  val data=DataInputStream(input)
  require(data.readInt()==0x4E434853){"Invalid NearChess header"}
  require(data.readUnsignedByte()==VERSION){"Incompatible protocol version"}
  val type=data.readUnsignedByte(); require(type in HELLO..RESIGN)
  val seq=data.readInt(); require(seq>=0)
  val size=data.readInt(); require(size in 0..MAX_BYTES)
  val bytes=ByteArray(size); data.readFully(bytes)
  val decoder=Charsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT)
  return WireMessage(type,seq,decoder.decode(ByteBuffer.wrap(bytes)).toString())
 }
}

/** Host-side state; transport callbacks must serialize access to this object. */
class MatchSession(initial: Position=Position()) {
 var position=initial; private set
 var sequence=0; private set
 var result=""; private set
 private val positions=mutableListOf(initial.key())
 val moves=mutableListOf<String>()
 @Synchronized fun acceptMove(expectedSequence: Int, senderWhite: Boolean, uci: String): Boolean {
  if(result.isNotEmpty() || expectedSequence!=sequence || senderWhite!=position.white) return false
  val move=position.legal().singleOrNull{it.uci()==uci}?:return false
  moves.add(position.san(move)); position=position.play(move); sequence++
  positions.add(position.key())
  result=when {
   position.legal().isEmpty() -> if(position.check()) if(position.white) "0-1" else "1-0" else "1/2-1/2"
   position.insufficient() || position.half>=150 || positions.count{it==position.key()}>=5 -> "1/2-1/2"
   else -> ""
  }
  return true
 }
 @Synchronized fun resign(white: Boolean) { if(result.isEmpty()) result=if(white) "0-1" else "1-0" }
}
