import java.util.Properties
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.serialization)
}

android {
    namespace = "com.example.spotted"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.spotted"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        val props = Properties().apply {
            load(rootProject.file("local.properties").inputStream())
        }
        buildConfigField("String", "SUPABASE_URL",      "\"${props["SUPABASE_URL"]}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${props["SUPABASE_ANON_KEY"]}\"")
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    compileSdk {
        version = release(36)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")
    implementation("androidx.navigation:navigation-compose:2.9.8")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.8")

    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("dev.chrisbanes.haze:haze:1.1.1")
    implementation(libs.androidx.compose.foundation)
    implementation(libs.engage.core)
    implementation("org.osmdroid:osmdroid-android:6.1.14")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.0")
    implementation(libs.androidbrowserhelper)
    implementation("androidx.activity:activity-compose:1.10.0-alpha03")
    implementation(platform("io.github.jan-tennert.supabase:bom:3.2.6"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:storage-kt")
    implementation("io.github.jan-tennert.supabase:auth-kt")
    implementation("io.github.jan-tennert.supabase:realtime-kt")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("io.insert-koin:koin-androidx-compose:4.0.2")
    implementation("io.ktor:ktor-client-okhttp:3.0.0") // o la versione coerente con il resto dei tuoi ktor-client



    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}