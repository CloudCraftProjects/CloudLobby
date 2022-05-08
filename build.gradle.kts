plugins {
    id("java-library")
    id("maven-publish")
}

group = "dev.booky"
version = "1.7.0"

repositories {
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://repo.codemc.org/repository/maven-public/")
    maven("https://libraries.minecraft.net/")
    maven("https://jitpack.io/")
}

dependencies {
    api("io.papermc.paper:paper-api:1.17.1-R0.1-SNAPSHOT")
    api("dev.jorel.commandapi:commandapi-core:8.1.0")
    api("com.mojang:brigadier:1.0.18")
}

tasks {
    processResources {
        filesMatching("plugin.yml") {
            expand("version" to project.version)
        }
    }
}

java {
    withSourcesJar()
    withJavadocJar()

    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

publishing {
    publications.create<MavenPublication>("maven") {
        artifactId = project.name.toLowerCase()
        from(components["java"])
    }
}
