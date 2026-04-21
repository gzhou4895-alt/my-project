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

        versionCode = (System.currentTimeMillis() / 1000).toInt()
        versionName = "1.0.${System.currentTimeMillis()}"
    }

    // ✅ 必须在 android {} 里面
    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    // ✅ 也必须在 android {} 里面
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // ✅ Kotlin 新DSL（你之前踩过坑的地方）
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.22")

    // LiteRT（先保留基础）
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
}
