package dev.nearchess
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.delay

enum class BotLevel(val title:String,val depth:Int,val budget:Int,val positional:Boolean){
 NOVICE("Новичок",1,300,false), AMATEUR("Любитель",2,1500,false),
 STUDENT("Ученик",3,6000,true), TACTICIAN("Тактик",4,18000,true),
 STRATEGIST("Стратег",5,45000,true), EXPERT("Эксперт",6,100000,true),
 MASTER("Мастер",7,220000,true), MAXIMUM("Максимум",9,450000,true)
}
data class BotSearchResult(val move:Move?,val completedDepth:Int,val nodes:Int)
internal suspend fun chooseTrainingMove(p:Position,level:BotLevel=BotLevel.STUDENT):Move?{
 val start=System.nanoTime()
 val result=searchTraining(p,level)
 val target=850L+((p.board.hashCode().toLong() and 0x7fffffff)%550)
 val elapsed=(System.nanoTime()-start)/1000000
 if(result.move!=null && elapsed<target)delay(target-elapsed)
 return result.move
}
internal suspend fun searchTraining(position:Position,level:BotLevel):BotSearchResult {
 val ctx=currentCoroutineContext();var nodes=0
 val start=System.nanoTime();val timeout=if(level==BotLevel.MAXIMUM)8000000000L else 3000000000L
 fun value(c:Char)=when(c.uppercaseChar()){'P'->100;'N'->320;'B'->330;'R'->500;'Q'->900;else->0}
 fun evaluate(p:Position):Int=p.board.withIndex().sumOf{(sq,c)->
  if(c=='.')0 else {
   val advance=if(c.isUpperCase())sq/8 else 7-sq/8
   val center=7-kotlin.math.abs(sq%8*2-7)-kotlin.math.abs(sq/8*2-7)
   val bonus=if(!level.positional)0 else if(c.uppercaseChar()=='P')advance*7 else if(c.uppercaseChar() in "NB")center*4 else 0
   (value(c)+bonus)*if(c.isUpperCase()==p.white)1 else -1
  }
 }
 class BudgetReached:RuntimeException()
 fun search(p:Position,depth:Int,alphaIn:Int,beta:Int):Int {
  ctx.ensureActive();nodes++
  if(nodes>level.budget || System.nanoTime()-start>timeout)throw BudgetReached()
  val moves=p.legal()
  if(moves.isEmpty())return if(p.check())-100000-depth else 0
  if(p.insufficient() || p.half>=150)return 0
  if(depth==0)return evaluate(p)
  var alpha=alphaIn
  for(m in moves.sortedByDescending{value(p.board[it.to])*10-value(p.board[it.from])}){
   val score=-search(p.play(m),depth-1,-beta,-alpha)
   if(score>=beta)return beta
   alpha=maxOf(alpha,score)
  }
  return alpha
 }
 val legal=position.legal();if(legal.isEmpty())return BotSearchResult(null,0,0)
 var best=legal.first();var completed=0
 for(depth in 1..level.depth){
  var candidate=best;var score=Int.MIN_VALUE
  val scored=mutableListOf<Pair<Move,Int>>()
  try {
   for(m in legal.sortedByDescending{if(it==best)100000 else value(position.board[it.to])}){
    val v=-search(position.play(m),depth-1,-200000,200000)
    scored.add(m to v)
    if(v>score){score=v;candidate=m}
   }
   if(level==BotLevel.NOVICE || level==BotLevel.AMATEUR){
    // Controlled suboptimality among evaluated legal moves; never arbitrary illegal moves.
    val allowance=if(level==BotLevel.NOVICE)220 else 90
    val choices=scored.filter{it.second>=score-allowance}.sortedByDescending{it.second}
    if(choices.isNotEmpty()){
     val seed=position.board.hashCode().toLong() and 0x7fffffff
     candidate=choices[(seed%minOf(choices.size,if(level==BotLevel.NOVICE)5 else 3)).toInt()].first
    }
   }
   best=candidate;completed=depth
  }catch(e:BudgetReached){break}
 }
 return BotSearchResult(best,completed,nodes)
}
