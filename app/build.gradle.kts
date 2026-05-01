plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
}

// 1. 定义获取北京时间的方法
def static releaseTime() {
    return new Date().format("yyyyMMdd_HHmm", TimeZone.getTimeZone("GMT+08:00"))
}

android {
    namespace 'com.example.hello'
    compileSdk 34

    defaultConfig {
        applicationId "com.example.hello"
        minSdk 24
        targetSdk 34
        versionCode 1
        versionName "1.0"

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary true
        }
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
        debug {
            minifyEnabled false
        }
    }

    // 2. 核心命名逻辑：让每次生成的 APK 文件名都带上时间戳
    applicationVariants.all { variant ->
        variant.outputs.all {
            // 最终格式示例：AI_Helper_v1.0_20260501_2130_debug.apk
            // 注意：文件名建议尽量使用英文/数字，防止某些系统环境下出现乱码
            outputFileName = "AI_Helper_v${variant.versionName}_${releaseTime()}_${variant.buildType.name}.apk"
        }
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = '1.8'
    }

    buildFeatures {
        compose false
    }
}

dependencies {
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.7.0'
    implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0'
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
}
