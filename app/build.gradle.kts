import dev.szymonchaber.checkstory.gradle.Dependencies

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("dependencies")
    id("com.google.devtools.ksp") version "1.8.20-1.0.11"
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")
}

android {

    namespace = "dev.szymonchaber.checkstory"

    compileSdk = 33

    defaultConfig {
        applicationId = "dev.szymonchaber.checkstory"
        minSdk = 24
        targetSdk = 31
        versionCode = 46
        versionName = "1.6.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
            resValue("string", "app_name", "Devstory")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            resValue("string", "app_name", "Checkstory")
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.6"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    hilt {
        enableAggregatingTask = true
    }
    applicationVariants.forEach { variant ->
        kotlin.sourceSets {
            getByName(variant.name) {
                kotlin.srcDir("build/generated/ksp/${variant.name}/kotlin")
            }
        }
    }
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.2")

    implementation(project(":common"))
    implementation(project(":design"))
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(project(":navigation"))
    implementation(project(":checklist:catalog"))
    implementation(project(":checklist:template"))
    implementation(project(":checklist:fill"))
    implementation(project(":checklist:history"))
    implementation(project(":notifications"))
    implementation(project(":payments"))
    implementation(project(":about"))
    implementation(project(":onboarding"))
    implementation(project(":account"))

    Dependencies.common.forEach(::implementation)
    Dependencies.ui.forEach(::implementation)

    Dependencies.composeDestinations.forEach(::implementation)
    ksp(Dependencies.composeDestinationsKsp)

    implementation(Dependencies.hiltWork)
    kapt(Dependencies.hiltWorkKapt)
    kapt(Dependencies.hiltKapt)

    implementation(Dependencies.ads)
    implementation(Dependencies.review)

    implementation(platform(Dependencies.firebasePlatform))
    implementation(Dependencies.analytics)
    implementation(Dependencies.crashlytics)
    implementation(Dependencies.performance)
    implementation(Dependencies.messaging)

    debugImplementation(Dependencies.debugUiTooling)
    Dependencies.unitTest.forEach(::testImplementation)
    Dependencies.uiTest.forEach(::androidTestImplementation)
}
