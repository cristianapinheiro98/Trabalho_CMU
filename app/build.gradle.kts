plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services") // Firebase plugin
    id("kotlin-kapt")
}

android {
    namespace = "pt.ipp.estg.trabalho_cmu"
    compileSdk = 36

    defaultConfig {
        applicationId = "pt.ipp.estg.trabalho_cmu"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "GOOGLE_PLACES_API_KEY", "\"${project.findProperty("GOOGLE_PLACES_API_KEY") ?: ""}\"")
        manifestPlaceholders["GOOGLE_MAPS_API_KEY"] = project.findProperty("GOOGLE_PLACES_API_KEY") ?: ""
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
        jniLibs {
            useLegacyPackaging = false
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom)) //BOM (Bill of materials)

    //Compose UI
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3) // Material design
    implementation("androidx.compose.material:material-icons-extended-android:1.7.8")

    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.10.0")
    implementation(libs.androidx.adapters)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundation.layout)

    //Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:34.6.0"))

    // Firebase products
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-messaging")

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.9.6")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    implementation("com.squareup.okhttp3:logging-interceptor:5.3.2")
    implementation("androidx.compose.runtime:runtime-livedata:1.10.0")

    // Room
    implementation("androidx.room:room-runtime:2.8.4")
    implementation("androidx.room:room-ktx:2.8.4")
    kapt("androidx.room:room-compiler:2.8.3")

    // LiveData
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.10.0")
    implementation("androidx.compose.runtime:runtime-livedata")

    // Coil (Load images)
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.3.0")

    // Google Play Services - Location (GPS)
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Google Maps Compose
    implementation("com.google.maps.android:maps-compose:6.12.2")
    implementation("com.google.android.gms:play-services-maps:19.2.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2")

    // ViewModel Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")

    // Runtime Compose
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")

    // Accompanist (Permissions helper)
    implementation("com.google.accompanist:accompanist-permissions:0.37.3")

    // Responsive (phone + tablet)
    implementation("androidx.compose.material3:material3-window-size-class:1.4.0")

    // Google Places API
    implementation("com.google.android.libraries.places:places:5.1.1")

    // Parsing JSON from Places API
    implementation("com.google.code.gson:gson:2.13.2")

    //Cloudinary
    implementation("com.cloudinary:cloudinary-android:3.1.2")

    // Konfetti (animations)
    implementation("nl.dionsegijn:konfetti-compose:2.0.5")
}