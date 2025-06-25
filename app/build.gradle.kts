plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}


android {
    namespace = "com.example.eczadolabim"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.eczadolabim"
        minSdk = 24
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

    // buildFeatures bloğunu bu şekilde ekle
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
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
    // --- BİZİM EKLEYECEKLERİMİZ BURADAN BAŞLIYOR ---

    // ViewModel and LiveData (MVVM Mimarisi için)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.3")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.3")
    implementation("androidx.activity:activity-ktx:1.9.0")

    // Room (Yerel Veritabanı için)
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    // Kotlin Annotation Processing (KSP) -> Room'u Kotlin ile daha verimli kullanmak için
    ksp("androidx.room:room-compiler:2.6.1")
    // Room'un Coroutines (asenkron işlemler) ile entegrasyonu için
    implementation("androidx.room:room-ktx:2.6.1")

    // Kotlin Coroutines (Arka plan işlemleri için)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.coil)
    implementation(libs.androidx.preference.ktx)
}