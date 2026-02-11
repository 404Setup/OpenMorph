plugins {
    id("java")
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.serialization") version "2.3.0"

    id("com.gradleup.shadow") version "9.3.1"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "one.pkg"
version = "26.1.1"

repositories {
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    implementation("com.github.avro-kotlin.avro4k:avro4k-core:2.9.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks {
    test {
        useJUnitPlatform()
    }

    runServer {
        minecraftVersion("1.21.11")
    }
}

val targetJavaVersion = 21

java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
}

tasks.withType<JavaCompile>().configureEach {
    if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
        options.release.set(targetJavaVersion)
    }

    options.compilerArgs.add("-Xdiags:verbose")
}

kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks.shadowJar {
    isZip64 = true
    configurations = listOf(project.configurations.shadow.get())
    archiveClassifier.set("")
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("paper-plugin.yml") {
        expand(props)
    }
}
