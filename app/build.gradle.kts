plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.hello"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.hello"
        minSdk = 24
        targetSdk = 34

        // 每次构建自动变版本（防止APK缓存）
        versionCode = (System.currentTimeMillis() / 1000).toInt()
        versionName = "1.0.${System.currentTimeMillis()}"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.22")

    // LiteRT（基础推理）
    implementation("org.tensorflow:tensorflow-lite:2.14.0")

    // 如果后面要GPU可以再开（先不用）
    // implementation("org.tensorflow:tensorflow-lite-gpu:2.14.0")
}
