plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    `maven-publish`
}

kotlin {
    sourceSets.commonMain {
        dependencies {
            compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-core")
        }
    }

    jvm() {
        compilations["main"].defaultSourceSet {
            dependencies {
                implementation(project(":bit-set"))
            }
        }
    }
}
