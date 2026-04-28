plugins {
    kotlin("jvm") version "2.1.20"
    war
    id("org.wildfly.build.provision") version "0.0.11"
//    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
}

group = "com.ssnagin"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0")

    // REST TESTS
    testImplementation("io.rest-assured:rest-assured:5.5.0")
    testImplementation("io.rest-assured:kotlin-extensions:5.5.0")

    compileOnly("jakarta.platform:jakarta.jakartaee-web-api:10.0.0")
    compileOnly("jakarta.ws.rs:jakarta.ws.rs-api:3.1.0")

    implementation("com.fasterxml.jackson.jaxrs:jackson-jaxrs-json-provider:2.20.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.20.1")

    providedCompile("jakarta.platform:jakarta.jakartaee-api:10.0.0")
    implementation("org.eclipse.persistence:org.eclipse.persistence.jpa:4.0.8")

    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.20.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.20.1")

    implementation("at.favre.lib:bcrypt:0.10.2")

    providedCompile("org.eclipse.persistence:eclipselink:4.0.8")

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(17)
}

// INIT VARIABLES

var projectName = project.findProperty("sn.project.name")
val projectGroup = project.findProperty("sn.project.group")
val projectVersion = project.findProperty("sn.project.version")

val projectResourcesPath = project.findProperty("sn.project.resources_path")

// WAR

var warFilename = project.findProperty("sn.war.filename")
var warDirectory = project.findProperty("sn.war.directory")

// MUSIC

val snMusicPath = project.findProperty("sn.music.path")

// ALT

val kotlinDir = project.findProperty("sn.alt.kotlin.dir")

// 1. Compile

tasks.register("compile") {
    dependsOn(tasks.compileKotlin)
}

// 2. Build - уже есть

tasks.war {
    webAppDirectory = file(warDirectory.toString())
    archiveFileName.set(warFilename.toString())
}

tasks.named("build") {

    setDependsOn(emptyList<Any>())

    dependsOn("compile")
    dependsOn("war")

    doLast {
        println("Sources were successfully built: $warFilename")
    }
}

// 3. Clean - уже есть

// 4. Test

tasks.test {

    dependsOn("build")

    useJUnitPlatform()
}

// 5. Music

tasks.register("music") {
    dependsOn("build")

    println(snMusicPath)

    doLast {
        val os = System.getProperty("os.name").lowercase()
        println(os)

        when {
            os.contains("win") -> exec {
                commandLine("cmd", "/c", "start", "/min", snMusicPath)
            }

            os.contains("linux") -> exec {
                commandLine("ffplay", "-nodisp", "-autoexit", snMusicPath)
            }
        }

    }
}

val altSrcName = project.findProperty("sn.alt.src.dir.name")

val altSourceDir = layout.buildDirectory.dir(altSrcName.toString())

val projectClasses = mutableSetOf<String>()
val projectVars = mutableSetOf<String>()

val srcDir = file(kotlinDir.toString())

if (srcDir.exists()) {
    fileTree(srcDir).matching { include("**/*.kt") }.forEach { file ->

        projectClasses.add(file.nameWithoutExtension)

        file.readLines().forEach { line ->
            val match = Regex("\\b(?:val|var)\\s+([a-zA-Z_][a-zA-Z0-9_]*)").find(line)
            if (match != null) {
                projectVars.add(match.groupValues[1])
            }
        }
    }
}

val ignoredVars = setOf("id", "message", "status", "body", "e", "it", "x", "y")
projectVars.removeAll(ignoredVars)

val sortedClasses = projectClasses.sortedByDescending { it.length }
val sortedVars = projectVars.sortedByDescending { it.length }


tasks.register<Copy>("prepareAltSources") {

    from(srcDir)
    into(altSourceDir)

    rename { fileName ->
        if (fileName.endsWith(".kt")) "Alt$fileName" else fileName
    }

    filter { line ->
        var modifiedLine = line

        // PointResource -> AltPointResource
        sortedClasses.forEach { className ->
            modifiedLine = modifiedLine.replace("\\b$className\\b".toRegex(), "Alt$className")
        }

        // var/val username -> altUsername
        sortedVars.forEach { varName ->
            val altVarName = "alt" + varName.replaceFirstChar { it.uppercase() }
            modifiedLine = modifiedLine.replace("\\b$varName\\b".toRegex(), altVarName)
        }

        modifiedLine
    }

}

sourceSets {

    create("alt") {

        java.srcDir(altSourceDir)
        compileClasspath += sourceSets.main.get().compileClasspath
    }
}

tasks.named("compileAltKotlin") {
    dependsOn("prepareAltSources")
}

tasks.register<Jar>("altJar") {
    dependsOn("compileAltKotlin")
    archiveBaseName.set("$projectName-alt")
    archiveVersion.set(projectVersion.toString())

    from(sourceSets["alt"].output)

    from(projectResourcesPath.toString()) {
        include("**/*")
    }

    doLast {
        println("Alt was completed: ${archiveFile.get().asFile.absolutePath}")
    }
}

// 6. Alt
tasks.register("alt") {
    dependsOn("build")
    dependsOn("altJar")
}
//ktlint {
//    version.set("1.3.1")
//    verbose.set(true)
//}
