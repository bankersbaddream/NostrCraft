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
}

dependencies {
    // PaperMC API
    compileOnly("io.papermc.paper:paper-api:1.21.5-R0.1-SNAPSHOT")

    // Kotlin standard library (matching plugin version)
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.1.20")

    // Nostr dependencies
    implementation("org.java-websocket:Java-WebSocket:1.5.3") // WebSocket client
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0") // JSON serialization
    implementation("org.bouncycastle:bcprov-jdk18on:1.78") // Updated BouncyCastle
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.jar {
    archiveBaseName.set("NostrCraft")
    archiveVersion.set("1.0")
    
    // Include dependencies in the jar
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
