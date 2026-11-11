package dev.nearchess

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.delay

/** Non-focusable popup: no dimming and no full-screen touch target. */
@Composable internal fun ChatPreview(timeline:ChatTimeline,chatOpen:Boolean,peer:String,show:Boolean,sound:Boolean,onOpen:()->Unit){
 val context=LocalContext.current
 val motion=rememberNearMotionEnabled()
 val incoming=timeline.entries.lastOrNull{!it.mine}
 var lastSeen by rememberSaveable{mutableStateOf(incoming?.id?:-1L)}
 var entry by remember{mutableStateOf<ChatEntry?>(null)}
 var visible by remember{mutableStateOf(false)}
 val audio=remember{context.getSystemService(AudioManager::class.java)}
 val tone=remember{runCatching{ToneGenerator(AudioManager.STREAM_NOTIFICATION,25)}.getOrNull()}
 DisposableEffect(tone){onDispose{tone?.release()}}
 LaunchedEffect(incoming?.id,chatOpen,show){
  if(chatOpen || !show){visible=false;lastSeen=incoming?.id?:lastSeen;return@LaunchedEffect}
  val next=incoming?:return@LaunchedEffect
  if(next.id<=lastSeen)return@LaunchedEffect
  lastSeen=next.id;entry=next;visible=true
  if(sound && audio?.ringerMode==AudioManager.RINGER_MODE_NORMAL && audio.getStreamVolume(AudioManager.STREAM_NOTIFICATION)>0)runCatching{tone?.startTone(ToneGenerator.TONE_PROP_BEEP,60)}
  delay(4500);visible=false;delay(if(motion)180 else 0);entry=null
 }
 if(entry!=null && !chatOpen && show)Popup(alignment=Alignment.TopCenter,properties=PopupProperties(focusable=false,dismissOnBackPress=false,dismissOnClickOutside=false)){
  Box(Modifier.statusBarsPadding().padding(horizontal=16.dp,vertical=8.dp)){
   AnimatedVisibility(visible,enter=fadeIn(tween(if(motion)180 else 0))+slideInVertically(tween(if(motion)220 else 0)){ -it/3 },exit=fadeOut(tween(if(motion)150 else 0))){
    Surface(onClick=onOpen,shape=MaterialTheme.shapes.large,color=MaterialTheme.colorScheme.secondaryContainer,tonalElevation=3.dp,modifier=Modifier.widthIn(max=360.dp).fillMaxWidth()){
     Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(4.dp)){
      Text(peer,style=MaterialTheme.typography.labelLarge)
      Text(entry?.text?:"",style=MaterialTheme.typography.bodyMedium,maxLines=2,overflow=TextOverflow.Ellipsis)
      Text(if(timeline.unread>1)"${timeline.unread} непрочитанных · открыть чат" else "Нажми, чтобы ответить",style=MaterialTheme.typography.labelSmall)
     }
    }
   }
  }
 }
}
