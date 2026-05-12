import org.jetbrains.kotlin.metadata.deserialization.VersionRequirementTable.Companion.create

plugins {
    kotlin("jvm") version "2.1.20"
    war
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

// 1. Compile

tasks.register("compile") {
    dependsOn(tasks.compileKotlin)
}

// 2. Build - уже есть

tasks.war {
    var warDirectory = project.findProperty("sn.war.directory")
    var warFilename = project.findProperty("sn.war.filename")

    webAppDirectory = file(warDirectory.toString())
    archiveFileName.set(warFilename.toString())
}

tasks.named("build") {

    var warFilename = project.findProperty("sn.war.filename")

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

    val snMusicPath = project.findProperty("sn.music.path")

    dependsOn("build")

    println(snMusicPath)

    doLast {
        val os = System.getProperty("os.name").lowercase()
        println(os)

        when {
            os.contains("linux") -> exec {
                commandLine("ffplay", "-nodisp", "-autoexit", snMusicPath)
            }
        }

    }
}

// вынести в функцию?



val altSrcName = project.findProperty("sn.alt.src.dir.name")
val altSourceDir = layout.buildDirectory.dir(altSrcName.toString())
val kotlinDir = project.findProperty("sn.alt.kotlin.dir")

// Считываем регулярки из properties (или ставим дефолт, если не нашли)
val varDeclRegexProp = project.findProperty("sn.alt.regex.var_declaration")?.toString()
    ?: "\\b(?:val|var)\\s+(`[^`]+`|[\\p{L}_][\\p{L}\\d_]*)"
val varReplaceRegexProp = project.findProperty("sn.alt.regex.var_replace")?.toString()
    ?: "(?<![\\p{L}\\d_])%s(?![\\p{L}\\d_])"
val classReplaceRegexProp = project.findProperty("sn.alt.regex.class_replace")?.toString()
    ?: "(?<![\\p{L}\\d_])%s(?![\\p{L}\\d_])"


tasks.register("prepareAltSources") {
    val srcDir = file(kotlinDir.toString())
    val destDir = altSourceDir.get().asFile

    inputs.dir(srcDir)
    outputs.dir(destDir)

    doLast {
        val projectClasses = mutableSetOf<String>()
        val projectVars = mutableSetOf<String>()
        val varDeclRegex = varDeclRegexProp.toRegex()


        if (srcDir.exists()) {
            fileTree(srcDir).matching { include("**/*.kt") }.forEach { file ->
                projectClasses.add(file.nameWithoutExtension)

                file.readLines().forEach { line ->
                    varDeclRegex.findAll(line).forEach { match ->
                        val varName = match.groups[1]?.value
                        if (varName != null) {
                            projectVars.add(varName)
                        }
                    }
                }
            }
        }

        val ignoredVars = setOf(
            "id", "message", "status", "body", "e", "it", "x", "y",
            "data", "class", "val", "var", "fun", "if", "else", "for", "while", "return", "this", "super", "object", "is", "as", "in", "typealias", "package", "import", "true", "false", "null",
            "AUTHORIZATION", "OK", "BAD_REQUEST", "UNAUTHORIZED", "CONFLICT",
            "json" // <--- ДОБАВЛЯЕМ СЮДА
        )
        projectVars.removeAll(ignoredVars)

        val sortedClasses = projectClasses.sortedByDescending { it.length }
        val sortedVars = projectVars.sortedByDescending { it.length }


        destDir.deleteRecursively()
        destDir.mkdirs()

        if (srcDir.exists()) {
            fileTree(srcDir).matching { include("**/*.kt") }.forEach { file ->
                val relativePath = file.relativeTo(srcDir)
                val newFileName = if (file.name.endsWith(".kt")) "Alt${file.name}" else file.name
                val destFile = File(destDir, relativePath.parentFile?.path?.let { "$it/$newFileName" } ?: newFileName)
                destFile.parentFile.mkdirs()

                var content = file.readText()

                sortedClasses.forEach { className ->
                    val classRegex = classReplaceRegexProp.replace("%s", className).toRegex()
                    content = content.replace(classRegex, "Alt$className")
                }

                val lines = content.lines().map { line ->
                    val trimmed = line.trimStart()

                    if (trimmed.startsWith("package ") || trimmed.startsWith("import ")) {
                        line
                    } else {
                        var modifiedLine = line
                        sortedVars.forEach { varName ->
                            val isBackticked = varName.startsWith("`") && varName.endsWith("`")
                            val cleanName = if (isBackticked) varName.substring(1, varName.length - 1) else varName

                            val altVarName = if (isBackticked) {
                                "`alt${cleanName.replaceFirstChar { it.uppercase() }}`"
                            } else {
                                "alt${cleanName.replaceFirstChar { it.uppercase() }}"
                            }

                            if (isBackticked) {
                                modifiedLine = modifiedLine.replace(varName, altVarName)
                            } else {
                                val varRegex = varReplaceRegexProp.replace("%s", cleanName).toRegex()
                                modifiedLine = modifiedLine.replace(varRegex, altVarName)
                            }
                        }
                        modifiedLine
                    }
                }

                destFile.writeText(lines.joinToString("\n"))
            }
        }
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

    val projectResourcesPath = project.findProperty("sn.project.resources_path")

    var projectName = project.findProperty("sn.project.name")
    val projectVersion = project.findProperty("sn.project.version")

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
