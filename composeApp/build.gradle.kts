import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.ExperimentalComposeLibrary

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
}

configurations.implementation {
    exclude(group = "org.jetbrains", module = "annotations")
}

kotlin {
    android {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    sourceSets {
        val androidMain by getting {
            dependencies {
                implementation(compose.preview)
                implementation(libs.androidx.activity.compose)
            }
        }

        @OptIn(ExperimentalComposeLibrary::class)
        val commonMain by getting {
            dependencies {
                implementation(project(":core"))
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.preview)
                implementation(libs.androidx.lifecycle.viewmodel)
                implementation(libs.androidx.lifecycle.runtime.compose)
            }
        }
    }
}

android {
    namespace = "org.example.project"
    compileSdk = 34

    defaultConfig {
        applicationId = "org.example.project"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    packagingOptions {
        resources.excludes.add("kotlin/reflect/reflect.kotlin_builtins")
        resources.excludes.add("kotlin/internal/internal.kotlin_builtins")
        resources.excludes.add("kotlin/kotlin.kotlin_builtins")
        resources.excludes.add("kotlin/coroutines/coroutines.kotlin_builtins")
        resources.excludes.add("kotlin/ranges/ranges.kotlin_builtins")
        resources.excludes.add("kotlin/collections/collections.kotlin_builtins")
        resources.excludes.add("kotlin/annotation/annotation.kotlin_builtins")
        // 如果需要排除更多重复资源，可以继续添加
        excludes.addAll(listOf(
            "META-INF/DEPENDENCIES",
            "META-INF/LICENSE",
            "META-INF/LICENSE.txt",
            "META-INF/NOTICE",
            "META-INF/NOTICE.txt",
            "META-INF/ASL2.0"
        ))
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildToolsVersion = "34.0.0"
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.8.22")
    debugImplementation(compose.uiTooling)
    implementation(compose.ui)
    implementation(compose.preview) // 引入预览功能
}