plugins {
    id("org.jetbrains.intellij")
    kotlin("jvm")
}

repositories {
    mavenCentral()
    mavenLocal()
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version.set("2022.1") // target version
    type.set("IC")
    updateSinceUntilBuild.set(false)
    plugins.set(listOf("com.intellij.java", "org.jetbrains.kotlin"))
}

dependencies {
    implementation(project(":eden-ide"))
//    implementation("io.github.zsqw123:eden-idea:1.0.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

tasks {
    getByName<Test>("test") {
        useJUnitPlatform()
    }
    withType<JavaCompile> {
        sourceCompatibility = JavaVersion.VERSION_11.toString()
        targetCompatibility = JavaVersion.VERSION_11.toString()
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
    }
    runIde {
        val idePath: String by project.extra
        if (idePath.isNotEmpty()) {
            println("config ide_path: $idePath")
            ideDir.set(file(idePath))
        }
        jvmArgs("-Xmx2048m")
    }
    withType<org.jetbrains.intellij.tasks.PatchPluginXmlTask> {
        changeNotes.set(
            """Add change notes here.<br>
      <em>most HTML tags may be used</em>"""
        )
    }
}