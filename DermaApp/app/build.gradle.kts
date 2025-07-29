import com.android.build.api.dsl.Packaging

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.dermaApp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.dermaApp"
        minSdk = 21
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
    androidResources {
        noCompress.add("tflite")
    }
    fun Packaging.() {
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

configurations.all {
    exclude(group = "com.google.ai.edge.litert", module = "litert-api")
}

dependencies {
    // TensorFlow Lite
    implementation(libs.tensorflow.lite.metadata)
    implementation(libs.tensorflow.lite.task.vision)
    implementation(libs.tensorflow.lite)
    implementation(libs.tensorflow.lite.gpu)
    // Image Cropper
    implementation(libs.vanniktech.android.image.cropper)
    // AndroidX Core & Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    // Jetpack Compose
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    // Coil
    implementation(libs.coil.compose)
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    // Debugging
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}