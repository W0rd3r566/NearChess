package dev.nearchess

import android.os.Build
import android.content.SharedPreferences
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

object NearTokens {
 val Space1=4.dp;val Space2=8.dp;val Space3=12.dp;val Space4=16.dp;val Space5=24.dp;val Space6=32.dp
 val CornerSmall=8.dp;val CornerMedium=12.dp;val CornerLarge=16.dp;val CornerExtraLarge=28.dp
}
@Composable private fun themeRevision(prefs:SharedPreferences):Int {
 var revision by remember{mutableStateOf(0)}
 DisposableEffect(prefs){
  val listener=SharedPreferences.OnSharedPreferenceChangeListener{_,_->revision++}
  prefs.registerOnSharedPreferenceChangeListener(listener)
  onDispose{prefs.unregisterOnSharedPreferenceChangeListener(listener)}
 }
 return revision
}
@Composable fun NearTheme(content:@Composable ()->Unit){
 val context=LocalContext.current
 val prefs=remember{context.getSharedPreferences("appearance",0)}
 val revision=themeRevision(prefs)
 val mode=remember(revision){prefs.getString("mode","dark")?:"dark"}
 val dynamic=remember(revision){prefs.getBoolean("wallpaper",false)}
 val dark=when(mode){"light"->false;"system"->isSystemInDarkTheme();else->true}
 val scheme=if(dynamic && Build.VERSION.SDK_INT>=31){if(dark)dynamicDarkColorScheme(context)else dynamicLightColorScheme(context)}else if(dark)darkColorScheme(
  primary=Color(0xFF71D8BC),onPrimary=Color(0xFF00382B),primaryContainer=Color(0xFF244D43),onPrimaryContainer=Color(0xFFA2F1D9),
  secondary=Color(0xFFB6CCC4),onSecondary=Color(0xFF20332D),secondaryContainer=Color(0xFF33463F),onSecondaryContainer=Color(0xFFD2E8DF),
  background=Color(0xFF111719),onBackground=Color(0xFFE3E9E6),surface=Color(0xFF111719),onSurface=Color(0xFFE3E9E6),
  surfaceContainerLowest=Color(0xFF0C1214),surfaceContainerLow=Color(0xFF192123),surfaceContainer=Color(0xFF1D2628),surfaceContainerHigh=Color(0xFF273133),surfaceContainerHighest=Color(0xFF323C3E),
  onSurfaceVariant=Color(0xFFB9C7C1),outline=Color(0xFF82938B),outlineVariant=Color(0xFF3D4A45)
 )else lightColorScheme(
  primary=Color(0xFF176B55),onPrimary=Color.White,primaryContainer=Color(0xFFBAEEDB),onPrimaryContainer=Color(0xFF073C2D),
  background=Color(0xFFF4F8F5),onBackground=Color(0xFF17201C),surface=Color(0xFFF4F8F5),onSurface=Color(0xFF17201C),
  surfaceContainerLow=Color(0xFFEDF2EE),surfaceContainer=Color(0xFFE7EDE8),surfaceContainerHigh=Color(0xFFE1E8E2),onSurfaceVariant=Color(0xFF43554B),outlineVariant=Color(0xFFC2CFC6)
 )
 MaterialTheme(colorScheme=scheme,typography=Typography(),shapes=Shapes(),content=content)
}
@Composable fun ThemeControls(){
 val context=LocalContext.current
 val prefs=remember{context.getSharedPreferences("appearance",0)}
 val revision=themeRevision(prefs)
 val mode=remember(revision){prefs.getString("mode","dark")}
 val wallpaper=remember(revision){prefs.getBoolean("wallpaper",false)}
 Text("Тема приложения",style=MaterialTheme.typography.titleMedium)
 Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){
  listOf("light" to "Светлая","dark" to "Тёмная","system" to "Система").forEach{(id,label)->FilterChip(selected=mode==id,onClick={prefs.edit().putString("mode",id).apply()},label={Text(label)})}
 }
 if(Build.VERSION.SDK_INT>=31)Row{Text("Цвета обоев Android",Modifier.weight(1f));Switch(checked=wallpaper,onCheckedChange={prefs.edit().putBoolean("wallpaper",it).apply()})}
}
@Composable fun NearLogo(modifier:Modifier=Modifier){
 val color=MaterialTheme.colorScheme.primary;val cut=MaterialTheme.colorScheme.surfaceContainerHigh
 Canvas(modifier.size(64.dp)){
  val w=size.width;val h=size.height
  val path=Path().apply{moveTo(w*.2f,h*.22f);lineTo(w*.32f,h*.22f);lineTo(w*.32f,h*.14f);lineTo(w*.44f,h*.14f);lineTo(w*.44f,h*.22f);lineTo(w*.56f,h*.22f);lineTo(w*.56f,h*.14f);lineTo(w*.68f,h*.14f);lineTo(w*.68f,h*.22f);lineTo(w*.8f,h*.22f);lineTo(w*.7f,h*.72f);lineTo(w*.8f,h*.8f);lineTo(w*.8f,h*.87f);lineTo(w*.2f,h*.87f);lineTo(w*.2f,h*.8f);lineTo(w*.3f,h*.72f);close()}
  drawPath(path,color)
  val n=Path().apply{moveTo(w*.39f,h*.68f);lineTo(w*.39f,h*.4f);lineTo(w*.61f,h*.68f);lineTo(w*.61f,h*.4f)}
  drawPath(n,cut,style=Stroke(w*.065f))
 }
}
