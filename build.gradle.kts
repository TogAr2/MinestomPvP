plugins {
    java
    `maven-publish`
}

group = "io.github.togar2"

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

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}