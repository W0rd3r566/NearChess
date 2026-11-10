package dev.nearchess

import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme

private val NearPrimary=Color(0xFF71D8BC)
private val NearDark=Color(0xFF121A1C)
private val NearLight=Color(0xFFEDF3EF)

@Composable fun NearTheme(content: @Composable () -> Unit) {
 val context=LocalContext.current
 val scheme=if(Build.VERSION.SDK_INT>=31) dynamicDarkColorScheme(context) else darkColorScheme(
  primary=NearPrimary,onPrimary=Color(0xFF00382B),primaryContainer=Color(0xFF00513F),onPrimaryContainer=Color(0xFF8EF6D6),
  secondary=Color(0xFFB5CCC3),onSecondary=Color(0xFF20332D),secondaryContainer=Color(0xFF374A43),onSecondaryContainer=NearLight,
  background=NearDark,onBackground=NearLight,surface=NearDark,onSurface=NearLight,surfaceVariant=Color(0xFF414944),onSurfaceVariant=Color(0xFFC0CCC6),outline=Color(0xFF89958F),outlineVariant=Color(0xFF414944)
 )
 MaterialTheme(colorScheme=scheme,typography=Typography(),shapes=Shapes(),content=content)
}

@Composable fun NearLogo(modifier: Modifier=Modifier) {
 Canvas(modifier.size(82.dp)) {
  val primary=MaterialTheme.colorScheme.primary
  val on=MaterialTheme.colorScheme.onSurface
  val w=size.width; val h=size.height
  drawRoundRect(MaterialTheme.colorScheme.surfaceContainerHigh,style=androidx.compose.ui.graphics.drawscope.Fill)
  val rook=Path().apply {
   moveTo(w*.23f,h*.28f); lineTo(w*.34f,h*.28f); lineTo(w*.34f,h*.20f); lineTo(w*.44f,h*.20f); lineTo(w*.44f,h*.28f)
   lineTo(w*.56f,h*.28f); lineTo(w*.56f,h*.20f); lineTo(w*.66f,h*.20f); lineTo(w*.66f,h*.28f); lineTo(w*.77f,h*.28f)
   lineTo(w*.72f,h*.39f); lineTo(w*.68f,h*.73f); lineTo(w*.82f,h*.79f); lineTo(w*.82f,h*.86f); lineTo(w*.18f,h*.86f); lineTo(w*.18f,h*.79f); lineTo(w*.32f,h*.73f); lineTo(w*.28f,h*.39f); close()
  }
  drawPath(rook,primary)
  val n=Path().apply {moveTo(w*.39f,h*.43f);lineTo(w*.39f,h*.68f);lineTo(w*.61f,h*.43f);lineTo(w*.61f,h*.68f)}
  drawPath(n,on,style=Stroke(w*.075f))
 }
}
