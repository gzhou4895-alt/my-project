android {
    namespace = "com.example.hello"
    compileSdk = 34 // 2. 建议升级到 34 以更好地支持 Java 21

    defaultConfig {
        applicationId = "com.example.hello"
        minSdk = 26
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        // 3. 将 Kotlin 目标也对齐到 21，防止版本冲突
        jvmTarget = "21"
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.code.gson:gson:2.10.1")
    // 这里是你那个报错的库，确保它被正确引入
}
