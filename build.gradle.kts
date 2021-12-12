import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.0"
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

group = "dev.taboo.taboo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.altrisi:JDA:68a46e84ee")
    implementation("com.github.altrisi:JDA-Chewtils:96b65c4fd")
    implementation("com.github.MinnDevelopment:jda-ktx:0.8.1-alpha.2")
    implementation("com.github.MinnDevelopment:jda-reactor:1.5.0")
    implementation("ch.qos.logback:logback-classic:1.2.7")
    implementation("com.zaxxer:HikariCP:5.0.0")
    implementation("org.xerial:sqlite-jdbc:3.36.0.3")
    implementation("com.github.oshi:oshi-core:5.8.5")
    implementation("org.jetbrains.exposed:exposed-core:0.36.2")
    implementation("org.jetbrains.exposed:exposed-dao:0.36.2")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.36.2")
    implementation("io.sentry:sentry:5.4.3")
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