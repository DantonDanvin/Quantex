plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.quantex"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.quantex"
        minSdk = 26
        targetSdk = 34
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

    viewBinding{
        enable = true
    }
}

dependencies {

    implementation ("androidx.core:core-ktx:1.12.0")
    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation ("com.google.android.material:material:1.11.0")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation ("junit:junit:4.13.2")
    androidTestImplementation ("androidx.test.ext:junit:1.1.5")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.5.1")

    // Lifecycle - for lifecycleScope in Activities & Fragments
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // Coil for image loading (SVG + PNG) - replaces Glide & Picasso
    implementation ("io.coil-kt:coil:2.6.0")
    implementation ("io.coil-kt:coil-svg:2.6.0")

    // Firebase (BOM manages ALL versions)
    implementation (platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation ("com.google.firebase:firebase-analytics")
    implementation ("com.google.firebase:firebase-auth")
    implementation ("com.google.firebase:firebase-database")
    implementation ("com.google.firebase:firebase-firestore")

    // Chart
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Shimmer effect
    implementation ("com.facebook.shimmer:shimmer:0.5.0")

    // News - Picasso still needed for NewsHAdapter/NewsVAdapter image loading
    implementation ("com.squareup.picasso:picasso:2.8")
    // News API
    implementation ("com.github.KwabenBerko:News-API-Java:1.0.2")

    // Retrofit + Coroutines
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.google.code.gson:gson:2.10.1")

    // SwipeRefreshLayout
    implementation ("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
}