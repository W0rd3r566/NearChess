package dev.nearchess

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class ArchivedGame(val id:String,val mode:String,val opponent:String,val result:String,val finished:Long,val duration:Long,val level:Int,val positions:List<Position>,val notation:List<String>)
class GameArchive(context:Context) {
 private val prefs=context.getSharedPreferences("archive",0)
 @Synchronized fun all():List<ArchivedGame> = runCatching{
  val a=JSONArray(prefs.getString("games","[]"))
  (0 until a.length()).map{ i->val o=a.getJSONObject(i);val h=o.getJSONArray("positions");val n=o.getJSONArray("notation");ArchivedGame(o.getString("id"),o.getString("mode"),o.getString("opponent"),o.getString("result"),o.getLong("finished"),o.getLong("duration"),o.optInt("level",-1),(0 until h.length()).map{Position.fromFen(h.getString(it))},(0 until n.length()).map{n.getString(it)})}
 }.getOrDefault(emptyList())
 @Synchronized fun record(game:ArchivedGame) {
  val list=(listOf(game)+all().filter{it.id!=game.id}).take(200)
  val a=JSONArray()
  list.forEach{g->a.put(JSONObject().put("id",g.id).put("mode",g.mode).put("opponent",g.opponent).put("result",g.result).put("finished",g.finished).put("duration",g.duration).put("level",g.level).put("positions",JSONArray(g.positions.map{it.fen()})).put("notation",JSONArray(g.notation)))}
  prefs.edit().putString("games",a.toString()).apply()
 }
}
