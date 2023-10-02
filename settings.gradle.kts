rootProject.name = "ipfs-api-kotlin"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

plugins {
    id("de.fayard.refreshVersions") version "0.60.3"
}

refreshVersions { // Optional: configure the plugin
}


include("example")
include("ipfs-api")
