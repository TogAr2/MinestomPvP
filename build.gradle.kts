import com.vanniktech.maven.publish.tasks.JavadocJar
import java.time.LocalDate
import java.time.format.DateTimeFormatter

plugins {
    java
    id("com.vanniktech.maven.publish") version "0.35.0"
}

group = "io.github.togar2"
description = "Minecraft combat library for Minestom, with support for both 1.9+ and 1.8 combat"

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(libs.minestom)
    testImplementation(libs.minestom)
    implementation(libs.fastutil)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.test {
    onlyIf { false }
}

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()

    val mcVersion = libs.versions.minestom.get().split("-")[1]
    val date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
    val version = "$date-$mcVersion"
    coordinates(project.group.toString(), project.name, version)

    pom {
        name = project.name
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
