import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    kotlin("android")
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
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

/**
 * Kotlin 2.x / Gradle 8 推荐写法
 */
kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation("org.tensorflow:tensorflow-lite:2.14.0")

    // =========================
    // 基础 Android
    // =========================
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")

    // =========================
    // LiteRT（TensorFlow Lite）
    // =========================
    implementation("org.tensorflow:tensorflow-lite:2.14.0")

    // 可选：GPU 加速（后面再用）
    implementation("org.tensorflow:tensorflow-lite-gpu:2.14.0")

    // =========================
    // Kotlin 协程（后面做推理异步用）
    // =========================
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
