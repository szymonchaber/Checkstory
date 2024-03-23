plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("dagger.hilt.android.plugin")
    id("library-config")
    id("com.google.devtools.ksp")
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
    testImplementation(project(":test"))
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.bundles.common)
    implementation(libs.bundles.ui)

    ksp(libs.hilt.compiler)

    testImplementation(libs.bundles.unitTest)
}
