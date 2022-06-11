import java.util.*

// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.21")
        classpath("org.jetbrains.intellij.plugins:gradle-intellij-plugin:1.5.2")
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.20.0")
    }
}

allprojects {
    repositories {
        google()
        maven { setUrl("https://www.jetbrains.com/intellij-repository/releases/") }
        mavenCentral()
    }
    extra["idePath"] = getIdePath()
}

task("clean", Delete::class) {
    delete(rootProject.buildDir)
}

fun getIdePath(): String {
    val localPropertiesFile = File("local.properties")
    var path = ""
    if (localPropertiesFile.exists()) {
        val prop = Properties()
        prop.load(localPropertiesFile.inputStream())
        path = prop.getProperty("ide_path", "")
    }
    return path
}
