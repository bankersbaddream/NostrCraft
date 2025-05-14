plugins {
    kotlin("jvm") version "2.1.20"
    kotlin("plugin.serialization") version "2.1.20"
    application
}

group = "org.sectiontwo"
version = "1.0"

repositories {
    mavenCentral()
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
    maven { url = uri("https://repo.bouncycastle.org/repo") }
}

dependencies {
    // PaperMC API
    compileOnly("io.papermc.paper:paper-api:1.21.5-R0.1-SNAPSHOT")

    // Kotlin standard library
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.22")

    // Nostr dependencies
    implementation("org.java-websocket:Java-WebSocket:1.5.3") // WebSocket client
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0") // JSON serialization
    implementation("org.bouncycastle:bcprov-jdk15on:1.70") // BouncyCastle for ECDSA
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}
