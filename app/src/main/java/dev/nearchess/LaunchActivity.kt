package dev.nearchess

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

class LaunchActivity:ComponentActivity(){override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{NearTheme{MaterialTheme(typography=NearTypography){LaunchScreen()}}}}}
@Composable private fun LaunchScreen(){
 val context=LocalContext.current
 var page by remember{mutableStateOf(0)}
 fun open(type:Class<*>){context.startActivity(Intent(context,type))}
 BackHandler(enabled=page!=0){page=0}
 Scaffold(bottomBar={PillNavigation(page){page=it}}){padding->Box(Modifier.fillMaxSize().padding(padding)){
  when(page){1->ArchiveScreen(onBack={page=0});2->AppearanceScreen();else->Column(Modifier.fillMaxSize().padding(horizontal=20.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(16.dp)){
   Spacer(Modifier.height(4.dp))
   Row(verticalAlignment=Alignment.CenterVertically){
    NearLogo(Modifier.size(40.dp));Spacer(Modifier.width(10.dp))
    Text("NearChess",Modifier.weight(1f),style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.SemiBold)
    IconButton(onClick={open(ProfileActivity::class.java)}){Icon(Icons.Outlined.AccountCircle,"Профиль")}
   }
   val name=context.getSharedPreferences("profile",0).getString("nickname","Игрок")?:"Игрок"
   Column(verticalArrangement=Arrangement.spacedBy(4.dp)){
    Text("Твой ход, $name",style=MaterialTheme.typography.headlineSmall)
    Text("Одна доска. Тысячи возможностей.",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
   }
   Surface(shape=MaterialTheme.shapes.extraLarge,color=MaterialTheme.colorScheme.primaryContainer){
    Row(Modifier.fillMaxWidth().padding(16.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){
     Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(4.dp)){
      Text("Начнём партию?",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.SemiBold)
      Text("С компьютером или вдвоём",style=MaterialTheme.typography.bodySmall)
     }
     FilledIconButton(onClick={open(MainActivity::class.java)},modifier=Modifier.size(52.dp)){Icon(Icons.Outlined.PlayArrow,"Выбрать и начать партию",Modifier.size(28.dp))}
    }
   }
   Text("Рядом с другом",style=MaterialTheme.typography.labelLarge,color=MaterialTheme.colorScheme.onSurfaceVariant)
   Surface(shape=MaterialTheme.shapes.large,color=MaterialTheme.colorScheme.surfaceContainerLow){
    Column{
     HomeRow("Bluetooth","Создать партию или подключиться",Icons.Outlined.Bluetooth){open(NearbyActivity::class.java)}
     HorizontalDivider(Modifier.padding(start=56.dp,end=16.dp),color=MaterialTheme.colorScheme.outlineVariant)
     HomeRow("На время","Одна доска · 3+2, 5+0, 10+5",Icons.Outlined.Timer){open(TimedActivity::class.java)}
    }
   }
   Text("Твои шахматы",style=MaterialTheme.typography.labelLarge,color=MaterialTheme.colorScheme.onSurfaceVariant)
   Surface(shape=MaterialTheme.shapes.large,color=MaterialTheme.colorScheme.surfaceContainerLow){Column{
    if(context.getSharedPreferences("game",0).contains("positions")){
     HomeRow("Продолжить","Открыть сохранённую партию",Icons.Outlined.PlayCircle){open(MainActivity::class.java)}
     HorizontalDivider(Modifier.padding(start=56.dp,end=16.dp),color=MaterialTheme.colorScheme.outlineVariant)
    }
    HomeRow("История","${GameArchive(context).all().size} завершённых партий",Icons.Outlined.History){page=1}
    HorizontalDivider(Modifier.padding(start=56.dp,end=16.dp),color=MaterialTheme.colorScheme.outlineVariant)
    HomeRow("Оформление","Доска, фигуры и тема",Icons.Outlined.Palette){page=2}
   }}
   Spacer(Modifier.height(4.dp))
  }}
 }}
}
@Composable private fun HomeRow(title:String,subtitle:String,icon:androidx.compose.ui.graphics.vector.ImageVector,onClick:()->Unit){
 Surface(onClick=onClick,color=MaterialTheme.colorScheme.surfaceContainerLow){
  ListItem(colors=ListItemDefaults.colors(containerColor=MaterialTheme.colorScheme.surfaceContainerLow),leadingContent={Icon(icon,null,tint=MaterialTheme.colorScheme.primary,modifier=Modifier.size(24.dp))},headlineContent={Text(title,style=MaterialTheme.typography.titleMedium)},supportingContent={Text(subtitle,style=MaterialTheme.typography.bodySmall)},trailingContent={Icon(Icons.Outlined.ChevronRight,null,Modifier.size(20.dp))})
 }
}
@Composable private fun AppearanceScreen(){
 val context=LocalContext.current
 val board=remember{context.getSharedPreferences("board",0)}
 val game=remember{context.getSharedPreferences("game",0)}
 var palette by remember{mutableStateOf(game.getInt("theme",0).coerceIn(boardPalettes.indices))}
 var set by remember{mutableStateOf(board.getString("pieces","cburnett")?:"cburnett")}
 var animations by remember{mutableStateOf(board.getBoolean("animations",true))}
 Column(Modifier.fillMaxSize().padding(horizontal=20.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(16.dp)){
  Spacer(Modifier.height(8.dp))
  Text("Твой стиль игры",style=MaterialTheme.typography.headlineSmall)
  Text("Превью меняется сразу",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
  Surface(shape=MaterialTheme.shapes.large,color=MaterialTheme.colorScheme.surfaceContainerLow){Column(Modifier.padding(12.dp)){
   AppearancePreview(set,boardPalettes[palette])
   Text("${boardPalettes[palette].name} · ${set.replaceFirstChar{it.uppercase()}}",Modifier.padding(top=8.dp),style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
  }}
  Text("Доска",style=MaterialTheme.typography.titleMedium)
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)){
   boardPalettes.forEachIndexed{i,p->
    Column(Modifier.weight(1f),horizontalAlignment=Alignment.CenterHorizontally){
     FilledTonalIconButton(onClick={palette=i;game.edit().putInt("theme",i).apply()},colors=IconButtonDefaults.filledTonalIconButtonColors(containerColor=if(palette==i)MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh),modifier=Modifier.size(48.dp)){
      Canvas(Modifier.size(28.dp)){val c=size.width/2;for(r in 0..1)for(col in 0..1)drawRect(if((r+col)%2==0)Color(p.light)else Color(p.dark),Offset(col*c,r*c),Size(c,c))}
     }
     Text(if(palette==i)"• ${p.name}" else p.name,style=MaterialTheme.typography.labelSmall,maxLines=1)
    }
   }
  }
  Text("Фигуры",style=MaterialTheme.typography.titleMedium)
  listOf("cburnett","chessnut","spatial").forEach{s->
   FilterChip(selected=set==s,onClick={set=s;board.edit().putString("pieces",s).apply()},label={Text(s.replaceFirstChar{it.uppercase()})},modifier=Modifier.fillMaxWidth())
  }
  HorizontalDivider();ThemeControls();HorizontalDivider()
  Row(verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text("Анимации и эффекты",style=MaterialTheme.typography.titleMedium);Text("Движение фигур, шах и мат",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)};Switch(checked=animations,onCheckedChange={animations=it;board.edit().putBoolean("animations",it).apply()})}
  FeedbackControls();HorizontalDivider()
  Text("О NearChess",style=MaterialTheme.typography.titleMedium)
  val version=remember{runCatching{context.packageManager.getPackageInfo(context.packageName,0).versionName}.getOrNull()?:""}
  Text("Версия $version",style=MaterialTheme.typography.bodySmall)
  SourceCodeLink()
  Spacer(Modifier.height(8.dp))
 }
}
