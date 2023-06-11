buildscript {
    repositories {
        google()
        mavenCentral()
    }
}
plugins {
    id("com.android.application") version "8.0.2" apply false
    id("com.android.library") version "8.0.2" apply false
    id("org.jetbrains.kotlin.android") version "1.8.20" apply false
    id("com.google.dagger.hilt.android") version "2.44.2" apply false
    id("com.google.gms.google-services") version "4.3.15" apply false
    id("com.google.firebase.crashlytics") version "2.9.5" apply false
    id("com.google.firebase.firebase-perf") version "1.4.2" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.20" apply false
    id("com.github.ben-manes.versions") version "0.45.0"
}
