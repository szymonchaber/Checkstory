import dev.szymonchaber.checkstory.gradle.Secrets.getSecret

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("dagger.hilt.android.plugin")
    id("library-config")
    id("com.google.devtools.ksp")
}

android {
    namespace = "dev.szymonchaber.checkstory.design"

    buildTypes {
        debug {
            buildConfigField("String", "BANNER_AD_UNIT_ID", getSecret("DEBUG_BANNER_AD_UNIT_ID"))
        }
        release {
            buildConfigField("String", "BANNER_AD_UNIT_ID", getSecret("RELEASE_BANNER_AD_UNIT_ID"))
        }
    }
    buildFeatures {
        buildConfig = true
    }

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
    implementation(project(":domain"))
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.bundles.common)
    implementation(libs.bundles.ui)


    ksp(libs.hilt.compiler)

    implementation(libs.ads)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)

    debugImplementation(libs.compose.debugUiTooling)
    testImplementation(libs.bundles.unitTest)
    androidTestImplementation(libs.bundles.uiTest)
}
