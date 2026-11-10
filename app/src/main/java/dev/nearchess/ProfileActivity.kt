package dev.nearchess

import android.content.Intent
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ProfileActivity : ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  setContent { MaterialTheme(colorScheme=darkColorScheme(primary=Color(0xFF71D8BC),onPrimary=Color(0xFF00382B),surface=Color(0xFF121A1C),onSurface=Color(0xFFEDF3EF))) { ProfileScreen() } }
 }
}
@Composable private fun ProfileScreen() {
 val context=LocalContext.current
 val prefs=remember{context.getSharedPreferences("profile",0)}
 val file=remember{File(context.filesDir,"profile-avatar.jpg")}
 var nickname by remember{mutableStateOf(prefs.getString("nickname","Игрок")?:"Игрок")}
 var revision by remember{mutableStateOf(0)}
 var busy by remember{mutableStateOf(false)}
 var status by remember{mutableStateOf("")}
 var remove by remember{mutableStateOf(false)}
 val scope=rememberCoroutineScope()
 val bitmap by produceState<android.graphics.Bitmap?>(null,revision) {
  value=withContext(Dispatchers.IO){runCatching{if(file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null}.getOrNull()}
 }
 val picker=rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()){uri ->
  if(uri!=null) scope.launch {
   busy=true
   val outcome=withContext(Dispatchers.IO){runCatching {
    val resolver=context.contentResolver
    val bounds=BitmapFactory.Options().apply{inJustDecodeBounds=true}
    resolver.openInputStream(uri)?.use{BitmapFactory.decodeStream(it,null,bounds)}
    require(bounds.outWidth>0 && bounds.outHeight>0){"Не удалось прочитать изображение"}
    var sample=1
    while(maxOf(bounds.outWidth,bounds.outHeight)/sample>1024) sample*=2
    val opts=BitmapFactory.Options().apply{inSampleSize=sample}
    val source=resolver.openInputStream(uri)?.use{BitmapFactory.decodeStream(it,null,opts)} ?: error("Формат изображения не поддерживается")
    val edge=minOf(source.width,source.height)
    val crop=android.graphics.Bitmap.createBitmap(source,(source.width-edge)/2,(source.height-edge)/2,edge,edge)
    val scaled=android.graphics.Bitmap.createScaledBitmap(crop,512,512,true)
    val temp=File(context.filesDir,"profile-avatar.tmp")
    temp.outputStream().use{require(scaled.compress(android.graphics.Bitmap.CompressFormat.JPEG,90,it))}
    require(temp.renameTo(file)){"Не удалось сохранить фото"}
   }}
   busy=false
   outcome.onSuccess{revision++; status="Фото сохранено только на этом телефоне"}.onFailure{status=it.message?:"Не удалось загрузить фото"}
  }
 }
 Surface(Modifier.fillMaxSize()) {
  Column(Modifier.systemBarsPadding().imePadding().widthIn(max=560.dp).fillMaxWidth().padding(24.dp).verticalScroll(rememberScrollState()),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(16.dp)) {
   Text("Твой NearChess",style=MaterialTheme.typography.headlineLarge)
   Text("Локальный профиль · без регистрации",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
   val image=bitmap
   if(image!=null) Image(image.asImageBitmap(),"Аватар игрока",Modifier.size(112.dp).clip(CircleShape),contentScale=ContentScale.Crop)
   else PlayerAvatar(0,Modifier.size(112.dp))
   FilledTonalButton(enabled=!busy,onClick={picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))}){Text(if(busy) "Сохраняю…" else "Выбрать фото")}
   if(image!=null) TextButton(enabled=!busy,onClick={remove=true}){Text("Убрать фото")}
   OutlinedTextField(value=nickname,onValueChange={nickname=it.take(24)},label={Text("Ник")},singleLine=true,supportingText={Text("1–24 символа · ${nickname.length}/24")},isError=nickname.isBlank(),modifier=Modifier.fillMaxWidth())
   Button(enabled=nickname.isNotBlank() && !busy,onClick={prefs.edit().putString("nickname",nickname.trim()).apply(); status="Ник сохранён"},modifier=Modifier.fillMaxWidth()){Text("Сохранить профиль")}
   OutlinedButton(enabled=!busy,onClick={context.startActivity(Intent(context,MainActivity::class.java))},modifier=Modifier.fillMaxWidth()){Text("Перейти к шахматам")}
   Text("Фото обрезается по центру до квадрата. Оно не отправляется на сервер. Отображение этого профиля в партии и передача другу по Bluetooth — следующий этап.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
   if(status.isNotEmpty()) Text(status,style=MaterialTheme.typography.bodyMedium)
  }
 }
 if(remove) AlertDialog(onDismissRequest={remove=false},title={Text("Убрать аватар?")},text={Text("Удалится только копия в NearChess. Исходное фото останется в галерее.")},confirmButton={TextButton(onClick={if(!file.exists() || file.delete()){revision++; status="Аватар убран"} else status="Не удалось убрать аватар"; remove=false}){Text("Убрать")}},dismissButton={TextButton(onClick={remove=false}){Text("Отмена")}})
}
