plugins {
    `java-library`
    `maven-publish`

    id("xyz.jpenilla.run-paper") version "1.0.4"
}

group = "tk.booky"
version = "1.6.0"

repositories {
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://repo.codemc.org/repository/maven-public/")
    maven("https://libraries.minecraft.net/")
    maven("https://jitpack.io/")
}

dependencies {
    api("io.papermc.paper:paper-api:1.17.1-R0.1-SNAPSHOT")
    api("dev.jorel.commandapi:commandapi-core:6.3.0")
    api("com.mojang:brigadier:1.0.18")
}

tasks {
    processResources {
        filesMatching("plugin.yml") {
            expand("version" to project.version)
        }
    }

    runServer {
        minecraftVersion("1.17.1")
    }
}
