// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.google.devtools.ksp) apply false
  alias(libs.plugins.roborazzi) apply false
  alias(libs.plugins.secrets) apply false
}

abstract class DownloadFilesTask : DefaultTask() {
    @TaskAction
    fun downloadFiles() {
        val baseUrl = "https://raw.githubusercontent.com/zahid5656/crypto_oracle_nexus/phase1-oracle-nexus-upgrade"
        fun download(path: String) {
            val dest = project.file(path)
            dest.parentFile.mkdirs()
            val url = java.net.URL("$baseUrl/$path")
            val conn = url.openConnection() as java.net.HttpURLConnection
            if (conn.responseCode == 200) {
                dest.writeBytes(conn.inputStream.readBytes())
                println("Downloaded: $path")
            } else {
                println("Failed to download: $path")
            }
        }
        
        download("app/src/main/java/com/example/model/CryptoData.kt")
        download("app/src/main/java/com/example/ui/theme/Color.kt")
        download("app/src/main/java/com/example/ui/theme/Theme.kt")
        download("app/src/main/java/com/example/ui/theme/Type.kt")
        download("app/src/main/res/values/strings.xml")
        download("app/src/main/res/drawable/ic_app_logo_ai.xml")
        download("app/src/main/res/drawable/ic_launcher_background.xml")
        download("app/src/main/res/drawable/ic_launcher_foreground.xml")
        download("app/src/main/res/drawable/ic_crypto_oracle_logo_1780253119065.xml")
    }
}
tasks.register<DownloadFilesTask>("downloadAll")

tasks.register<Exec>("pullGit") {
    commandLine("git", "pull")
}
tasks.register<Exec>("cleanGit") {
    commandLine("git", "clean", "-fd")
}
