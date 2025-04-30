plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
}

android {
    namespace = "com.aether.core.runtime"
    compileSdk = 34

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.8" // This version works with Kotlin 1.8.22
    }
//    android {
//        composeOptions {
//            kotlinCompilerExtensionVersion = "1.5.15"
//        }
//    }
//    defaultConfig {
//        minSdk = 34
//        targetSdk = 34
//        versionCode = 1
//        versionName = "1.0"
//
//        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
//    }


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
}

configurations.implementation {
    exclude(group = "com.intellij", module = "annotations")
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Kotlin 标准库
//    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.8.20")
    // AndroidX Core KTX
    implementation("androidx.core:core-ktx:1.3.1")
    // AndroidX AppCompat
    implementation("androidx.appcompat:appcompat:1.2.0")
    // Material Design 组件
//    implementation("com.google.android.material:material:1.2.1")
    // ConstraintLayout
    implementation("androidx.constraintlayout:constraintlayout:2.0.1")
    // JUnit 测试框架
//    testImplementation("junit:junit:4.13.2")
    // AndroidX 测试扩展 JUnit
//    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    // Espresso 测试框架
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
    // Kotlin 编译器
    implementation("org.jetbrains.kotlin:kotlin-compiler:1.8.20") {
        exclude(group = "com.google.guava", module = "guava")
    }

    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.8.22")
    implementation("androidx.compose.ui:ui:1.6.0")
    implementation("androidx.compose.material:material:1.6.0")
//    implementation("androidx.compose.runtime:runtime:1.6.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
//    implementation("com.google.firebase:firebase-inappmessaging-display:17.2.0")
//    implementation("com.google.guava:guava:27.0.1-android")
//    implementation("com.google.guava:guava:27.0.1-jre")
    implementation("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")
//    implementation("com.google.code.gson:gson:2.8.8")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.14.2")
}