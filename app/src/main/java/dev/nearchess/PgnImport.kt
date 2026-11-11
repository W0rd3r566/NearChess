package dev.nearchess

/** Imports one standard-chess mainline; variations are skipped, not merged into the game. */
data class ImportedPgn(val tags:Map<String,String>,val positions:List<Position>,val notation:List<String>,val result:String)
object PgnImport {
 fun parse(text:String):ImportedPgn {
  require(text.length<=2_000_000){"PGN слишком большой (максимум 2 МБ текста)"}
  val tags=linkedMapOf<String,String>()
  val body=StringBuilder()
  val tag=Regex("^\\[([A-Za-z0-9_]+)\\s+\"((?:\\\\.|[^\"\\\\])*)\"\\s*]$")
  var movesStarted=false
  for(line in text.lineSequence()) {
   val trimmed=line.trim().removePrefix("\uFEFF")
   if(trimmed.startsWith("[")) {
    require(!movesStarted){"Импортируйте одну партию за раз"}
    val match=tag.matchEntire(trimmed)?:error("Некорректный тег PGN")
    tags[match.groupValues[1]]=match.groupValues[2].replace("\\\"","\"").replace("\\\\","\\")
   }else {if(trimmed.isNotEmpty()&&!trimmed.startsWith("%"))movesStarted=true; if(!trimmed.startsWith("%"))body.append(line).append('\n')}
  }
  require(tags["Variant"]==null || tags["Variant"] in listOf("Standard","Chess")){"Поддерживаются только обычные шахматы"}
  val clean=StringBuilder();var variation=0;var comment=false;var semicolon=false
  for(c in body) {
   if(semicolon){if(c=='\n'){semicolon=false;clean.append(' ')};continue}
   if(comment){if(c=='}'){comment=false;clean.append(' ')};continue}
   when(c){
    ';'->semicolon=true
    '{'->comment=true
    '('->{variation++;clean.append(' ')}
    ')'->{require(variation>0){"Лишняя скобка варианта"};variation--;clean.append(' ')}
    else->if(variation==0)clean.append(c)
   }
  }
  require(!comment&&variation==0){"Незакрытый комментарий или вариант"}
  require(tags["SetUp"]!="1" || tags.containsKey("FEN")){"Отсутствует FEN"}
  var p=tags["FEN"]?.let{Position.fromFen(it)}?:Position()
  val positions=mutableListOf(p);val notation=mutableListOf<String>()
  var ending:String?=null
  val tokens=clean.toString().replace(Regex("\\$\\d+")," ").split(Regex("\\s+"))
  fun normal(s:String)=s.replace('0','O').replace(Regex("[!?+#]+$"),"")
  for(raw in tokens){
   val token=raw.replace(Regex("^\\d+\\.(?:\\.\\.)?"),"")
   if(token.isBlank()||token=="..."||token=="e.p.")continue
   require(ending==null){"Лишние ходы после результата; возможны несколько партий"}
   if(token in listOf("1-0","0-1","1/2-1/2","*")){ending=token;continue}
   require(notation.size<2000){"Слишком длинная партия"}
   val legal=p.legal()
   val candidates=legal.filter{normal(p.san(it))==normal(token)}
   require(candidates.size==1){"Нелегальный или неоднозначный ход ${notation.size+1}: $token"}
   val move=candidates.single()
   if(token.endsWith("#")){val next=p.play(move);require(next.check()&&next.legal().isEmpty()){"Ложная отметка мата: $token"}}
   notation.add(p.san(move));p=p.play(move);positions.add(p)
  }
  val result=ending?:tags["Result"]?:"*"
  require(result in listOf("1-0","0-1","1/2-1/2","*")){"Некорректный результат"}
  require(tags["Result"]==null||tags["Result"]==result){"Результат тегов не совпадает с записью ходов"}
  return ImportedPgn(tags,positions,notation,result)
 }
}
