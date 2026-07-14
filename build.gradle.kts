plugins {
    java
    kotlin("jvm") version "2.4.0"
    kotlin("plugin.serialization") version "2.4.0"

    id("com.gradleup.shadow") version "9.5.1"
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

group = "one.pkg"
version = "26.7.1"

repositories {
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.+")
    implementation("com.github.avro-kotlin.avro4k:avro4k-core:2.10.1")
}

val targetJavaVersion = 25

java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
}

kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks {
    runServer {
        minecraftVersion("26.1.2")
    }

    shadowJar {
        isZip64 = true
        configurations = listOf(project.configurations.shadow.get())
        archiveClassifier.set("")
    }

    build {
        dependsOn("shadowJar")
    }

    withType<JavaCompile>().configureEach {
        if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
            options.release.set(targetJavaVersion)
        }

        options.compilerArgs.add("-Xdiags:verbose")
    }

    processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("paper-plugin.yml") {
            expand(props)
        }
    }
}