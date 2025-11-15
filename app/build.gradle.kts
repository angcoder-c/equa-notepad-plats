import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.equa_notepad_plats"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.equa_notepad_plats"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // propiedades
        val properties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            properties.load(FileInputStream(localPropertiesFile))
        }

        // BuildConfig
        buildConfigField(
            "String",
            "WEB_CLIENT_ID",
            "\"${properties.getProperty("WEB_CLIENT_ID", "")}\""
        )
        buildConfigField(
            "String",
            "WEB_CLIENT_SECRET",
            "\"${properties.getProperty("WEB_CLIENT_SECRET", "")}\""
        )
        buildConfigField(
            "String",
            "ANDROID_CLIENT_ID",
            "\"${properties.getProperty("ANDROID_CLIENT_ID", "")}\""
        )
        buildConfigField(
            "String",
            "SUPABASE_URL",
            "\"${properties.getProperty("SUPABASE_URL", "")}\""
        )
        buildConfigField(
            "String",
            "SUPABASE_API_KEY",
            "\"${properties.getProperty("SUPABASE_API_KEY", "")}\""
        )

        buildConfigField(
            "String",
            "OPEN_ROUTER_KEY",
            "\"${properties.getProperty("OPEN_ROUTER_KEY", "")}\""
        )

        manifestPlaceholders["supabaseAuthCallback"] =
            properties.getProperty("SUPABASE_AUTH_CALLBACK_URL", "com.example.equa_notepad_plats://login")
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

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("com.google.android.material:material:1.12.0")

    // google oauth and credentials
    implementation("androidx.credentials:credentials:1.5.0-beta01")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0-beta01")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // supabase
    implementation(platform("io.github.jan-tennert.supabase:bom:3.0.2"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:auth-kt")
    implementation("io.github.jan-tennert.supabase:realtime-kt")
    implementation("io.ktor:ktor-client-okhttp:3.0.2")
    implementation("io.ktor:ktor-client-core:3.0.2")
    implementation("io.ktor:ktor-client-content-negotiation:3.0.2")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.2")
    implementation("io.github.jan-tennert.supabase:compose-auth")

    // splash screen
    implementation("androidx.core:core-splashscreen:1.0.1")

    // viewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    // coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // coil async image
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.androidx.runtime)
    ksp(libs.room.compiler)
    implementation("androidx.credentials:credentials:1.6.0-beta03")
    implementation("androidx.credentials:credentials-play-services-auth:1.6.0-beta03")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.text.google.fonts)
    implementation(libs.androidx.compose.foundation.layout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation("com.google.android.gms:play-services-auth:21.2.0")
}