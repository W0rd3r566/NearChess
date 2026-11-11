package dev.nearchess

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.content.SharedPreferences
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay

/** Local presentation only: feedback never changes a position or network state. */
@Composable internal fun GameFeedback(history:List<Position>, result:String) {
 val context=LocalContext.current
 val prefs=remember{context.getSharedPreferences("feedback",0)}
 val audio=remember{context.getSystemService(AudioManager::class.java)}
 val tone=remember{runCatching{ToneGenerator(AudioManager.STREAM_MUSIC,45)}.getOrNull()}
 DisposableEffect(tone){onDispose{tone?.release()}}
 var previous by remember{mutableStateOf(history.last())}
 var previousSize by remember{mutableStateOf(history.size)}
 var announced by remember{mutableStateOf(false)}
 val current=history.last()
 LaunchedEffect(current,history.size,result){
  val advanced=history.size==previousSize+1
  val old=previous;previous=current;previousSize=history.size
  if(result.isEmpty())announced=false
  if(!advanced)return@LaunchedEffect
  val terminal=result.isNotEmpty() && !announced
  if(terminal)announced=true
  val quiet=audio?.ringerMode!=AudioManager.RINGER_MODE_NORMAL || audio.getStreamVolume(AudioManager.STREAM_MUSIC)==0
  if(!quiet && prefs.getBoolean("sound",false)) {
   val captured=current.board.count{it!='.'}<old.board.count{it!='.'}
   val type=when{terminal && result.contains("Ничья",true)->ToneGenerator.TONE_PROP_ACK;terminal->ToneGenerator.TONE_PROP_PROMPT;captured->ToneGenerator.TONE_PROP_BEEP2;else->ToneGenerator.TONE_PROP_BEEP}
   tone?.startTone(type,if(terminal)220 else 45)
   if(terminal){delay(250);tone?.stopTone()}
  }
  if(!quiet && prefs.getBoolean("haptic",false)) {
   val vibrator=if(Build.VERSION.SDK_INT>=31)context.getSystemService(VibratorManager::class.java)?.defaultVibrator else context.getSystemService(Vibrator::class.java)
   runCatching{vibrator?.vibrate(VibrationEffect.createOneShot(if(terminal)40L else 12L,VibrationEffect.DEFAULT_AMPLITUDE))}
  }
 }
}

@Composable internal fun FeedbackControls(){
 val context=LocalContext.current
 val prefs=remember{context.getSharedPreferences("feedback",0)}
 var sound by remember{mutableStateOf(prefs.getBoolean("sound",false))}
 var vibration by remember{mutableStateOf(prefs.getBoolean("haptic",false))}
 Text("Звук и отклик",style=MaterialTheme.typography.titleMedium)
 Row{Text("Короткие сигналы партии",Modifier.weight(1f));Switch(checked=sound,onCheckedChange={sound=it;prefs.edit().putBoolean("sound",it).apply()})}
 Row{Text("Лёгкая вибрация",Modifier.weight(1f));Switch(checked=vibration,onCheckedChange={vibration=it;prefs.edit().putBoolean("haptic",it).apply()})}
 Text("По умолчанию выключены. В беззвучном режиме отклик не воспроизводится. Сигналы пока системные, не записанные звуковые ассеты.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
}
