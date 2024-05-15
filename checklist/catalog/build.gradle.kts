plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
    id("library-config")
}

android {
    namespace = "dev.szymonchaber.checkstory.checklist.catalog"
    hilt {
        enableAggregatingTask = true
    }
}

ksp {
    arg("compose-destinations.moduleName", "home")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":domain"))
    implementation(project(":design"))
    implementation(project(":data"))
    implementation(project(":navigation"))

    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.bundles.common)
    implementation(libs.bundles.ui)

    ksp(libs.compose.destinations.ksp)

    ksp(libs.hilt.compiler)

    implementation(libs.review)
    implementation(libs.firebase.crashlytics)

    debugImplementation(libs.compose.debugUiTooling)
    testImplementation(libs.bundles.unitTest)
    androidTestImplementation(libs.bundles.uiTest)
}
