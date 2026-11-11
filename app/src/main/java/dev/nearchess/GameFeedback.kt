package dev.nearchess

import android.content.Context
import android.media.*
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.*

/** Original procedural audio: offline PCM, not copied third-party assets. */
private class ChessAudio {
 private var track:AudioTrack?=null
 @Synchronized fun play(capture:Boolean,ending:Boolean,draw:Boolean){
  close()
  val rate=22050
  val duration=if(ending).32 else if(capture).11 else .065
  val count=(rate*duration).toInt()
  var seed=719
  val data=ShortArray(count){i->
   val t=i.toDouble()/rate
   seed=seed*1664525+1013904223
   val noise=((seed ushr 9)/4194303.5-1.0)
   val sample=if(ending){
    val envelope=sin(PI*i/count).pow(2)*.16
    (sin(2*PI*440*t)+sin(2*PI*(if(draw)550 else 660)*t))*.5*envelope
   }else{
    val attack=(t/.0015).coerceAtMost(1.0)
    val f=if(capture)600.0 else 920.0
    val body=sin(2*PI*f*t)*exp(-t*(if(capture)55 else 85))
    val click=noise*exp(-t*220)*.6
    (body*.55+click)*attack*.45
   }
   (sample.coerceIn(-1.0,1.0)*32767).toInt().toShort()
  }
  val audio=AudioTrack.Builder()
   .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
   .setAudioFormat(AudioFormat.Builder().setSampleRate(rate).setEncoding(AudioFormat.ENCODING_PCM_16BIT).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
   .setTransferMode(AudioTrack.MODE_STATIC).setBufferSizeInBytes(data.size*2).build()
  track=audio
  if(audio.state==AudioTrack.STATE_INITIALIZED){audio.write(data,0,data.size);audio.play()}
 }
 @Synchronized fun close(){track?.let{runCatching{it.stop()};it.release()};track=null}
}
@Composable internal fun GameFeedback(history:List<Position>,result:String){
 val context=LocalContext.current
 val prefs=remember{context.getSharedPreferences("feedback",0)}
 val manager=remember{context.getSystemService(AudioManager::class.java)}
 val sound=remember{ChessAudio()}
 DisposableEffect(sound){onDispose{sound.close()}}
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
  val quiet=manager==null || manager.ringerMode!=AudioManager.RINGER_MODE_NORMAL || manager.getStreamVolume(AudioManager.STREAM_MUSIC)==0
  if(!quiet && prefs.getBoolean("sound",false))withContext(Dispatchers.Default){
   runCatching{sound.play(current.board.count{it!='.'}<old.board.count{it!='.'},terminal,result.contains("Ничья",true))}
  }
  if(!quiet && prefs.getBoolean("haptic",false)){
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
 Row{Text("Щелчки фигур и взятий",Modifier.weight(1f));Switch(checked=sound,onCheckedChange={sound=it;prefs.edit().putBoolean("sound",it).apply()})}
 Row{Text("Лёгкая вибрация",Modifier.weight(1f));Switch(checked=vibration,onCheckedChange={vibration=it;prefs.edit().putBoolean("haptic",it).apply()})}
 Text("Короткие оригинальные звуки, без загрузки из сети. В беззвучном режиме отклик отключён.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
}
