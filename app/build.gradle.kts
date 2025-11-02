import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

// Leer la clave de API desde local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

android {
    namespace = "com.zulian.ayudafinanzas"
    compileSdk = 36 // ACTUALIZADO

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.zulian.ayudafinanzas"
        minSdk = 24
        targetSdk = 36 // ACTUALIZADO
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Exponer la clave de API como un campo de BuildConfig
        buildConfigField("String", "GEMINI_API_KEY", "\"${localProperties.getProperty("GEMINI_API_KEY")}\"")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Retrofit & OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")

    // Jsoup for Web Scraping
    implementation("org.jsoup:jsoup:1.17.2")

    // Glide for Image Loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
    
    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // ML Kit - Eliminado para reemplazarlo con Gemini

    // Gson
    implementation("com.google.code.gson:gson:2.10.1")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
