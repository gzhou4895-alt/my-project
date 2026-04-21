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
}

dependencies {
    // 显式引入本地 AAR 文件
    implementation(files("libs/litertlm-android-0.10.2.aar")) 
    
    implementation("org.nanohttpd:nanohttpd:2.3.1")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
}

// 终极兼容方案：直接操作编译器原始参数，绕过所有 DSL 限制
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    @Suppress("DEPRECATION")
    kotlinOptions {
        // 将 jvm-target 作为原始参数传入，避开对 .jvmTarget 属性的直接赋值检查
        freeCompilerArgs = freeCompilerArgs + listOf(
            "-jvm-target", "17",
            "-Xskip-metadata-version-check" // 强制跳过 AAR 库的 Kotlin 元数据版本冲突
        )
    }
}
