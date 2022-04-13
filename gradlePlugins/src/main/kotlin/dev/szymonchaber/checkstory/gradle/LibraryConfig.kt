package dev.szymonchaber.checkstory.gradle

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Action
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension

class LibraryConfig : Plugin<Project> {

    override fun apply(target: Project) {
        target.apply(plugin = "com.android.library")
        target.apply(plugin = "org.jetbrains.kotlin.android")
        target.extensions.configure(LibraryExtension::class.java) {
            compileSdk = 31

            defaultConfig {
                minSdk = 24
                targetSdk = 31

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
                sourceCompatibility = JavaVersion.VERSION_11
                targetCompatibility = JavaVersion.VERSION_11
            }
            buildFeatures {
                compose = true
            }
            composeOptions {
                kotlinCompilerExtensionVersion = Dependencies.composeVersion
            }
            kotlinOptions {
                jvmTarget = "11"
            }
            libraryVariants.forEach { variant ->
                target.kotlinExtension.sourceSets.create(variant.name) {
                    kotlin.srcDir("build/generated/ksp/${variant.name}/kotlin")
                }
            }
        }
        target.dependencies.add("coreLibraryDesugaring", "com.android.tools:desugar_jdk_libs:1.1.5")
    }

    private fun LibraryExtension.kotlinOptions(configure: Action<KotlinJvmOptions>) {
        (this as org.gradle.api.plugins.ExtensionAware).extensions.configure("kotlinOptions", configure)
    }
}
