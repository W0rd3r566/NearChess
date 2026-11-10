package dev.nearchess
import org.junit.Assert.*
import org.junit.Test
class ChessTest {
 private fun perft(p: Position,n: Int): Long = if(n==0) 1 else p.legal().sumOf{perft(p.play(it),n-1)}
 @Test fun initialPerft() { assertEquals(20L,perft(Position(),1)); assertEquals(400L,perft(Position(),2)); assertEquals(8902L,perft(Position(),3)) }
 @Test fun fenRoundTrip() { val p=Position(); assertEquals(p,Position.fromFen(p.fen())) }
 @Test fun foolsMate() {
  var p=Position()
  for(u in listOf("f2f3","e7e5","g2g4","d8h4")) p=p.play(p.legal().single{it.uci()==u})
  assertTrue(p.check()); assertTrue(p.legal().isEmpty())
 }
 @Test fun enPassantCannotExposeKing() {
  val p=Position.fromFen("k3r3/8/8/3pP3/8/8/8/4K3 w - d6 0 1")
  assertFalse(p.legal().any{it.uci()=="e5d6"})
 }
 @Test fun promotions() { val p=Position.fromFen("7k/P7/8/8/8/8/8/7K w - - 0 1"); assertEquals(4,p.legal().count{it.from==48}) }
 @Test fun castleThroughCheckForbidden() { val p=Position.fromFen("k4r2/8/8/8/8/8/8/4K2R w K - 0 1"); assertFalse(p.legal().any{it.uci()=="e1g1"}) }
 @Test fun stalemate() { val p=Position.fromFen("7k/5Q2/6K1/8/8/8/8/8 b - - 0 1"); assertFalse(p.check()); assertTrue(p.legal().isEmpty()) }
}
