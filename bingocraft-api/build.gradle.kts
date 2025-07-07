plugins {
    id("java-library")
    id("io.freefair.lombok") version "8.13.1"
    id("maven-publish")
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


publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            groupId = "com.pritam.bingocraft"
            artifactId = "bingocraft-api"
            version = "1.0.0-beta1"
        }
    }
}