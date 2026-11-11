package dev.nearchess
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

class LaunchActivity:ComponentActivity(){override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{NearTheme{MaterialTheme(typography=NearTypography){LaunchScreen()}}}}}
@Composable private fun LaunchScreen(){
 val context=LocalContext.current;var page by remember{mutableStateOf(0)}
 fun open(type:Class<*>){context.startActivity(Intent(context,type))}
 BackHandler(enabled=page!=0){page=0}
 Scaffold(bottomBar={PillNavigation(page){page=it}}){padding->Box(Modifier.fillMaxSize().padding(padding)){
  when(page){1->ArchiveScreen(onBack={page=0});2->AppearanceScreen();else->Column(Modifier.fillMaxSize().padding(horizontal=20.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(16.dp)){
   Spacer(Modifier.height(8.dp))
   Row(verticalAlignment=Alignment.CenterVertically){NearLogo(Modifier.size(44.dp));Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text("NearChess",style=MaterialTheme.typography.titleLarge);Text("Шахматы рядом",style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)};IconButton(onClick={open(ProfileActivity::class.java)}){Icon(Icons.Outlined.Person,"Профиль")}}
   val name=context.getSharedPreferences("profile",0).getString("nickname","Игрок")?:"Игрок"
   Text("Твой ход, $name",style=MaterialTheme.typography.headlineSmall)
   Text("Хорошая партия начинается рядом.",style=MaterialTheme.typography.bodyLarge,color=MaterialTheme.colorScheme.onSurfaceVariant)
   Surface(shape=MaterialTheme.shapes.extraLarge,color=MaterialTheme.colorScheme.primaryContainer){Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){Text("Новая партия",style=MaterialTheme.typography.titleLarge);Text("Выбери компьютерного соперника или играй вдвоём.",style=MaterialTheme.typography.bodyMedium);Button(onClick={open(MainActivity::class.java)},modifier=Modifier.fillMaxWidth().heightIn(min=48.dp)){Text("Выбрать игру")}}}
   HomeRow("С другом по Bluetooth","Без интернета и аккаунта",Icons.Outlined.Bluetooth){open(NearbyActivity::class.java)}
   HomeRow("Партия с часами","3+2 · 5+0 · 10+5",Icons.Outlined.Timer){open(TimedActivity::class.java)}
   if(context.getSharedPreferences("game",0).contains("positions"))HomeRow("Сохранённая партия","Вернуться к доске",Icons.Outlined.PlayArrow){open(MainActivity::class.java)}
   HomeRow("История партий","${GameArchive(context).all().size} завершённых",Icons.Outlined.History){page=1}
   HomeRow("Профиль","Ник и собственное фото",Icons.Outlined.Person){open(ProfileActivity::class.java)}
  }}
 }}
}
@Composable private fun HomeRow(title:String,subtitle:String,icon:androidx.compose.ui.graphics.vector.ImageVector,onClick:()->Unit){Surface(onClick=onClick,shape=MaterialTheme.shapes.medium,color=MaterialTheme.colorScheme.surfaceContainerLow){ListItem(colors=ListItemDefaults.colors(containerColor=MaterialTheme.colorScheme.surfaceContainerLow),leadingContent={Icon(icon,null,tint=MaterialTheme.colorScheme.primary)},headlineContent={Text(title,style=MaterialTheme.typography.titleMedium)},supportingContent={Text(subtitle)},trailingContent={Icon(Icons.Outlined.ChevronRight,null)})}}
@Composable private fun AppearanceScreen(){
 val context=LocalContext.current
 val board=remember{context.getSharedPreferences("board",0)};val game=remember{context.getSharedPreferences("game",0)}
 var palette by remember{mutableStateOf(game.getInt("theme",0).coerceIn(boardPalettes.indices))}
 var set by remember{mutableStateOf(board.getString("pieces","cburnett")?:"cburnett")}
 var animations by remember{mutableStateOf(board.getBoolean("animations",true))}
 Column(Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)){
  Text("Настройки",style=MaterialTheme.typography.headlineSmall);ThemeControls();HorizontalDivider()
  Text("Доска",style=MaterialTheme.typography.titleMedium)
  boardPalettes.forEachIndexed{i,p->FilterChip(selected=palette==i,onClick={palette=i;game.edit().putInt("theme",i).apply()},label={Text(p.name)})}
  Text("Векторные фигуры",style=MaterialTheme.typography.titleMedium)
  listOf("cburnett","chessnut","spatial").forEach{s->FilterChip(selected=set==s,onClick={set=s;board.edit().putString("pieces",s).apply()},label={Text(s.replaceFirstChar{it.uppercase()})})}
  Row(verticalAlignment=Alignment.CenterVertically){Text("Анимации и эффекты",Modifier.weight(1f));Switch(checked=animations,onCheckedChange={animations=it;board.edit().putBoolean("animations",it).apply()})}
  FeedbackControls();HorizontalDivider();Text("О NearChess",style=MaterialTheme.typography.titleMedium)
  val version=remember{runCatching{context.packageManager.getPackageInfo(context.packageName,0).versionName}.getOrNull()?:""}
  Text("Версия $version");Text("Офлайн-шахматы",style=MaterialTheme.typography.bodySmall)
 }
}
