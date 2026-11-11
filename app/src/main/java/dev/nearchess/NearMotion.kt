package dev.nearchess

import android.provider.Settings
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext

object NearMotion {
 const val Press=110
 const val State=180
 const val Navigation=220
 const val Panel=280
 const val Piece=170
}
/** Observe user preference and Android's animator duration scale without writing system settings. */
@Composable fun rememberNearMotionEnabled():Boolean {
 val context=LocalContext.current
 val prefs=remember{context.getSharedPreferences("board",0)}
 fun allowed()=prefs.getBoolean("animations",true) && runCatching{Settings.Global.getFloat(context.contentResolver,Settings.Global.ANIMATOR_DURATION_SCALE,1f)>0f}.getOrDefault(true)
 var enabled by remember{mutableStateOf(allowed())}
 DisposableEffect(context,prefs){
  val observer=object:ContentObserver(Handler(Looper.getMainLooper())){override fun onChange(selfChange:Boolean){enabled=allowed()}}
  val listener=android.content.SharedPreferences.OnSharedPreferenceChangeListener{_,_->enabled=allowed()}
  context.contentResolver.registerContentObserver(Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE),false,observer)
  prefs.registerOnSharedPreferenceChangeListener(listener)
  onDispose{context.contentResolver.unregisterContentObserver(observer);prefs.unregisterOnSharedPreferenceChangeListener(listener)}
 }
 return enabled
}
@Composable fun Modifier.nearPress(interaction:MutableInteractionSource):Modifier {
 val enabled=rememberNearMotionEnabled()
 val pressed by interaction.collectIsPressedAsState()
 val scale by animateFloatAsState(if(pressed && enabled).97f else 1f,animationSpec=if(enabled)spring(dampingRatio=.9f,stiffness=700f)else snap(),label="near press")
 return graphicsLayer{scaleX=scale;scaleY=scale}
}
/** Crossfade only: no translated duplicate hit targets and no layout-size animation. */
@Composable fun NearPage(target:Int,content:@Composable (Int)->Unit){
 val enabled=rememberNearMotionEnabled()
 AnimatedContent(targetState=target,transitionSpec={fadeIn(tween(if(enabled)NearMotion.Navigation else 0)) togetherWith fadeOut(tween(if(enabled)110 else 0))},label="near page"){content(it)}
}
