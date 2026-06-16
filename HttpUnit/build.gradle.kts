plugins {
    war
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.gargoylesoftware.htmlunit"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(fileTree("lib") { include("*.jar") })
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "Main"
    }
}

tasks.war {
    // var warDir = project.file("src/main/webapp")
    archiveFileName.set("HttpUnit.war")
}

sourceSets {
    main {
        java.setSrcDirs(listOf("src"))
    }
}

tasks.shadowJar {
    archiveFileName.set("HttpUnit-1.0-all.jar")
    manifest {
        attributes["Main-Class"] = "Main"
    }

    mergeServiceFiles()
}