plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

gradlePlugin {
    plugins {
        create("dependencies") {
            id = "dev.szymonchaber.checkstory.dependencies"
            implementationClass = "dev.szymonchaber.checkstory.gradle.Dependencies"
        }
    }
}
