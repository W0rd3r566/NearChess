package dev.nearchess
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import org.junit.Test
import org.junit.Assert.*
class WireTest {
 @Test fun unicodeRoundTrip() {
  val message=WireMessage(Wire.CHAT,0,"Привет ♟️")
  val out=ByteArrayOutputStream(); Wire.write(out,message)
  assertEquals(message,Wire.read(ByteArrayInputStream(out.toByteArray())))
 }
 @Test(expected=IllegalArgumentException::class) fun oversizedPayloadRejected() {Wire.write(ByteArrayOutputStream(),WireMessage(Wire.CHAT,0,"x".repeat(Wire.MAX_BYTES+1)))}
 @Test fun wrongTurnAndDuplicateRejected() {
  val session=MatchSession()
  assertFalse(session.acceptMove(0,false,"e7e5"))
  assertTrue(session.acceptMove(0,true,"e2e4"))
  assertFalse(session.acceptMove(0,true,"e2e4"))
  assertFalse(session.acceptMove(1,false,"e7e4"))
  assertTrue(session.acceptMove(1,false,"e7e5"))
 }
 @Test fun resignationStopsMoves() {val s=MatchSession(); s.resign(true); assertEquals("0-1",s.result); assertFalse(s.acceptMove(0,true,"e2e4"))}
}
