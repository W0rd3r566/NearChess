import java.net.URI

plugins {
 id("com.android.application")
 id("org.jetbrains.kotlin.android")
 id("org.jetbrains.kotlin.plugin.compose")
}
val pieceAssets = layout.buildDirectory.dir("generated/pieceAssets")
val preparePieces by tasks.registering {
 outputs.dir(pieceAssets)
 doLast {
  val root=pieceAssets.get().asFile
  val revision="87e973ad65489975caf55c440537ea317f40a242"
  val base="https://raw.githubusercontent.com/lichess-org/lila/$revision/"
  fun fetch(url:String,path:String) {
   val file=root.resolve(path); file.parentFile.mkdirs()
   val connection=URI(url).toURL().openConnection()
   connection.connectTimeout=30000; connection.readTimeout=30000
   connection.getInputStream().use { input -> file.outputStream().use {input.copyTo(it)} }
  }
  for(set in listOf("cburnett","chessnut","spatial")) for(side in "wb") for(piece in "KQRBNP") {
   val path="piece/$set/$side$piece.svg"
   fetch(base+"public/"+path,path)
   require(root.resolve(path).readText().contains("<svg")) {"Invalid SVG: $path"}
  }
  fetch(base+"COPYING.md","licenses/lila-COPYING.md")
  fetch("https://raw.githubusercontent.com/LexLuengas/chessnut-pieces/master/LICENSE.txt","licenses/chessnut-APACHE-2.0.txt")
  fetch("https://raw.githubusercontent.com/maurimo/chess-art/main/LICENSE","licenses/spatial-MIT.txt")
  fetch("https://raw.githubusercontent.com/lichess-org/chessground/master/LICENSE","licenses/chessground-GPL-3.0.txt")
 }
}
android {
 namespace = "dev.nearchess"
 compileSdk = 35
 defaultConfig { applicationId = "dev.nearchess"; minSdk = 26; targetSdk = 35; versionCode = 2; versionName = "0.2.0-dev" }
 sourceSets.getByName("main").assets.srcDir(pieceAssets)
 buildFeatures { compose = true }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 kotlinOptions { jvmTarget = "17" }
}
tasks.named("preBuild").configure { dependsOn(preparePieces) }
dependencies {
 implementation(platform("androidx.compose:compose-bom:2024.12.01"))
 implementation("androidx.activity:activity-compose:1.9.3")
 implementation("androidx.compose.material3:material3")
 implementation("androidx.compose.material:material-icons-extended")
 implementation("androidx.compose.ui:ui")
 implementation("androidx.compose.foundation:foundation")
 implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
 implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
 implementation("com.caverock:androidsvg-aar:1.4")
 testImplementation("junit:junit:4.13.2")
}
