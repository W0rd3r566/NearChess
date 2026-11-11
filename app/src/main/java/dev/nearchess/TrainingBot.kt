package dev.nearchess
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
internal suspend fun chooseTrainingMove(position:Position):Move? {
 val ctx=currentCoroutineContext()
 fun value(c:Char)=when(c.uppercaseChar()){'P'->100;'N'->320;'B'->330;'R'->500;'Q'->900;else->0}
 fun search(p:Position,depth:Int,alphaIn:Int,beta:Int):Int {
  ctx.ensureActive()
  val legal=p.legal()
  if(legal.isEmpty())return if(p.check())-100000-depth else 0
  if(p.insufficient())return 0
  if(depth==0)return p.board.sumOf{if(it=='.')0 else value(it)*if(it.isUpperCase()==p.white)1 else -1}
  var alpha=alphaIn
  for(m in legal.sortedByDescending{value(p.board[it.to])}) {
   val score=-search(p.play(m),depth-1,-beta,-alpha)
   if(score>=beta)return beta
   if(score>alpha)alpha=score
  }
  return alpha
 }
 return position.legal().maxByOrNull{-search(position.play(it),2,-200000,200000)}
}
