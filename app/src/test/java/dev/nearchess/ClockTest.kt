package dev.nearchess
import org.junit.Test
import org.junit.Assert.*
class ClockTest {
 @Test fun incrementAndTurn() {val c=ChessClock(180000,2000); c.start(100); assertTrue(c.completeMove(true,1100)); assertEquals(181000L,c.whiteMillis); assertFalse(c.whiteTurn); c.update(3100); assertEquals(178000L,c.blackMillis)}
 @Test fun timeoutCannotBeRescuedByIncrement() {val c=ChessClock(1000,2000); c.start(0); assertFalse(c.completeMove(true,1000)); assertEquals(true,c.flaggedWhite); assertEquals(0L,c.whiteMillis)}
 @Test fun pauseDoesNotChargeTime() {val c=ChessClock(10000); c.start(0); c.pause(1000); c.start(100000); c.update(101000); assertEquals(8000L,c.whiteMillis)}
 @Test fun wrongSideCannotMoveClock() {val c=ChessClock(10000); c.start(0); assertFalse(c.completeMove(false,100)); assertTrue(c.whiteTurn)}
 @Test fun sessionMateStopsFurtherMoves() {
  val s=MatchSession(); listOf("f2f3","e7e5","g2g4","d8h4").forEachIndexed{i,m -> assertTrue(s.acceptMove(i,i%2==0,m))}
  assertEquals("0-1",s.result); assertFalse(s.acceptMove(4,true,"a2a3"))
 }
}
