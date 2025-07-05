plugins {
    id("java-library")
    id("com.gradleup.shadow") version "9.0.0-beta13"
}

val paperVersion = "1.21.7-R0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

allprojects {
    group = "com.pritam.bingocraft"
    version = "1.0.0-beta1"

    ext {
        set("paperVersion", paperVersion)
        set("paperApi", "io.papermc.paper:paper-api:$paperVersion")
    }

    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
    }
}

subprojects {
    apply(plugin = "java-library")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    tasks.withType<ProcessResources> {
        filteringCharset = "UTF-8"
    }
}

tasks.register("buildAll") {
    dependsOn("clean")
    dependsOn(":bingocraft-plugin:shadowJar")
    group = "build"
    description = "Builds all modules and creates a combined jar"
}