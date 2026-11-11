package dev.nearchess

import org.junit.Assert.*
import org.junit.Test

class PlayerPanelTest {
 private fun play(p:Position,uci:String):Position = p.play(p.legal().single{it.uci()==uci})
 @Test fun initialMaterialIsEqual(){
  assertEquals(39,materialFor(Position(),true))
  assertEquals(39,materialFor(Position(),false))
  assertTrue(capturesBy(listOf(Position()),true).isEmpty())
 }
 @Test fun captureBelongsToMovingSide(){
  val p=Position.fromFen("7k/8/8/3p4/4P3/8/8/K7 w - - 0 1")
  val after=play(p,"e4d5")
  assertEquals(listOf(PieceType.PAWN),capturesBy(listOf(p,after),true))
  assertTrue(capturesBy(listOf(p,after),false).isEmpty())
 }
 @Test fun enPassantCountsPawn(){
  val p=Position.fromFen("7k/8/8/3pP3/8/8/8/K7 w - d6 0 1")
  assertEquals(listOf(PieceType.PAWN),capturesBy(listOf(p,play(p,"e5d6")),true))
 }
 @Test fun promotionIsNotCapturedPawn(){
  val p=Position.fromFen("7k/P7/8/8/8/8/8/K7 w - - 0 1")
  val after=play(p,"a7a8q")
  assertTrue(capturesBy(listOf(p,after),true).isEmpty())
  assertTrue(capturesBy(listOf(p,after),false).isEmpty())
  assertEquals(9,materialFor(after,true))
 }
 @Test fun promotionCaptureCountsOnlyEnemy(){
  val p=Position.fromFen("1r5k/P7/8/8/8/8/8/K7 w - - 0 1")
  assertEquals(listOf(PieceType.ROOK),capturesBy(listOf(p,play(p,"a7b8q")),true))
 }
 @Test fun castleDoesNotCapture(){
  val p=Position.fromFen("7k/8/8/8/8/8/8/4K2R w K - 0 1")
  assertTrue(capturesBy(listOf(p,play(p,"e1g1")),true).isEmpty())
 }
 @Test fun customFenDoesNotInventCaptures(){
  val p=Position.fromFen("7k/8/8/8/8/8/8/K7 w - - 0 1")
  assertTrue(capturesBy(listOf(p),true).isEmpty())
  assertTrue(capturesBy(listOf(p),false).isEmpty())
 }
}
