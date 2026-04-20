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
        // 核心：支持 Java 21
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
        // 关键补充：强制忽略库与编译器之间的 Kotlin 元数据版本不一致问题
        freeCompilerArgs = freeCompilerArgs + listOf(
            "-Xskip-metadata-version-check",
            "-Xallow-jvm-ir-dependencies"
        )
    }
}

dependencies {
    // 1. Android 基础 UI 与工具库
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("com.google.code.gson:gson:2.10.1")

    // 2. 网络服务器库 (用于 Adapter 通信)
    implementation("org.nanohttpd:nanohttpd:2.3.1")

    // 3. 引用本地 libs 文件夹下的 AAR 文件
    // 它会自动加载 app/libs/litertlm-android-0.10.2.aar
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
}
