plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
    id("com.google.firebase.appdistribution")
}

android {
    namespace = "com.example.a1logisticstest1"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.a1logisticstest1"
        minSdk = 28
        compileSdk = 35
        targetSdk = 35
        versionCode = 3
        versionName = "1.1.0.3"
//gh release create v1.1.0.2 "app/release/A1 Logistics.apk" --title "Version 1.1.0.2" --notes "Initial release with basic functionality"
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
    implementation("com.google.android.gms:play-services-base:18.6.0") // Usually required
    implementation("androidx.appcompat:appcompat:1.6.1") // or the latest version
    implementation ("com.airbnb.android:lottie:6.1.0") // Latest stable version
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    // https://mvnrepository.com/artifact/com.google.android.gms/play-services-auth-api-phone
    implementation("com.google.android.gms:play-services-auth-api-phone:18.2.0")
    implementation ("com.google.android.material:material:1.6.0")// or higher
    implementation ("com.google.firebase:firebase-storage:20.3.0")  // For APK hosting
    implementation ("com.google.firebase:firebase-appdistribution:16.0.0-beta10")  // For update checks

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
    implementation(libs.firebase.functions)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}