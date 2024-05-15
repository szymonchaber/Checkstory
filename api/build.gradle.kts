plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
    id("library-config")
    id("kotlinx-serialization")
}

android {

    namespace = "dev.szymonchaber.checkstory.api"
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }

    hilt {
        enableAggregatingTask = true
    }
    sourceSets {
        getByName("androidTest") {
            assets.srcDir("$projectDir/schemas")
        }
    }

}

dependencies {
    implementation(project(":common"))
    implementation(project(":domain"))

    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.bundles.common)
    implementation(libs.bundles.ui)

    ksp(libs.hilt.compiler)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.auth)

    implementation(libs.bundles.ktor)

    implementation(libs.work)
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)

    testImplementation(libs.bundles.unitTest)
    androidTestImplementation(libs.bundles.uiTest)
}
