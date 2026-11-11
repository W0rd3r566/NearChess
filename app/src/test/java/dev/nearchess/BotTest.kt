package dev.nearchess
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*
class BotTest {
 @Test fun everyLevelReturnsLegalMove()=runBlocking {
  val p=Position.fromFen("7k/5Q2/6K1/8/8/8/8/8 w - - 0 1")
  for(level in BotLevel.entries){val r=searchTraining(p,level);assertTrue(r.move in p.legal())}
 }
 @Test fun noMoveAfterMate()=runBlocking {
  val p=Position.fromFen("7k/6Q1/6K1/8/8/8/8/8 b - - 0 1")
  for(level in BotLevel.entries)assertNull(searchTraining(p,level).move)
 }
 @Test fun configurationsDiffer(){assertEquals(8,BotLevel.entries.map{it.depth to it.budget}.distinct().size)}
}
