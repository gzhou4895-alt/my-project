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

        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64")
        }
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
}dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.22")

    // LiteRT（基础推理）
    implementation("org.tensorflow:tensorflow-lite:2.14.0")

    // Material 组件库（底部导航栏、主题等）
    implementation("com.google.android.material:material:1.11.0")

    // 协程支持
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

depe

    // Material 组件库（底部导航栏、主题等）
    implementation("com.google.android.material:material:1.11.0")

    // OkHttp 用于下载模型
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.9.22"))
    implementation("com.squareup.okhttp3:okhttp:4.12.0") {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-common")
    }

    // 协程支持
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
