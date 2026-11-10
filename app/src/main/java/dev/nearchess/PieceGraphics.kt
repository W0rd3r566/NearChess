package dev.nearchess

import android.content.Context
import android.graphics.Canvas
import android.graphics.Picture
import android.graphics.RectF
import com.caverock.androidsvg.SVG

/** Original SVG geometry, rendered as vector Picture commands, not Unicode or bitmaps. */
class PieceGraphics(context: Context, set: String) {
 private val pictures: Map<Char,Picture> = "KQRBNPkqrbnp".associateWith { p ->
  val side=if(p.isUpperCase()) "w" else "b"
  context.assets.open("piece/$set/$side${p.uppercaseChar()}.svg").use { SVG.getFromInputStream(it).renderToPicture() }
 }
 fun draw(canvas: Canvas,piece:Char,x:Float,y:Float,size:Float) {
  val picture=pictures[piece]?:return
  val ratio=picture.width.toFloat()/picture.height.coerceAtLeast(1)
  val w=if(ratio>1) size else size*ratio
  val h=if(ratio>1) size/ratio else size
  canvas.drawPicture(picture,RectF(x-w/2,y-h/2,x+w/2,y+h/2))
 }
}
