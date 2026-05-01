plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // Ktor 网关解析 JSON 必须的插件
    kotlin("plugin.serialization") version "1.9.0"
}

android {
    namespace = "com.example.hello"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.hello"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    // 1. 解决 Netty/Ktor 引起的 META-INF 文件冲突（修复你刚才的报错）
    packaging {
        resources {
            excludes += "/META-INF/INDEX.LIST"
            excludes += "/META-INF/io.netty.versions.properties"
            excludes += "/META-INF/okio.kotlin_module"
        }
    }

    // 2. 防止模型文件被压缩（否则 GPU 推理引擎加载会失败）
    aaptOptions {
        noCompress("tflite", "litertlm", "bin")
    }
}

dependencies {
    // Android 核心 UI
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // 🔥 MediaPipe LLM 推理引擎 (Gemma 本地运行核心)
    implementation("com.google.mediapipe:tasks-genai:0.10.14")

    // 🌐 Ktor 服务器 (让手机变成 AI 网关)
    val ktor_version = "2.3.7"
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")

    // 测试库
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
