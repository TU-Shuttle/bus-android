import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

android {
    namespace = "com.tukorea.bus"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.tukorea.bus"
        minSdk = 25
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val naverMapClientId = localProperties.getProperty("naver.map.client.id") ?: ""
        manifestPlaceholders["naverMapClientId"] = naverMapClientId
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Retrofit
    implementation(libs.retrofit)

    // Gson Converter (JSON 직렬화/역직렬화용)
    implementation(libs.converter.gson)

    // OkHttp Logging (네트워크 로그 디버깅용, 선택 사항)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    // Kotlin + Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    /* Hilt */
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    /* Fragment */
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.activity.ktx)

    /* SplashScreen */
    implementation(libs.androidx.core.splashscreen)

    /* Naver Map SDK */
    implementation(libs.naver.map.sdk)

    /* Google Play Services Location */
    implementation(libs.play.services.location)
}
