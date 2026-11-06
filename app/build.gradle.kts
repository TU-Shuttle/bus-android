plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

// .env
val envFile = rootProject.file(".env")
val envVars = mutableMapOf<String, String>()

if (envFile.exists()) {
    envFile.readLines().forEach { line ->
        val trimmed = line.trim()
        if (trimmed.isNotEmpty() && !trimmed.startsWith("#") && trimmed.contains("=")) {
            val (key, value) = trimmed.split("=", limit = 2)
            envVars[key.trim()] = value.trim()
        }
    }
}

fun getEnv(key: String): String {
    return envVars[key] ?: System.getenv(key) ?: ""
}

fun String.withTrailingSlash(): String = if (endsWith('/')) this else "$this/"

val configuredBaseUrl = getEnv("BASE_URL")
// debug 빌드는 에뮬레이터 기본값을 허용하고, release 빌드는 명시한 BASE_URL만 사용한다.
val debugBaseUrl = configuredBaseUrl.ifBlank { "http://10.0.2.2:8080/" }.withTrailingSlash()
val releaseBaseUrl = configuredBaseUrl.withTrailingSlash()

gradle.taskGraph.whenReady {
    val buildsRelease = allTasks.any { it.project == project && it.name.contains("Release") }
    if (buildsRelease && configuredBaseUrl.isBlank()) {
        throw GradleException("release 빌드에는 BASE_URL(.env 또는 환경변수)이 필요합니다.")
    }
}

// 릴리즈 서명: .env 또는 환경변수에 RELEASE_STORE_FILE 등을 넣으면 서명한다. 없으면 서명 없는 release가 나온다.
val releaseStoreFile = getEnv("RELEASE_STORE_FILE")

android {
    namespace = "com.tukorea.bus"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.tukorea.bus"
        minSdk = 25
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["naverMapClientId"] = getEnv("NAVER_MAP_CLIENT_ID")
    }

    signingConfigs {
        if (releaseStoreFile.isNotBlank()) {
            create("release") {
                storeFile = file(releaseStoreFile)
                storePassword = getEnv("RELEASE_STORE_PASSWORD")
                keyAlias = getEnv("RELEASE_KEY_ALIAS")
                keyPassword = getEnv("RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            buildConfigField("String", "BASE_URL", "\"$debugBaseUrl\"")
        }
        release {
            // 코드 축소·난독화. 서버와 주고받는 JSON 클래스는 proguard-rules.pro에서 지킨다.
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.findByName("release")
            buildConfigField("String", "BASE_URL", "\"$releaseBaseUrl\"")
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
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.annotation)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.runtime)
    implementation(libs.compose.foundation)
    implementation(libs.compose.activity)
    implementation(libs.compose.viewmodel)
    implementation(libs.compose.runtime.livedata)
    implementation(libs.compose.navigation)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.accompanist.permissions)
    debugImplementation(libs.compose.ui.tooling)

    // Retrofit
    implementation(libs.retrofit)

    // Gson Converter (JSON 직렬화/역직렬화용)
    implementation(libs.converter.gson)

    // OkHttp Logging (네트워크 로그 디버깅용, 선택 사항)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    // Kotlin + Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)

    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    /* Hilt */
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    /* SplashScreen */
    implementation(libs.androidx.core.splashscreen)

    /* Naver Map SDK */
    implementation(libs.naver.map.sdk)

    /* Google Play Services Location */
    implementation(libs.play.services.location)
}
