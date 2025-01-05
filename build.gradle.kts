plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.0"
}

group = "me.leonrobi"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("net.minestom:minestom-snapshots:d760a60a5c")
    implementation("dev.hollowcube:polar:1.12.0")
}

tasks.jar {
    isEnabled = false
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.withType<Jar> {
    manifest {
        attributes(
            "Main-Class" to "me.leonrobi.Main"
        )
    }

    destinationDirectory.set(file("launch4j"))
}