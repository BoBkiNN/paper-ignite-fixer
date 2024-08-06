import org.jetbrains.kotlin.gradle.utils.extendsFrom

plugins {
    id("java")
    `kotlin-dsl`
    id("io.papermc.paperweight.userdev") version "1.7.1"
}

version = project.properties["mod_version"].toString()

var mcVer = properties["mc_version"]?.toString()
if (mcVer == null){
    throw IllegalArgumentException("Missing mc_version field")
}

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://repo.spongepowered.org/maven/")
}

dependencies {
    implementation("org.jetbrains:annotations:24.1.0")
    implementation("io.papermc.paperweight.userdev:io.papermc.paperweight.userdev.gradle.plugin:1.5.11")
    implementation("com.github.johnrengelman:shadow:8.1.1")

    remapper("net.fabricmc:tiny-remapper:0.10.1:fat")

    val include = configurations.create("include")
    // We can use "include" configuration to add dependencies that will be included to jar
    include("net.fabricmc:tiny-remapper:0.10.1:fat") {isTransitive = false}

    configurations.implementation.extendsFrom(configurations.named("include"))

    implementation("space.vectrix.ignite:ignite-api:1.0.1")
    implementation("org.spongepowered:mixin:0.8.5")
    implementation("io.github.llamalad7:mixinextras-common:0.3.5")


    paperweight.paperDevBundle(mcVer)
}

tasks.processResources {
    filteringCharset = "UTF-8"
    filesMatching("ignite.mod.json") {
        expand(project.properties)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.jar {
    val dependencies = configurations.named("include").get().map {
        if (it.isDirectory()){
            return@map it
        } else {
            return@map zipTree(it)
        }
    }
    from(dependencies)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}


// Remove if below Paper 1.20.5
paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION

// Remapping, required below Paper 1.20.5
//tasks.reobfJar {
//    remapperArgs.add("--mixin")
//}
//
//tasks.build {
//    dependsOn("reobfJar")
//}