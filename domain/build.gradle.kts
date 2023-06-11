plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
    id("dependencies")
    id("library-config")
}

android {
    namespace = "dev.szymonchaber.checkstory.domain"
    hilt {
        enableAggregatingTask = true
    }
    libraryVariants.all {
        kotlin.sourceSets {
            getByName(name) {
                kotlin.srcDir("build/generated/ksp/$name/kotlin")
            }
        }
    }
}

dependencies {
    implementation(libs.bundles.common)
    implementation(libs.bundles.ui)

    kapt(libs.hilt.compiler)

    testImplementation(libs.bundles.unitTest)
}
