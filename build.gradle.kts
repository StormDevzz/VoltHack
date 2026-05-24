plugins {
    alias(libs.plugins.fabric.loom)
    alias(libs.plugins.kotlin)
}

base {
    archivesName = property("archives_name") as String
}

version = property("mod_version") as String
group = property("maven_group") as String

repositories {
    mavenCentral()
}

dependencies {
    minecraft(libs.minecraft)
    mappings(loom.officialMojangMappings())
    modImplementation(libs.fabric.loader)

    implementation(libs.kotlin.stdlib)
    include(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)
    include(libs.kotlin.reflect)
    implementation(libs.discord.ipc)
    include(libs.discord.ipc)
}

loom {
    accessWidenerPath = file("src/main/resources/volthack.accesswidener")

    mixin {
        defaultRefmapName = "volthack.refmap.json"
    }

    runs {
        named("client") {
            client()
            configName = "Client"
            ideConfigGenerated(true)
            vmArgs("-Xmx2G", "-XX:+UseG1GC")
        }
    }
}

val mcVersion: String = property("minecraft_version") as String
val loaderVersion: String = property("loader_version") as String

tasks.processResources {
    val props = mapOf(
        "version" to version,
        "minecraft_version" to mcVersion,
        "loader_version" to loaderVersion
    )

    inputs.properties(props)

    filesMatching("fabric.mod.json") {
        expand(props)
    }
}


kotlin {
    jvmToolchain(21)
}
