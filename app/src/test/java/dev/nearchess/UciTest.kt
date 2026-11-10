package dev.nearchess
import org.junit.Test
import org.junit.Assert.*
class UciTest {
 @Test fun coordinates(){assertEquals(Move(12,28),Uci.parse("e2e4")); assertEquals(Move(48,56,'q'),Uci.parse("a7a8q"))}
 @Test fun allSquaresRoundTrip(){for(from in 0..63) for(to in 0..63) if(from!=to) {val m=Move(from,to); assertEquals(m,Uci.parse(m.uci()))}}
 @Test(expected=IllegalArgumentException::class) fun invalidFile(){Uci.parse("i2e4")}
 @Test(expected=IllegalArgumentException::class) fun invalidPromotion(){Uci.parse("a7a8k")}
 @Test(expected=IllegalArgumentException::class) fun invalidLength(){Uci.parse("e2")}
 @Test(expected=IllegalArgumentException::class) fun nullMove(){Uci.parse("e2e2")}
}
