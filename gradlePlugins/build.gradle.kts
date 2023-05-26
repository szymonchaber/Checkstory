plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    google()
}

dependencies {
    implementation("com.android.library:com.android.library.gradle.plugin:8.0.2")
    implementation("org.jetbrains.kotlin.android:org.jetbrains.kotlin.android.gradle.plugin:1.7.20")
}

gradlePlugin {
    plugins {
        create("dependencies") {
            id = "dependencies"
            implementationClass = "dev.szymonchaber.checkstory.gradle.Dependencies"
        }
        create("library-config") {
            id = "library-config"
            implementationClass = "dev.szymonchaber.checkstory.gradle.LibraryConfig"
        }
    }
}
