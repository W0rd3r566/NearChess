package dev.nearchess

/** a1=0, h8=63. Uppercase pieces are white. Immutable positions. */
data class Move(val from: Int, val to: Int, val promotion: Char = ' ') {
 fun uci() = square(from) + square(to) + if (promotion == ' ') "" else promotion.lowercase()
 companion object { fun square(s: Int) = "${('a'.code + s % 8).toChar()}${s / 8 + 1}" }
}
data class Position(
 val board: String = "RNBQKBNR" + "PPPPPPPP" + ".".repeat(32) + "pppppppp" + "rnbqkbnr",
 val white: Boolean = true, val rights: String = "KQkq", val ep: Int = -1,
 val half: Int = 0, val full: Int = 1
) {
 fun own(p: Char) = p != '.' && p.isUpperCase() == white
 fun attacked(s: Int, byWhite: Boolean): Boolean {
  val x = s % 8; val y = s / 8
  fun piece(dx: Int, dy: Int): Char {
   val a = x + dx; val b = y + dy
   return if (a in 0..7 && b in 0..7) board[b * 8 + a] else '.'
  }
  fun enemy(c: Char, type: Char) = c != '.' && c.isUpperCase() == byWhite && c.uppercaseChar() == type
  val py = if (byWhite) -1 else 1
  if (enemy(piece(-1, py), 'P') || enemy(piece(1, py), 'P')) return true
  for ((a,b) in knights) if (enemy(piece(a,b), 'N')) return true
  for ((a,b) in dirs) {
   if (enemy(piece(a,b), 'K')) return true
   for (d in 1..7) {
    val xx=x+a*d; val yy=y+b*d
    if (xx !in 0..7 || yy !in 0..7) break
    val p=board[yy*8+xx]
    if (p=='.') continue
    if (p.isUpperCase()==byWhite && (p.uppercaseChar()=='Q' || p.uppercaseChar()==if (a==0 || b==0) 'R' else 'B')) return true
    break
   }
  }
  return false
 }
 fun check(side: Boolean = white): Boolean {
  val king=board.indexOf(if(side) 'K' else 'k')
  return king < 0 || attacked(king, !side)
 }
 fun legal(): List<Move> = pseudo().filter { !play(it).check(white) }
 private fun pseudo(): List<Move> {
  val out=mutableListOf<Move>()
  for (s in 0..63) {
   val p=board[s]; if (!own(p)) continue
   val x=s%8; val y=s/8
   fun add(t: Int) {
    if (p.uppercaseChar()=='P' && t/8 in listOf(0,7)) for (q in "qrbn") out.add(Move(s,t,q)) else out.add(Move(s,t))
   }
   when (p.uppercaseChar()) {
    'P' -> {
     val step=if(white) 1 else -1; val ny=y+step
     if (ny !in 0..7) continue
     val t=s+step*8
     if (board[t]=='.') {
      add(t)
      if (y==if(white) 1 else 6) { val t2=t+step*8; if(board[t2]=='.') add(t2) }
     }
     for (dx in listOf(-1,1)) if (x+dx in 0..7) {
      val c=ny*8+x+dx
      if ((board[c]!='.' && !own(board[c]) && board[c].uppercaseChar()!='K') ||
       (c==ep && board[c]=='.' && board[c-step*8]==if(white) 'p' else 'P')) add(c)
     }
    }
    'N','K' -> {
     for ((dx,dy) in if(p.uppercaseChar()=='N') knights else dirs) {
      val a=x+dx; val b=y+dy
      if(a in 0..7 && b in 0..7) { val t=b*8+a; if(!own(board[t]) && board[t].uppercaseChar()!='K') add(t) }
     }
     if(p.uppercaseChar()=='K') {
      val base=if(white) 0 else 56
      val rook=if(white) 'R' else 'r'
      if(s==base+4 && !check()) {
       if((if(white) 'K' else 'k') in rights && board[base+7]==rook && board[base+5]=='.' && board[base+6]=='.' && !attacked(base+5,!white) && !attacked(base+6,!white)) add(base+6)
       if((if(white) 'Q' else 'q') in rights && board[base]==rook && (1..3).all{board[base+it]=='.'} && !attacked(base+3,!white) && !attacked(base+2,!white)) add(base+2)
      }
     }
    }
    else -> {
     for ((dx,dy) in dirs) {
      if(p.uppercaseChar()=='B' && (dx==0 || dy==0)) continue
      if(p.uppercaseChar()=='R' && dx!=0 && dy!=0) continue
      for (d in 1..7) {
       val a=x+dx*d; val b=y+dy*d; if(a !in 0..7 || b !in 0..7) break
       val t=b*8+a
       if(own(board[t])) break
       if(board[t].uppercaseChar()!='K') add(t)
       if(board[t]!='.') break
      }
     }
    }
   }
  }
  return out
 }
 /** Caller must supply a legal move. */
 fun play(m: Move): Position {
  val b=board.toCharArray(); val p=b[m.from]; val capture=b[m.to]!='.' || (p.uppercaseChar()=='P' && m.to==ep)
  b[m.from]='.'; b[m.to]=if(m.promotion==' ') p else if(white) m.promotion.uppercaseChar() else m.promotion.lowercaseChar()
  if(p.uppercaseChar()=='P' && m.to==ep) b[m.to+(if(white) -8 else 8)]='.'
  if(p.uppercaseChar()=='K' && kotlin.math.abs(m.to-m.from)==2) {
   val rookFrom=if(m.to>m.from) m.from+3 else m.from-4
   val rookTo=if(m.to>m.from) m.from+1 else m.from-1
   b[rookTo]=b[rookFrom]; b[rookFrom]='.'
  }
  var r=rights
  if(p=='K') r=r.replace("K","").replace("Q","")
  if(p=='k') r=r.replace("k","").replace("q","")
  for((sq,c) in listOf(0 to 'Q',7 to 'K',56 to 'q',63 to 'k')) if(m.from==sq || m.to==sq) r=r.replace(c.toString(),"")
  return Position(String(b),!white,r,if(p.uppercaseChar()=='P' && kotlin.math.abs(m.to-m.from)==16) (m.to+m.from)/2 else -1,if(p.uppercaseChar()=='P' || capture) 0 else half+1,full+if(white) 0 else 1)
 }
 fun insufficient(): Boolean {
  val pieces=board.withIndex().filter{it.value!='.' && it.value.uppercaseChar()!='K'}
  if(pieces.isEmpty()) return true
  if(pieces.size==1 && pieces[0].value.uppercaseChar() in "BN") return true
  return pieces.all{it.value.uppercaseChar()=='B'} && pieces.map{(it.index%8+it.index/8)%2}.distinct().size==1
 }
 fun key(): String {
  val effectiveEp=if(ep>=0 && legal().any{it.to==ep && board[it.from].uppercaseChar()=='P'}) ep else -1
  return "$board:$white:$rights:$effectiveEp"
 }
 fun fen(): String {
  val rows=(7 downTo 0).map { y -> buildString { var n=0; for(x in 0..7) { val p=board[y*8+x]; if(p=='.') n++ else { if(n>0) append(n); n=0; append(p) } }; if(n>0) append(n) } }
  return rows.joinToString("/")+" ${if(white) "w" else "b"} ${rights.ifEmpty{"-"}} ${if(ep<0) "-" else Move.square(ep)} $half $full"
 }
 companion object {
  val knights=listOf(1 to 2,2 to 1,-1 to 2,-2 to 1,1 to -2,2 to -1,-1 to -2,-2 to -1)
  val dirs=listOf(1 to 0,-1 to 0,0 to 1,0 to -1,1 to 1,1 to -1,-1 to 1,-1 to -1)
  fun fromFen(fen: String): Position {
   val f=fen.trim().split(Regex("\\s+")); require(f.size==6)
   val rows=f[0].split('/'); require(rows.size==8)
   val b=CharArray(64){'.'}
   rows.forEachIndexed { row,text -> var x=0; for(c in text) { if(c in '1'..'8') x+=c.digitToInt() else { require(c in "prnbqkPRNBQK" && x<8); b[(7-row)*8+x++]=c } }; require(x==8) }
   require(b.count{it=='K'}==1 && b.count{it=='k'}==1)
   require(f[1] in listOf("w","b")); require(f[2]=="-" || (f[2].all{it in "KQkq"} && f[2].toSet().size==f[2].length))
   val ep=if(f[3]=="-") -1 else { require(f[3].length==2 && f[3][0] in 'a'..'h' && f[3][1] in "36"); (f[3][1]-'1')*8+(f[3][0]-'a') }
   val half=f[4].toInt(); val full=f[5].toInt(); require(half>=0 && full>=1)
   return Position(String(b),f[1]=="w",f[2].replace("-",""),ep,half,full)
  }
 }
}
fun Position.san(m: Move): String {
 val p=board[m.from]; val moves=legal(); require(m in moves)
 val capture=board[m.to]!='.' || (p.uppercaseChar()=='P' && m.to==ep)
 val body=if(p.uppercaseChar()=='K' && kotlin.math.abs(m.to-m.from)==2) { if(m.to>m.from) "O-O" else "O-O-O" } else buildString {
  if(p.uppercaseChar()!='P') {
   append(p.uppercaseChar())
   val others=moves.filter{it.from!=m.from && it.to==m.to && board[it.from]==p}
   if(others.isNotEmpty()) {
    if(others.none{it.from%8==m.from%8}) append(('a'.code+m.from%8).toChar())
    else if(others.none{it.from/8==m.from/8}) append(m.from/8+1)
    else append(Move.square(m.from))
   }
  } else if(capture) append(('a'.code+m.from%8).toChar())
  if(capture) append('x'); append(Move.square(m.to))
  if(m.promotion!=' ') append("=${m.promotion.uppercaseChar()}")
 }
 val next=play(m)
 return body+if(next.check()) { if(next.legal().isEmpty()) "#" else "+" } else ""
}
