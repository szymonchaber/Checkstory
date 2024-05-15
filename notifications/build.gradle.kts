plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
    id("library-config")
}

android {
    namespace = "dev.szymonchaber.checkstory.notifications"
    hilt {
        enableAggregatingTask = true
    }
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

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.cloud.messaging)

    implementation(libs.work)

    ksp(libs.hilt.compiler)

    testImplementation(libs.bundles.unitTest)
    androidTestImplementation(libs.bundles.uiTest)
}
