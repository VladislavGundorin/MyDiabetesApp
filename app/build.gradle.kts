import org.gradle.testing.jacoco.tasks.JacocoReport
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    kotlin("kapt")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("jacoco")
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
    packagingOptions {
        resources {
            excludes += setOf(
                "META-INF/INDEX.LIST",
                "META-INF/DEPENDENCIES"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildToolsVersion = "35.0.0"
}

dependencies {
    implementation(libs.androidx.junit.ktx)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.3")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("com.google.android.material:material:1.12.0")

    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("androidx.hilt:hilt-lifecycle-viewmodel:1.0.0-alpha03")
    kapt("androidx.hilt:hilt-compiler:1.0.0")

    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    val navVersion = "2.7.2"
    implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navVersion")

    implementation("com.google.android.gms:play-services-auth:20.5.0")
    implementation("com.google.api-client:google-api-client-android:2.7.2")
    implementation("com.google.http-client:google-http-client-gson:1.42.2")
    implementation("com.google.apis:google-api-services-drive:v3-rev20250427-2.0.0")
    implementation("com.google.http-client:google-http-client-android:1.42.2")
    implementation("com.jakewharton.timber:timber:5.0.1")

    implementation(platform("com.google.firebase:firebase-bom:32.1.0"))
    implementation("com.google.firebase:firebase-crashlytics-ktx")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("io.mockk:mockk:1.13.5")
    testImplementation("androidx.arch.core:core-testing:2.2.0")

    testImplementation("junit:junit:4.13.2")

}
jacoco {
    toolVersion = "0.8.9"
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")
    group = "verification"
    description = "Generate Jacoco coverage report for debug unit tests"

    val javaClasses = fileTree("$buildDir/intermediates/javac/debug/classes") {
        exclude(
            "**/R*.class",
            "**/BuildConfig.*",
            "**/*Test*.*",
            "com/example/mydiabetesapp/feature/**/ui/**",
            "com/example/mydiabetesapp/core/**",
            "com/example/mydiabetesapp/feature/journal/**",
            "com/example/mydiabetesapp/*.class"
        )
    }
    val kotlinClasses = fileTree("$buildDir/tmp/kotlin-classes/debug") {
        exclude(
            "**/R*.class",
            "**/BuildConfig.*",
            "**/*Test*.*",
            "com/example/mydiabetesapp/feature/**/ui/**",
            "com/example/mydiabetesapp/core/**",
            "com/example/mydiabetesapp/feature/journal/**",
            "com/example/mydiabetesapp/*.class"
        )
    }
    classDirectories.setFrom(files(javaClasses, kotlinClasses))

    sourceDirectories.setFrom(files("src/main/java", "src/main/kotlin"))

    executionData.setFrom(
        fileTree(buildDir) {
            include(
                "jacoco/testDebugUnitTest.exec",
                "outputs/unit_test_code_coverage/debugUnitTest/*.exec"
            )
        }
    )
    
    reports {
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/html"))
        xml.required.set(false)
        csv.required.set(false)
    }
}