import com.vanniktech.maven.publish.tasks.JavadocJar

plugins {
    java
    id("com.vanniktech.maven.publish") version "0.35.0"
}

group = "io.github.togar2"
description = "Minecraft combat library for Minestom, with support for both 1.9+ and 1.8 combat"
version = "1.0"

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("net.minestom:minestom:2025.10.31-1.21.10")
    testImplementation("net.minestom:minestom:2025.10.31-1.21.10")
    implementation("it.unimi.dsi:fastutil:8.5.12")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.test {
    onlyIf { false }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates(project.group.toString(), project.name, project.version.toString())
    pom {
        description = project.description
        url = "https://github.com/TogAr2/MinestomPvP/"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "TogAr2"
                name = "TogAr2"
                url = "https://github.com/TogAr2/"
            }
        }
        scm {
            url = "https://github.com/TogAr2/MinestomPvP/"
            connection = "scm:git:git://github.com/TogAr2/MinestomPvP.git"
            developerConnection = "scm:git:ssh://git@github.com/TogAr2/MinestomPvP.git"
        }
    }
}

tasks.withType<GenerateModuleMetadata>().configureEach {
    dependsOn(tasks.withType<JavadocJar>())
}
