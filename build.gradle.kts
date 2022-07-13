import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.1.0"
    id("org.springframework.boot") version "2.7.0"
    kotlin("jvm") version "1.6.20"
}

group = "xyz.chalky.taboo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://m2.dv8tion.net/releases")
    maven("https://m2.duncte123.dev/releases")
    maven("https://m2.chew.pro/snapshots")
    maven("https://jcenter.bintray.com")
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.DV8FromTheWorld:JDA:07e8166fc5")
    implementation("club.minnced:discord-webhooks:0.8.0")
    implementation("pw.chew:jda-chewtils:2.0-SNAPSHOT")
    implementation("com.github.Xirado:Lavalink-Client:041082f")
    implementation("com.github.Topis-Lavalink-Plugins:Topis-Source-Managers:2.0.6")
    implementation("com.dunctebot:sourcemanagers:1.8.0")
    implementation("ch.qos.logback:logback-classic:1.2.11")
    implementation("org.postgresql:postgresql:42.3.4")
    implementation("com.google.guava:guava:31.1-jre")
    implementation("com.github.minndevelopment:emoji-java:master-SNAPSHOT")
    implementation("io.sentry:sentry-spring-boot-starter:6.1.2")
    implementation("io.sentry:sentry-logback:6.1.2")
    implementation("javax.xml.bind:jaxb-api:2.3.0")
    implementation("org.springframework.boot:spring-boot-starter-web:2.7.0")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:2.7.0")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor:2.7.0")
    implementation(kotlin("stdlib-jdk8"))
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "18"
}
