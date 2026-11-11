package dev.nearchess

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable internal fun SourceCodeLink(){
 val uri=LocalUriHandler.current
 var failed by remember{mutableStateOf(false)}
 OutlinedButton(
  onClick={failed=runCatching{uri.openUri("https://github.com/W0rd3r566/NearChess")}.isFailure},
  modifier=Modifier.fillMaxWidth().heightIn(min=48.dp)
 ){
  Icon(painterResource(R.drawable.ic_github),contentDescription=null,modifier=Modifier.size(22.dp))
  Spacer(Modifier.width(10.dp))
  Text("Исходный код")
 }
 if(failed)Text("Не удалось открыть браузер. Репозиторий: github.com/W0rd3r566/NearChess",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.error)
}
