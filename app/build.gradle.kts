import dev.szymonchaber.checkstory.gradle.Secrets.getSecret

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")
    id("secrets-injection")
}

android {

    namespace = "dev.szymonchaber.checkstory"
    compileSdk = 34

    defaultConfig {
        applicationId = "dev.szymonchaber.checkstory"
        minSdk = 24
        targetSdk = 33
        versionCode = 53
        versionName = "2.0.0"

        resourceConfigurations.add("en")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    defaultConfig {
        resValue("string", "ads_application_id", getSecret("ADS_APPLICATION_ID"))
    }

    signingConfigs {
        create("release") {
            storeFile = file("/Users/szymonchaber/app_signing_keystore_important.jks")
            storePassword = "GrYpSuJ123456"
            keyAlias = "szymonchaber"
            keyPassword = "GrYpSuJ123456"
        }
    }
    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
            resValue("string", "app_name", "Devstory")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            resValue("string", "app_name", "Checkstory")
            signingConfig = signingConfigs.getByName("release")
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
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    hilt {
        enableAggregatingTask = true
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(project(":common"))
    implementation(project(":design"))
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(project(":api"))
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

    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.bundles.common)
    implementation(libs.bundles.ui)

    ksp(libs.compose.destinations.ksp)

    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)
    ksp(libs.hilt.compiler)

    implementation(libs.ads)
    implementation(libs.review)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.performance)
    implementation(libs.firebase.cloud.messaging)

    debugImplementation(libs.compose.debugUiTooling)
    testImplementation(libs.bundles.unitTest)
    androidTestImplementation(libs.bundles.uiTest)
}
