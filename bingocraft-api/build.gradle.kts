plugins {
    id("java-library")
    id("io.freefair.lombok") version "8.13.1"
}

dependencies {
    compileOnly(rootProject.ext["paperApi"].toString())
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
    withJavadocJar()
}