plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services) // Required for Firebase
}

android {
    namespace = "com.example.pawalert"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.pawalert"
        minSdk = 25
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
}

dependencies {
    // AndroidX / UI
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Firebase BOM — manages all Firebase SDK versions automatically
    implementation(platform(libs.firebase.bom))

    // Firebase Authentication (Email + Phone)
    implementation(libs.firebase.auth)

    // Cloud Firestore
    implementation(libs.firebase.firestore)

    // Firebase Cloud Messaging (Push Notifications)
    implementation(libs.firebase.messaging)

    // Firebase Analytics (recommended baseline)
    implementation(libs.firebase.analytics)

    // Google Maps SDK
    implementation(libs.google.maps)

    // Google Location Services (GPS / location tracking)
    implementation(libs.google.location)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}