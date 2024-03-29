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
    maven("https://maven.pkg.github.com/CloudCraftProjects/*/") {
        name = "github"
        credentials(PasswordCredentials::class.java)
    }
    maven("https://papermc.io/repo/repository/maven-public/")
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
        languageVersion.set(JavaLanguageVersion.of(17))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}

publishing {
    publications.create<MavenPublication>("maven") {
        artifactId = project.name.lowercase()
        from(components["java"])
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
        minecraftVersion("1.20.2")

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
