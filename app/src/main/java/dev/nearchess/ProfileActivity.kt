package dev.nearchess

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ProfileActivity:ComponentActivity(){override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{NearTheme{ProfileScreen()}}}}
@Composable private fun ProfileScreen(){
 val context=LocalContext.current;val prefs=remember{context.getSharedPreferences("profile",0)}
 val file=remember{File(context.filesDir,"profile-avatar.jpg")}
 var nickname by remember{mutableStateOf(prefs.getString("nickname","Игрок")?:"Игрок")}
 var revision by remember{mutableStateOf(0)};var busy by remember{mutableStateOf(false)};var status by remember{mutableStateOf("")};var remove by remember{mutableStateOf(false)};var archive by remember{mutableStateOf(false)}
 val scope=rememberCoroutineScope()
 val games=remember{GameArchive(context).all()}
 val bitmap by produceState<android.graphics.Bitmap?>(null,revision){value=withContext(Dispatchers.IO){runCatching{if(file.exists())BitmapFactory.decodeFile(file.absolutePath)else null}.getOrNull()}}
 val picker=rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()){uri->if(uri!=null)scope.launch{
  busy=true
  val outcome=withContext(Dispatchers.IO){runCatching{
   val resolver=context.contentResolver;val bounds=BitmapFactory.Options().apply{inJustDecodeBounds=true}
   resolver.openInputStream(uri)?.use{BitmapFactory.decodeStream(it,null,bounds)}
   require(bounds.outWidth>0 && bounds.outHeight>0){"Не удалось прочитать изображение"}
   var sample=1;while(maxOf(bounds.outWidth,bounds.outHeight)/sample>1024)sample*=2
   val source=resolver.openInputStream(uri)?.use{BitmapFactory.decodeStream(it,null,BitmapFactory.Options().apply{inSampleSize=sample})}?:error("Формат не поддерживается")
   val edge=minOf(source.width,source.height);val crop=android.graphics.Bitmap.createBitmap(source,(source.width-edge)/2,(source.height-edge)/2,edge,edge)
   val scaled=android.graphics.Bitmap.createScaledBitmap(crop,512,512,true);val temp=File(context.filesDir,"profile-avatar.tmp")
   temp.outputStream().use{require(scaled.compress(android.graphics.Bitmap.CompressFormat.JPEG,90,it))};require(temp.renameTo(file)){"Не удалось сохранить фото"}
  }}
  busy=false;outcome.onSuccess{revision++;status="Аватар сохранён"}.onFailure{status=it.message?:"Ошибка изображения"}
 }}
 if(archive){ArchiveScreen(onBack={archive=false});return}
 Surface(Modifier.fillMaxSize()){
  Column(Modifier.systemBarsPadding().imePadding().fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){
   TextButton(onClick={(context as? android.app.Activity)?.finish()}){Text("Главная")}
   Text("Профиль",style=MaterialTheme.typography.headlineSmall)
   Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(16.dp)){
    val image=bitmap
    if(image!=null)Image(image.asImageBitmap(),"Твой аватар",Modifier.size(80.dp).clip(CircleShape),contentScale=ContentScale.Crop)
    else Surface(shape=MaterialTheme.shapes.large,color=MaterialTheme.colorScheme.primaryContainer,modifier=Modifier.size(80.dp)){Box(contentAlignment=Alignment.Center){Text(nickname.firstOrNull()?.uppercase()?:"?",style=MaterialTheme.typography.headlineMedium)}}
    Column{Text(nickname,style=MaterialTheme.typography.titleLarge);Text("Локальный профиль",color=MaterialTheme.colorScheme.onSurfaceVariant);Text("Без аккаунта и облака",style=MaterialTheme.typography.labelSmall)}
   }
   FilledTonalButton(enabled=!busy,onClick={picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))}){Text(if(busy)"Сохраняю…" else "Изменить фото")}
   if(bitmap!=null)TextButton(enabled=!busy,onClick={remove=true}){Text("Убрать фото")}
   OutlinedTextField(value=nickname,onValueChange={nickname=it.take(24)},label={Text("Никнейм")},singleLine=true,isError=nickname.isBlank(),supportingText={Text("${nickname.length}/24")},modifier=Modifier.fillMaxWidth())
   Button(enabled=nickname.isNotBlank() && !busy,onClick={prefs.edit().putString("nickname",nickname.trim()).apply();status="Профиль сохранён"}){Text("Сохранить")}
   HorizontalDivider();Text("Реальные результаты",style=MaterialTheme.typography.titleMedium)
   Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Column{Text("${games.size}",style=MaterialTheme.typography.headlineMedium);Text("Партий")};Column{Text("${games.count{it.result=="1/2-1/2"}}",style=MaterialTheme.typography.headlineMedium);Text("Ничьих")}}
   if(games.isEmpty())Text("Заверши первую партию — здесь появится статистика.",color=MaterialTheme.colorScheme.onSurfaceVariant)
   else games.groupBy{it.mode}.forEach{(mode,list)->ListItem(headlineContent={Text(if(mode=="bot")"Тренировка" else "Локальная игра")},trailingContent={Text("${list.size}")})}
   Text("Победы и поражения относительно игрока пока не вычисляются: в старом архиве не сохранялся его цвет. Ничего не подставляем вместо реальных данных.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
   OutlinedButton(onClick={archive=true},modifier=Modifier.fillMaxWidth()){Text("Открыть историю партий")}
   if(status.isNotEmpty())Text(status,color=MaterialTheme.colorScheme.primary)
  }
 }
 if(remove)AlertDialog(onDismissRequest={remove=false},title={Text("Убрать аватар?")},text={Text("Оригинальное фото останется в галерее.")},confirmButton={TextButton(onClick={if(!file.exists() || file.delete()){revision++;status="Аватар убран"}else status="Ошибка удаления";remove=false}){Text("Убрать")}},dismissButton={TextButton(onClick={remove=false}){Text("Отмена")}})
}
