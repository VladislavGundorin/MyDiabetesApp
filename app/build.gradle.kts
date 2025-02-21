plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    kotlin("kapt")
}

android {
    namespace = "com.example.mydiabetesapp"
    compileSdk = 35

    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        applicationId = "com.example.mydiabetesapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    buildToolsVersion = "35.0.0"
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
    //я добавил!
    implementation("com.google.android.material:material:1.11.0")

    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1") // Компилятор для Room
    implementation("androidx.room:room-ktx:2.6.1") // Поддержка Kotlin Coroutines (Flow)

    // Jetpack ViewModel и LiveData (если понадобится)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")

    // Flow и Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3") // Основная библиотека Flow
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3") // Для работы в Android
    // Dependency Injection (если будешь использовать)
    implementation("androidx.hilt:hilt-lifecycle-viewmodel:1.0.0-alpha03") // Hilt (если используешь)
    kapt("androidx.hilt:hilt-compiler:1.0.0")
}