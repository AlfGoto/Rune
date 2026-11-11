plugins {
    java
    id("xyz.jpenilla.run-paper") version "3.1.0"
}

group = "com.alfgoto.rune"
version = "0.1.0-SNAPSHOT"

val minecraftVersion: String by project

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:$minecraftVersion.build.+")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks {
    processResources {
        val props = mapOf("version" to project.version, "minecraftVersion" to minecraftVersion)
        inputs.properties(props)
        filesMatching("plugin.yml") { expand(props) }
    }
    runServer {
        minecraftVersion(minecraftVersion)
    }
}
