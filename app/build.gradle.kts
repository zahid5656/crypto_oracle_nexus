import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.devtools.ksp)
}

android {
    namespace = "com.example"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.titancryptoraclenexus.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        buildConfigField("String", "GEMINI_API_KEY", "\"${System.getenv("GEMINI_API_KEY") ?: "MY_GEMINI_API_KEY"}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        buildConfig = true
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    
    // Navigation
    implementation(libs.androidx.navigation.compose)
    
    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    
    // Network & Serialization
    implementation(libs.retrofit)
    implementation(libs.converter.moshi)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation(libs.moshi.kotlin)
    ksp(libs.moshi.kotlin.codegen)
    
    // Coil (Images)
    implementation(libs.coil.compose)
    
    // Local Testing
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
}

abstract class CopyApkToDestinationsTask : DefaultTask() {
    @get:InputFile
    abstract val sourceApk: RegularFileProperty

    @get:OutputDirectory
    abstract val rootDestDir: DirectoryProperty

    @get:OutputDirectory
    abstract val buildOutputsDestDir: DirectoryProperty

    @TaskAction
    fun copy() {
        val src = sourceApk.get().asFile
        if (src.exists()) {
            val rootDest = rootDestDir.file("app-debug.apk").get().asFile
            val buildOutputsDest = buildOutputsDestDir.file("app-debug.apk").get().asFile
            
            buildOutputsDest.parentFile.mkdirs()
            src.copyTo(rootDest, overwrite = true)
            src.copyTo(buildOutputsDest, overwrite = true)
            println("APK copied successfully to $rootDest and $buildOutputsDest")
        } else {
            println("Source APK does not exist: ${src.absolutePath}")
        }
    }
}

tasks.register<CopyApkToDestinationsTask>("copyApkToDestinations") {
    sourceApk.set(layout.buildDirectory.file("outputs/apk/debug/app-debug.apk"))
    rootDestDir.set(rootProject.layout.projectDirectory)
    buildOutputsDestDir.set(rootProject.layout.projectDirectory.dir(".build-outputs"))
}

tasks.configureEach {
    if (name == "assembleDebug") {
        finalizedBy("copyApkToDestinations")
    }
    if (name == "createDebugApkListingFileRedirect") {
        mustRunAfter("copyApkToDestinations")
    }
}

tasks.register("printApkSize") {
    doLast {
        val rootApk = file("../app-debug.apk")
        println("rootApk exists: " + rootApk.exists())
        println("rootApk size: " + rootApk.length() + " bytes")
        
        // Let's also search for all APKs in the build directory
        project.fileTree(mapOf("dir" to project.layout.buildDirectory, "include" to "**/*.apk")).forEach { apkFile ->
            println("Found APK in buildDir: ${apkFile.absolutePath}")
        }
        rootProject.fileTree(mapOf("dir" to rootProject.layout.projectDirectory, "include" to "**/*.apk")).forEach { apkFile ->
            println("Found APK in rootProject: ${apkFile.absolutePath}")
        }
    }
}
