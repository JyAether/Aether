plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp") version "1.8.22-1.0.11"
}

// 确保处理器可以作为工件发布
sourceSets.main {
    resources {
        srcDir("src/main/resources")
    }
}

dependencies {
    implementation(project(":annotations"))
    implementation("com.google.devtools.ksp:symbol-processing-api:1.8.22-1.0.11")
    implementation("com.squareup:kotlinpoet:1.14.2")
    implementation("com.squareup:kotlinpoet-ksp:1.14.2")
}