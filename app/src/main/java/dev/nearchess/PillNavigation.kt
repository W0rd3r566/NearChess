package dev.nearchess

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable fun PillNavigation(selected:Int,onSelect:(Int)->Unit){
 val icons=listOf(Icons.Outlined.SportsEsports,Icons.Outlined.History,Icons.Outlined.Settings)
 val labels=listOf("Играть","Партии","Настройки")
 val motion=rememberNearMotionEnabled()
 Box(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal=24.dp,vertical=12.dp),contentAlignment=Alignment.Center){
  Surface(shape=CircleShape,color=MaterialTheme.colorScheme.surfaceContainerHigh,tonalElevation=3.dp,modifier=Modifier.widthIn(max=420.dp).fillMaxWidth()){
   Row(Modifier.selectableGroup().padding(6.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(4.dp)){
    labels.forEachIndexed{i,label->
     val active=selected==i
     val interaction=remember{MutableInteractionSource()}
     val bg by animateColorAsState(if(active)MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,animationSpec=tween(if(motion)NearMotion.State else 0),label="tab background")
     val fg by animateColorAsState(if(active)MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,animationSpec=tween(if(motion)NearMotion.State else 0),label="tab content")
     // Selectable Surface owns its indication INSIDE its shape. Do not attach an
     // external selectable modifier: Surface's internal layer can reorder clipping.
     Surface(selected=active,onClick={onSelect(i)},interactionSource=interaction,shape=CircleShape,color=bg,contentColor=fg,modifier=Modifier.weight(1f).nearPress(interaction)){
      Column(Modifier.heightIn(min=58.dp).padding(horizontal=4.dp,vertical=8.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(3.dp)){
       Icon(icons[i],contentDescription=null,modifier=Modifier.size(22.dp))
       Text(label,style=MaterialTheme.typography.labelMedium,fontWeight=if(active)FontWeight.SemiBold else FontWeight.Medium,maxLines=1)
      }
     }
    }
   }
  }
 }
}
