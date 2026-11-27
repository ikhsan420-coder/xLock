plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "id.xlock.app"
    compileSdk = 35
    defaultConfig {
        applicationId = "id.xlock.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }
    buildFeatures { buildConfig = true }
}
dependencies { implementation("androidx.biometric:biometric:1.1.0") }
