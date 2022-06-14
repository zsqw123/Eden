import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.intellij")
    id("com.vanniktech.maven.publish")
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

group = "io.github.zsqw123"
version = "1.0.1"

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version.set("2022.1") // target version
    type.set("IC")
    updateSinceUntilBuild.set(false)
    plugins.set(listOf("com.intellij.java", "org.jetbrains.kotlin"))
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = JavaVersion.VERSION_11.toString()
        targetCompatibility = JavaVersion.VERSION_11.toString()
    }
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
    }
}