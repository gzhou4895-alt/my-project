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
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    // 注意：这里删除了会导致报错的 kotlinOptions 块，改用文件末尾的 tasks.withType
}

dependencies {
    // 显式引入 AAR
    implementation(files("libs/litertlm-android-0.10.2.aar")) 
    
    implementation("org.nanohttpd:nanohttpd:2.3.1")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
}

// 解决 Kotlin 2.0 编译器严格检查和元数据版本冲突的终极方案
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        @Suppress("DEPRECATION")
        this.jvmTarget = "17"
        // 关键：强制跳过“metadata 2.3.0”版本不匹配的错误
        freeCompilerArgs.add("-Xskip-metadata-version-check")
    }
}
