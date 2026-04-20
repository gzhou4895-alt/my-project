plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.hello"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.hello"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
    }
}

dependencies {
    // 基础 Android 支持库
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("com.google.code.gson:gson:2.10.1")

    // 1. LiteRT 推理引擎依赖 (解决 ai.preferred 报错)
    // 根据之前日志中出现的库名补全
    implementation("ai.preferred:litertlm-android:0.10.2")

    // 2. NanoHTTPD 嵌入式服务器 (解决 fi.iki.elonen 报错)
    implementation("org.nanohttpd:nanohttpd:2.3.1")

    // 3. 自动引用 libs 目录下的所有本地 jar/aar 文件 (非常重要)
    // 如果远程仓库找不到库，确保你把相关的 .aar 放在了 app/libs 文件夹内
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
}
