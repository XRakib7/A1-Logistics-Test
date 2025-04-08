plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.a1logisticstest1"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.a1logisticstest1"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.1.0.1"

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
    implementation("org.apache.poi:poi:5.2.2")
    implementation("org.apache.poi:poi-ooxml:5.2.2")
    implementation("com.google.code.gson:gson:2.8.8")
    implementation ("com.firebaseui:firebase-ui-firestore:8.0.2")
    implementation ("androidx.recyclerview:recyclerview:1.3.2")
    implementation ("androidx.media:media:1.6.0")
    implementation("com.google.android.gms:play-services-base:18.2.0") // Usually required
    implementation("androidx.appcompat:appcompat:1.6.1") // or the latest version
    implementation ("com.airbnb.android:lottie:6.1.0") // Latest stable version



    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.messaging)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}