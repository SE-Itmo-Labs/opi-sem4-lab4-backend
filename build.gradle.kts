plugins {
    kotlin("jvm") version "2.1.20"
    war
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
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

    compileOnly("jakarta.platform:jakarta.jakartaee-web-api:10.0.0")
    compileOnly("jakarta.ws.rs:jakarta.ws.rs-api:3.1.0")

    implementation("com.fasterxml.jackson.jaxrs:jackson-jaxrs-json-provider:2.20.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.20.1")

    providedCompile("jakarta.platform:jakarta.jakartaee-api:10.0.0")
    implementation("org.eclipse.persistence:org.eclipse.persistence.jpa:4.0.8")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.20.1")

    implementation("at.favre.lib:bcrypt:0.10.2")

    providedCompile("org.eclipse.persistence:eclipselink:4.0.8")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

tasks.war {
    webAppDirectory = file("src/main/webapp")
    archiveFileName.set("lab4.war")
}

ktlint {
    version.set("1.3.1")
    verbose.set(true)
}