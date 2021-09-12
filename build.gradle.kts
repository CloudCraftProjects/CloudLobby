plugins {
    `java-library`
    `maven-publish`
}

group = "tk.booky"
version = "1.6.0"

repositories {
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://jitpack.io/")
    mavenLocal()
}

dependencies {
    api("io.papermc.paper:paper-api:1.17.1-R0.1-SNAPSHOT")
    api("me.rockyhawk99:commandpanels:3.15.6.2")
}

tasks.processResources {
    filesMatching("plugin.yml") {
        expand("version" to project.version)
    }
}
