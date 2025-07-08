plugins {
    id("java-library")
    id("com.gradleup.shadow") version "9.0.0-beta13"
    id("io.freefair.lombok") version "8.13.1"
}

dependencies {
    compileOnly(rootProject.ext["paperApi"].toString())
    implementation(project(":bingocraft-api"))
    compileOnly("org.xerial:sqlite-jdbc:3.47.1.0")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.shadowJar {
    archiveBaseName.set("Bingocraft-Core")
    archiveClassifier.set("")
    minimize()
}

tasks.processResources {
    val props = mapOf("version" to project.version)
    inputs.properties(props)
    filteringCharset = "UTF-8"

    filesMatching("paper-plugin.yml") {
        expand(props)
    }
}
