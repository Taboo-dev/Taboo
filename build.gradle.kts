import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.0"
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "dev.taboo.taboo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://m2.dv8tion.net/releases")
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.aasmart:JDA:dbce81721f")
    implementation("com.github.chalkyjeans:JDA-Chewtils:8197beaf9c")
    implementation("ch.qos.logback:logback-classic:1.2.7")
    implementation("com.zaxxer:HikariCP:5.0.0")
    implementation("org.postgresql:postgresql:42.3.1")
    implementation("com.github.oshi:oshi-core:5.8.4")
    implementation("org.jetbrains.exposed:exposed-core:0.36.2")
    implementation("org.jetbrains.exposed:exposed-dao:0.36.2")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.36.2")
    implementation("io.sentry:sentry:5.4.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

tasks.withType<Jar> {
    manifest {
        attributes(mapOf("Main-Class" to "dev.taboo.taboo.Taboo"))
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}