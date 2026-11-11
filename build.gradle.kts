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

    testImplementation("io.papermc.paper:paper-api:$minecraftVersion.build.+")

    // MockBukkit artifact name follows the Minecraft version (v26.3 for 26.3).
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v$minecraftVersion:4.120.0")
    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks {
    test {
        useJUnitPlatform()
    }
    processResources {
        val props = mapOf("version" to project.version, "minecraftVersion" to minecraftVersion)
        inputs.properties(props)
        filesMatching("plugin.yml") { expand(props) }
    }
    runServer {
        minecraftVersion(minecraftVersion)
    }
}
