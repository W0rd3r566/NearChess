package dev.nearchess

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun BotSetup(onBack:()->Unit,onStart:(BotLevel,Boolean)->Unit){
 val context=androidx.compose.ui.platform.LocalContext.current
 val prefs=remember{context.getSharedPreferences("bot_setup",0)}
 var chosen by remember{mutableStateOf(BotLevel.entries[prefs.getInt("level",2).coerceIn(0..7)])}
 var white by remember{mutableStateOf(prefs.getBoolean("white",true))}
 var archive by remember{mutableStateOf(false)}
 if(archive){ArchiveScreen(onBack={archive=false});return}
 BackHandler(onBack=onBack)
 val labels=listOf("400","700","1000","1300","1600","1900","2200","2500+")
 val descriptions=listOf("Материал и простые взятия","Проверяет ответ соперника","Развитие фигур и контроль центра","Больший бюджет тактического расчёта","Позиционная оценка и углубление","Длительный поиск вариантов","Высокий бюджет встроенного поиска","Максимальный доступный бюджет поиска")
 Scaffold(topBar={TopAppBar(title={Text("Компьютер")},navigationIcon={IconButton(onClick=onBack){Icon(Icons.AutoMirrored.Filled.ArrowBack,"Назад")}},actions={IconButton(onClick={archive=true}){Icon(Icons.Default.History,"История партий")}})},bottomBar={
  Surface(color=MaterialTheme.colorScheme.surfaceContainer,tonalElevation=2.dp){
   Column(Modifier.navigationBarsPadding().fillMaxWidth().padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
    Text("${chosen.title} · ${if(white)"белые" else "чёрные"} · без часов",style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
    Button(onClick={prefs.edit().putInt("level",chosen.ordinal).putBoolean("white",white).apply();onStart(chosen,white)},modifier=Modifier.fillMaxWidth().heightIn(min=52.dp)){Text("Начать партию")}
   }
  }
 }){padding->
  LazyColumn(Modifier.fillMaxSize().padding(padding),contentPadding=PaddingValues(horizontal=20.dp,vertical=12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
   item{Text("Твой соперник",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.SemiBold);Text("Выбери характер тренировки",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)}
   item{
    Text("Цвет фигур",Modifier.padding(top=8.dp,bottom=8.dp),style=MaterialTheme.typography.labelLarge)
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()){
     SegmentedButton(selected=white,onClick={white=true},shape=SegmentedButtonDefaults.itemShape(0,2),label={Text("Белые")})
     SegmentedButton(selected=!white,onClick={white=false},shape=SegmentedButtonDefaults.itemShape(1,2),label={Text("Чёрные")})
    }
   }
   item{Text("Сложность",Modifier.padding(top=12.dp,bottom=4.dp),style=MaterialTheme.typography.titleMedium)}
   itemsIndexed(BotLevel.entries){i,level->
    val selected=chosen==level
    val background by animateColorAsState(if(selected)MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,label="opponent selection")
    Surface(color=background,shape=MaterialTheme.shapes.medium,modifier=Modifier.fillMaxWidth().selectable(selected=selected,role=Role.RadioButton,onClick={chosen=level})){
     Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){
      Column(Modifier.width(32.dp),horizontalAlignment=Alignment.CenterHorizontally){
       Text("${i+1}",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.SemiBold)
       Row(Modifier.height(12.dp),horizontalArrangement=Arrangement.spacedBy(2.dp),verticalAlignment=Alignment.Bottom){repeat(4){bar->Box(Modifier.width(5.dp).height((3+bar*3).dp).background(if(bar<=i/2)MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,MaterialTheme.shapes.extraSmall))}}
      }
      Column(Modifier.weight(1f)){
       Text(if(i==7)"Гроссмейстер · максимум" else level.title,style=MaterialTheme.typography.titleSmall)
       Text(descriptions[i],style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
       Text("Ориентир ${labels[i]}",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
      }
      RadioButton(selected=selected,onClick=null)
     }
    }
   }
   item{Text("Обозначения сложности условные: это не измеренный Elo. Встроенный движок не гарантирует силу игрока соответствующего рейтинга.",Modifier.padding(vertical=12.dp),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
  }
 }
}
