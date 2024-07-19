plugins {
    id("java-library")
    id("maven-publish")

    alias(libs.plugins.pluginyml.bukkit)
    alias(libs.plugins.run.paper)
    alias(libs.plugins.shadow)
}

group = "dev.booky"
version = "2.0.0"

val plugin: Configuration by configurations.creating {
    isTransitive = false
}

repositories {
    maven("https://repo.cloudcraftmc.de/public/")
}

dependencies {
    compileOnly(libs.paper.api)

    implementation(libs.bstats.bukkit)

    // downloaded at runtime using library loader
    sequenceOf(
        libs.caffeine,
    ).forEach {
        compileOnlyApi(it)
        library(it)
    }

    compileOnlyApi(libs.cloudcore)

    // testserver dependency plugins (maven)
    plugin(variantOf(libs.cloudcore) { classifier("all") })
}

java {
    withSourcesJar()
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
        vendor = JvmVendorSpec.ADOPTIUM
    }
}

publishing {
    publications.create<MavenPublication>("maven") {
        artifactId = project.name.lowercase()
        from(components["java"])
    }
    repositories.maven("https://repo.cloudcraftmc.de/releases") {
        name = "horreo"
        credentials(PasswordCredentials::class.java)
    }
}

bukkit {
    main = "$group.cloudlobby.CloudLobbyMain"
    apiVersion = "1.20"
    authors = listOf("booky10")
    depend = listOf("CommandAPI", "CloudCore")
}

tasks {
    runServer {
        minecraftVersion("1.20.4")

        pluginJars.from(plugin.resolve())
        downloadPlugins {
            hangar("CommandAPI", libs.versions.commandapi.get())
            github(
                "PaperMC", "Debuggery",
                "v${libs.versions.debuggery.get()}",
                "debuggery-bukkit-${libs.versions.debuggery.get()}.jar"
            )
        }
    }

    shadowJar {
        relocate("org.bstats", "${project.group}.cloudlobby.bstats")
    }

    assemble {
        dependsOn(shadowJar)
    }
}
