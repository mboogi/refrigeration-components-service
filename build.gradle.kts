import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.7.5"
    id("io.spring.dependency-management") version "1.0.15.RELEASE"
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21"
    id("jacoco")
//    id("org.flywaydb.flyway") version "9.5.1"
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

    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc:2.7.5")
    runtimeOnly("io.r2dbc:r2dbc-postgresql:0.8.13.RELEASE")
    runtimeOnly("org.postgresql:postgresql:42.5.0")
    implementation("org.flywaydb:flyway-core")

    testImplementation("org.testcontainers:testcontainers")
    //  testImplementation("org.testcontainers:junit-jupiter:1.17.5")
    testImplementation("org.testcontainers:junit-jupiter:1.17.5")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.1")
    testImplementation("org.testcontainers:postgresql:1.17.5")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")



    implementation("org.postgresql:postgresql:42.5.0")

    implementation("io.springfox:springfox-swagger2:3.0.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.0-rc3")

    compileOnly("org.springframework.metrics:spring-metrics:latest.release")
    compileOnly("io.prometheus:simpleclient_common:latest.release")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
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
// tasks.jacocoTestCoverageVerification {
//    violationRules {
//        rule {
//            limit {
//                minimum = "0.30".toBigDecimal()
//            }
//        }
//        rule {
//            enabled = true
//            element = "CLASS"
//            limit {
//                counter = "BRANCH"
//                value = "COVEREDRATIO"
//                minimum = "0.90".toBigDecimal()
//            }
//
//            limit {
//                counter = "LINE"
//                value = "COVEREDRATIO"
//                minimum = "0.80".toBigDecimal()
//            }
//
//            limit {
//                counter = "LINE"
//                value = "TOTALCOUNT"
//                maximum = "200".toBigDecimal()
//            }
//            excludes = listOf(
//                "*.test.*",
//                "*.Kotlin*"
//            )
//        }
//    }
// }
tasks.withType<Test> {
    jacoco {
        toolVersion = "0.8.8"
    }
    useJUnitPlatform()
}
