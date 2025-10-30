plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services") // Firebase plugin
    id("com.google.dagger.hilt.android") version "2.57.2" // Hilt (Injeção de dependências)
    id("kotlin-kapt") // processador de anotações para Kotlin
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

    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.6")
    implementation(libs.androidx.adapters)

    //Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:34.4.0"))

    // TODO: Add the dependencies for Firebase products
    // When using the BoM, don't specify versions in Firebase dependencies
    implementation("com.google.firebase:firebase-analytics")

    // Add the dependencies for any other desired Firebase products
    // https://firebase.google.com/docs/android/setup#available-libraries
    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth")

    // Cloud Firestore
    implementation("com.google.firebase:firebase-firestore")

    // Cloud Storage
    implementation("com.google.firebase:firebase-storage")

    // Cloud Messaging (Notificações)
    implementation("com.google.firebase:firebase-messaging")

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.9.5")

    // Hilt (Injeção de Dependências)
    implementation("com.google.dagger:hilt-android:2.57.2")
    kapt("com.google.dagger:hilt-compiler:2.57.2")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")


    // Retrofit (para Dog/Cat API)
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    implementation("com.squareup.okhttp3:logging-interceptor:5.2.1")

    // Room (Base de dados local)
    implementation("androidx.room:room-runtime:2.8.3")
    implementation("androidx.room:room-ktx:2.8.3")
    kapt("androidx.room:room-compiler:2.8.3")

    // LiveData
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.9.4")
    implementation("androidx.compose.runtime:runtime-livedata")

    // Coil (Carregar imagens)
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Google Play Services - Location (GPS)
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Google Maps Compose
    implementation("com.google.maps.android:maps-compose:6.12.1")
    implementation("com.google.android.gms:play-services-maps:19.2.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2")

    // ViewModel Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")

    // Runtime Compose
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.4")

    // Accompanist (Permissions helper)
    implementation("com.google.accompanist:accompanist-permissions:0.37.3")
}