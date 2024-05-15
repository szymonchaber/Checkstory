package dev.szymonchaber.checkstory.gradle

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Action
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions

class LibraryConfig : Plugin<Project> {

    override fun apply(target: Project) {
        target.apply(plugin = "com.android.library")
        target.apply(plugin = "org.jetbrains.kotlin.android")
        target.extensions.configure(LibraryExtension::class.java) {
            compileSdk = 34

            defaultConfig {
                minSdk = 24

                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                consumerProguardFiles("consumer-rules.pro")
            }

            buildTypes {
                release {
                    isMinifyEnabled = false
                    proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
                }
            }
            compileOptions {
                isCoreLibraryDesugaringEnabled = true
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
            buildFeatures {
                compose = true
            }
            composeOptions {
                kotlinCompilerExtensionVersion = "1.5.12"
            }
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    private fun LibraryExtension.kotlinOptions(configure: Action<KotlinJvmOptions>) {
        (this as org.gradle.api.plugins.ExtensionAware).extensions.configure("kotlinOptions", configure)
    }
}
