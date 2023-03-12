plugins {
    id("org.jetbrains.intellij")
    id("com.vanniktech.maven.publish")
    kotlin("jvm")
}
group = "io.github.zsqw123"
version = ext.get("edenPublishVersion")!!

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(project(":eden-ide"))
    compileOnly("com.squareup:kotlinpoet:1.12.0")
}

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
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
    }
}
