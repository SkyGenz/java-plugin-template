import java.io.ByteArrayOutputStream

plugins {
    id("java")
    id("maven-publish")
    id("java-library")

    alias(libs.plugins.run.paper)
    alias(libs.plugins.shadow)
}

group = "com.arcanius.template"
val fullVersion = "1.0.0"
val snapshot = true
version = "$fullVersion${getVersionMeta(true)}"
ext["versionBeta"] = getVersionMeta(true)
ext["versionNoHash"] = "$fullVersion${getVersionMeta(false)}"

fun getVersionMeta(includeHash: Boolean): String {
    if (!snapshot) {
        return ""
    }
    var commitHash = ""
    if (includeHash && file(".git").isDirectory) {
        val stdout = ByteArrayOutputStream()
        exec {
            commandLine("git", "rev-parse", "--short", "HEAD")
            standardOutput = stdout
        }
        commitHash = "+${stdout.toString().trim()}"
    }
    return "$commitHash-SNAPSHOT"
}

// Not the kotlin way, but works for now
var repoUser = properties["arcanius_nexus_username"]?.toString()
var repoPass = properties["arcanius_nexus_password"]?.toString()
if (System.getenv("ARCANIUS_NEXUS_USERNAME") != null) {
    repoUser = System.getenv("ARCANIUS_NEXUS_USERNAME")
}
if (System.getenv("ARCANIUS_NEXUS_PASSWORD") != null) {
    repoPass = System.getenv("ARCANIUS_NEXUS_PASSWORD")
}

repositories {
    mavenCentral()
    maven("https://jitpack.io/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")

    // Ideally, this repo will have mirrors so we don't care about downtime of dependency repos
    maven("https://nexus.arcanius.net/repository/maven-snapshots/") {
        credentials {
            username = repoUser
            password = repoPass
        }
    }
}

dependencies {
    compileOnly(libs.papermc.api);
}

publishing {
    repositories {
        maven {
            name = "nexus-arcanius"
            url = uri("https://nexus.arcanius.net/repository/maven-snapshots/")
            credentials {
                username = repoUser
                password = repoPass
            }
        }
    }
    publications {
        create<MavenPublication>("nexus") {
            groupId = project.group.toString()
            artifactId = name
            version = project.version.toString()

            // Include shaded jar if the shadow plugin is applied
            // Not sure if ts works
            from(components["java"])
            artifact(tasks.findByName("shadowJar")) {
                classifier = null
            }
            pom {
                name.set("Arcanius Template")
                description.set("A template for Arcanius plugins")
                url.set("https://arcanius.net")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/license/mit/")
                    }
                }
                developers {
                    developer {
                        id.set("Arcanius")
                        name.set("Arcanius")
                        email.set("test@test.com")
                        roles.set(listOf("Owner", "Developer"))
                        timezone.set("Europe/London")
                        organization.set("Arcanius")
                        organizationUrl.set("https://arcanius.net")
                    }
                }
            }
        }
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
    processResources {
        filesMatching("plugin.yml") {
            expand("version" to version)
        }
        filteringCharset = "UTF-8"
    }

    register("buildIfPossible") {
        group = "build"
        description = "Scans for possible build tasks with priority to build with, else ./gradlew build"

        val reobfTask = findByName("reobfJar")
        val shadowTask = findByName("shadowJar")
        when {
            reobfTask != null -> dependsOn(reobfTask)
            shadowTask != null -> dependsOn(shadowTask)
            else -> dependsOn("build")
        }
    }
}