import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.1.0"
    id("org.springframework.boot") version "2.7.0"
    id("com.google.cloud.tools.jib") version "3.2.1"
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
    implementation("net.dv8tion:JDA:5.0.0-alpha.14")
    implementation("club.minnced:discord-webhooks:0.8.0")
    implementation("pw.chew:jda-chewtils:2.0-SNAPSHOT")
    implementation("com.github.Xirado:Lavalink-Client:041082f")
    implementation("com.github.Topis-Lavalink-Plugins:Topis-Source-Managers:2.0.6")
    implementation("com.dunctebot:sourcemanagers:1.8.0")
    implementation("ch.qos.logback:logback-classic:1.2.11")
    implementation("org.postgresql:postgresql:42.3.6")
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

jib {
    val user = System.getenv("DOCKER_USERNAME")
    val pass = System.getenv("DOCKER_PASSWORD")
    from {
        image = "openjdk:18"
        auth {
            username = user
            password = pass
        }
    }
    to {
        image = "chalkyjeans/taboo:latest"
        auth {
            username = user
            password = pass
        }
    }
    container {
        ports = listOf("8080")
        workingDirectory = "/taboo"
    }
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "18"
}
