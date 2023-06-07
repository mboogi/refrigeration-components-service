import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.springframework.boot") version "3.0.1"
    id("io.spring.dependency-management") version "1.0.15.RELEASE"
    kotlin("jvm") version "1.8.0"
    kotlin("plugin.spring") version "1.8.0"
    id("jacoco")

}

group = "refrigeration.components.selector"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc:3.0.1")
    runtimeOnly("org.postgresql:r2dbc-postgresql:1.0.0.RELEASE")
    runtimeOnly("org.postgresql:postgresql:42.5.0")
    implementation("org.flywaydb:flyway-core")

    testImplementation("org.testcontainers:testcontainers")

    testImplementation("org.testcontainers:junit-jupiter:1.17.5")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.1")
    testImplementation("org.testcontainers:postgresql:1.17.5")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")

    implementation("org.postgresql:postgresql:42.5.0")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.0-rc3")

    //health
    implementation("org.springframework.boot:spring-boot-starter-actuator:3.0.1")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus:1.10.3")

    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.0.2")
    implementation("org.springframework.boot:spring-boot-starter-logging:3.1.0")

}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}
//tasks.getByName<BootJar>("bootJar") {
//    enabled = false
//}

//tasks.getByName<Jar>("jar") {
//    enabled = true
//    manifest {
//        attributes["Main-Class"] = "refrigeration.components.selector.ComponentsSelectorApplicationKt"
//    }
//    // To avoid the duplicate handling strategy error
//    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
//
//    // To add all of the dependencies
//    from(sourceSets.main.get().output)
//
//    dependsOn(configurations.runtimeClasspath)
//    from({
//        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
//    })
//}
tasks {
    val fatJar = register<Jar>("fatJar") {
        dependsOn.addAll(listOf("compileJava", "compileKotlin", "processResources")) // We need this for Gradle optimization to work
        archiveClassifier.set("standalone") // Naming the jar
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        manifest {
            attributes["Main-Class"] = "refrigeration.components.selector.ComponentsSelectorApplicationKt"
        }

        val sourcesMain = sourceSets.main.get()
        val contents = configurations.runtimeClasspath.get()
            .map { if (it.isDirectory) it else zipTree(it) } +
                sourcesMain.output
        from(contents)
    }
    build {
        dependsOn(fatJar) // Trigger fat jar creation during build
    }
}

tasks.jacocoTestReport {
    reports {
        html.required.set(true)
        xml.required.set(false)
        csv.required.set(false)
    }
    finalizedBy("jacocoTestCoverageVerification")
}
 tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.30".toBigDecimal()
            }
        }
        rule {
            enabled = true
            element = "CLASS"
            limit {
                counter = "BRANCH"
                value = "COVEREDRATIO"
                minimum = "0.90".toBigDecimal()
            }

            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.80".toBigDecimal()
            }

            limit {
                counter = "LINE"
                value = "TOTALCOUNT"
                maximum = "200".toBigDecimal()
            }
            excludes = listOf(
                "*.test.*",
                "*.Kotlin*"
            )
        }
    }
 }
tasks.withType<Test> {
    jacoco {
        toolVersion = "0.8.8"
    }
    useJUnitPlatform()
}
