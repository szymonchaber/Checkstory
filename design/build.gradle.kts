import dev.szymonchaber.checkstory.gradle.Dependencies

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("dependencies")
    id("library-config")
}

android {
    namespace = "dev.szymonchaber.checkstory.design"

    buildTypes {
        debug {
            buildConfigField("String", "BANNER_AD_UNIT_ID", "\"ca-app-pub-3940256099942544/6300978111\"")
        }
        release {
            buildConfigField("String", "BANNER_AD_UNIT_ID", "\"ca-app-pub-3940256099942544/6300978111\"")
        }
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
    implementation(project(mapOf("path" to ":domain")))
    Dependencies.common.forEach(::implementation)
    Dependencies.ui.forEach(::implementation)

    Dependencies.composeDestinations.forEach(::implementation)

    kapt(Dependencies.hiltKapt)

    implementation(Dependencies.ads)

    debugImplementation(Dependencies.debugUiTooling)
    Dependencies.unitTest.forEach(::testImplementation)
    Dependencies.uiTest.forEach(::androidTestImplementation)
}
