import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.0.0"
    kotlin("jvm") version "1.6.0-RC"
}

group = "io.github.chalkyjeans.taboo"
version = "1.0-SNAPSHOT"
description = "Taboo"

repositories {
    mavenCentral()
    maven("https://m2.dv8tion.net/releases")
    maven("https://jitpack.io")
    maven("https://repo.maven.apache.org/maven2/")
}

dependencies {
    implementation("net.dv8tion:JDA:4.3.0_334")
    implementation("com.github.chalkyjeans:JDA-Chewtils:8b203c50c2")
    implementation("ch.qos.logback:logback-classic:1.2.6")
    implementation("com.zaxxer:HikariCP:5.0.0")
    implementation("com.github.oshi:oshi-core:5.8.2")
    implementation("org.jetbrains.exposed:exposed-core:0.35.3")
    implementation("org.jetbrains.exposed:exposed-dao:0.35.3")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.35.3")
    implementation(kotlin("stdlib-jdk8"))
}


tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.isIncremental = true
}

tasks.withType<Jar> {
    manifest {
        attributes(mapOf("Main-Class" to "io.github.chalkyjeans.taboo"))
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}
