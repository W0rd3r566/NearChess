package dev.nearchess

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class BotPacingTest {
 @Test fun fastSearchWaitsRemainingTime()=runBlocking {
  var clock=0L;var waited=0L
  val expected=Move(12,28)
  val result=pacedBotReply(1400,nowNanos={clock},wait={waited=it;clock+=it*1000000}){clock=100000000;expected}
  assertEquals(expected,result);assertEquals(1300L,waited);assertEquals(1400000000L,clock)
 }
 @Test fun slowSearchAddsNoDelay()=runBlocking {
  var clock=0L;var waited=false
  pacedBotReply(1400,nowNanos={clock},wait={waited=true}){clock=2000000000;Move(12,28)}
  assertFalse(waited)
 }
 @Test fun terminalPositionDoesNotWait()=runBlocking {
  var waited=false
  assertNull(pacedBotReply(1400,nowNanos={0},wait={waited=true}){null})
  assertFalse(waited)
 }
 @Test fun cancellationDuringWaitDoesNotReturnMove()=runBlocking {
  var returned=false
  try {pacedBotReply(1400,nowNanos={0},wait={throw CancellationException("new game")}){Move(12,28)};returned=true}
  catch(expected:CancellationException){}
  assertFalse(returned)
 }
}
