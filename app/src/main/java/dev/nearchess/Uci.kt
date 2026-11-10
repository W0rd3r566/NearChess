package dev.nearchess

object Uci {
 fun parse(text: String): Move {
  require(text.length==4 || text.length==5){"UCI move must contain 4 or 5 characters"}
  fun square(offset: Int): Int {
   val file=text[offset]; val rank=text[offset+1]
   require(file in 'a'..'h' && rank in '1'..'8'){"Invalid square"}
   return (rank-'1')*8+(file-'a')
  }
  val from=square(0); val to=square(2)
  require(from!=to){"Source and destination must differ"}
  val promotion=if(text.length==5) text[4] else ' '
  require(promotion==' ' || promotion in "qrbn"){"Invalid promotion piece"}
  return Move(from,to,promotion)
 }
}
