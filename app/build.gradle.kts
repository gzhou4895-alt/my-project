plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // Ktor 序列化插件
    kotlin("plugin.serialization") version "1.9.0"
}

android {
    // 必须与 Manifest 中的 package 一致
    namespace = "com.example.hello"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.hello"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    // 🛠️ 核心修复：强制指定 Manifest 路径，确保权限配置被读取
    sourceSets {
        getByName("main") {
            manifest.srcFile("src/main/AndroidManifest.xml")
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/INDEX.LIST"
            excludes += "/META-INF/io.netty.versions.properties"
            excludes += "/META-INF/okio.kotlin_module"
        }
        // 允许直接映射大文件，提高加载速度
        jniLibs.useLegacyPackaging = true
    }

    // 🛠️ 核心修复：禁止压缩模型文件，否则 MediaPipe 无法直接读取 2.58GB 的大模型
    @Suppress("DEPRECATION")
    aaptOptions {
        noCompress("tflite", "litertlm", "bin", "model")
    }
}

dependencies {
    // Android 核心支持库
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // 🔥 MediaPipe LLM 推理核心 (运行 Gemma 模型必备)
    implementation("com.google.mediapipe:tasks-genai:0.10.14")

    // 🌐 Ktor 相关依赖
    val ktor_version = "2.3.7"
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")

    // 测试
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
