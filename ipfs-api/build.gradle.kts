import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.org.jetbrains.kotlin.multiplatform)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization)
    alias(libs.plugins.com.vanniktech.maven.publish)
    id("jacoco")
}

group = project.property("GROUP") as String
version = project.property("VERSION_NAME") as String

kotlin {
    val darwinTargets = arrayOf(
        "macosX64", "macosArm64",
        "iosArm64", "iosX64", "iosSimulatorArm64",
        "tvosArm64", "tvosX64", "tvosSimulatorArm64",
        "watchosArm32", "watchosArm64", "watchosX64", "watchosSimulatorArm64", //"watchosDeviceArm64"
    )
    val linuxTargets = arrayOf("linuxX64"/*, "linuxArm64" https://youtrack.jetbrains.com/issue/KTOR-6173/ */)
    val mingwTargets = arrayOf("mingwX64")
    val nativeTargets = linuxTargets + darwinTargets + mingwTargets

    jvmToolchain(8)
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
    }
    js(IR) {
        nodejs()
    }
    for (target in nativeTargets) {
        targets.add(presets.getByName(target).createTarget(target))
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
                api(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                api(libs.okio)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.okio.fakefilesystem)
            }
        }

        val jvmMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.ktor.client.okhttp)
            }
        }

        val jsMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.ktor.client.js)
                implementation(libs.okio.nodefilesystem)
            }
        }


        val jvmTest by getting {
            dependencies {
                implementation(libs.mockito.core)
                implementation(libs.mockwebserver)
                implementation(libs.assertj.core)
            }
        }
        val nativeMain by creating {
            dependsOn(commonMain)
        }

        val darwinMain by creating {
            dependsOn(nativeMain)
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }
        darwinTargets.forEach { target ->
            getByName("${target}Main") {
                dependsOn(darwinMain)
            }
        }
        val linuxAndMingwMain by creating {
            dependsOn(nativeMain)
            dependencies {
                implementation(libs.ktor.client.curl)
            }
        }
        (mingwTargets + linuxTargets).forEach { target ->
            getByName("${target}Main") {
                dependsOn(linuxAndMingwMain)
            }
        }

    }
}

tasks.withType<KotlinCompile> {
    compilerOptions.jvmTarget = JvmTarget.JVM_1_8
}

plugins.withId("com.vanniktech.maven.publish") {
    configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
        publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.S01)
        signAllPublications()
    }
}