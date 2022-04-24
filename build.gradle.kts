import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.1.0"
    kotlin("jvm") version "1.6.20"
}

group = "xyz.chalky.taboo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://m2.dv8tion.net/releases")
    maven("https://m2.chew.pro/snapshots")
    maven("https://jcenter.bintray.com")
    maven("https://jitpack.io")
}

dependencies {
    implementation("net.dv8tion:JDA:5.0.0-alpha.10")
    implementation("pw.chew:jda-chewtils:2.0-SNAPSHOT")
    implementation("com.github.MinnDevelopment:jda-ktx:master-SNAPSHOT")
    implementation("com.github.KittyBot-Org:Lavalink-Client:ba5094f880") {
        exclude("net.dv8tion", "JDA")
    }
    implementation("ch.qos.logback:logback-classic:1.2.11")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.xerial:sqlite-jdbc:3.36.0.3")
    implementation("org.jetbrains.exposed:exposed-core:0.37.3")
    implementation("org.jetbrains.exposed:exposed-dao:0.37.3")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.37.3")
    implementation("com.google.guava:guava:31.1-jre")
    implementation("io.github.microutils:kotlin-logging:2.1.21")
    implementation(kotlin("stdlib-jdk8"))
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "17"
}
