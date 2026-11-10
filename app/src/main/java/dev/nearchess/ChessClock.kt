package dev.nearchess

/** Fischer clock. Caller supplies monotonic milliseconds, never wall-clock time.
 * Not yet connected to the UI or Bluetooth protocol.
 */
class ChessClock(initialMillis: Long, val incrementMillis: Long = 0) {
 init {require(initialMillis>0 && incrementMillis>=0)}
 var whiteMillis=initialMillis; private set
 var blackMillis=initialMillis; private set
 var whiteTurn=true; private set
 var running=false; private set
 var flaggedWhite: Boolean?=null; private set
 private var anchor=0L
 fun start(now: Long) {require(now>=0); if(!running && flaggedWhite==null){anchor=now; running=true}}
 fun update(now: Long) {
  require(now>=0)
  if(!running) return
  require(now>=anchor){"Monotonic clock moved backwards"}
  val elapsed=now-anchor; anchor=now
  if(whiteTurn) whiteMillis=(whiteMillis-elapsed).coerceAtLeast(0) else blackMillis=(blackMillis-elapsed).coerceAtLeast(0)
  if((if(whiteTurn) whiteMillis else blackMillis)==0L){flaggedWhite=whiteTurn; running=false}
 }
 /** Invoke only after a legal move, for the side that actually moved. */
 fun completeMove(sideWhite: Boolean, now: Long): Boolean {
  if(!running || sideWhite!=whiteTurn) return false
  update(now)
  if(flaggedWhite!=null) return false
  if(whiteTurn) whiteMillis=Math.addExact(whiteMillis,incrementMillis) else blackMillis=Math.addExact(blackMillis,incrementMillis)
  whiteTurn=!whiteTurn
  return true
 }
 fun pause(now: Long){update(now); running=false}
}
