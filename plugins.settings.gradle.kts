pluginManagement {
    val kotlinVersion: String by settings
    val atomicFuVersion: String by settings
    val drillGradlePluginVersion: String by settings
    plugins {
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion
        id("kotlinx-atomicfu") version atomicFuVersion
        id("com.epam.drill.version.plugin") version drillGradlePluginVersion
        id("com.github.johnrengelman.shadow") version "5.2.0"
    }

    repositories {
        gradlePluginPortal()
        maven(url = "https://oss.jfrog.org/artifactory/list/oss-release-local")
        maven(url = "https://dl.bintray.com/kotlin/kotlinx/")
    }
}

//TODO remove after plugin marker is added to the atomicfu artifacts
mapOf(
    "kotlinx-atomicfu" to "org.jetbrains.kotlinx:atomicfu-gradle-plugin"
).let { substitutions ->
    pluginManagement.resolutionStrategy.eachPlugin {
        substitutions["${requested.id}"]?.let { useModule("$it:${target.version}") }
    }
}
