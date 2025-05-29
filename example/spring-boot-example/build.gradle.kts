val joinerVersion = "1.22"
val querydslVersion = "5.1.0"
val hibernateVersion = "6.6.1.Final"

plugins {
    kotlin("jvm") version "2.1.20"
    kotlin("plugin.spring") version "2.1.20"
    kotlin("plugin.jpa") version "2.1.20"
    id("org.springframework.boot") version "3.3.4"
    id("io.spring.dependency-management") version "1.1.5"
    id("com.google.devtools.ksp") version "2.1.20-2.0.1"
}

group = "cz.encircled.joiner"
version = "0.0.1-SNAPSHOT"
description = "Demo project for Spring Boot"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
    mavenLocal()
}


dependencies {
    ksp("cz.encircled:joiner-ksp:2.0.0")

    implementation("cz.encircled:joiner-kotlin:$joinerVersion")

    // DB drivers
    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("ch.vorburger.mariaDB4j:mariaDB4j:3.0.1")
    implementation("ch.vorburger.mariaDB4j:mariaDB4j-springboot:3.0.1")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.4.1")

    // Spring + Kotlin
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    // Hibernate
    implementation("org.hibernate.orm:hibernate-core:$hibernateVersion")
    implementation("com.querydsl:querydsl-core:$querydslVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf("-Xjsr305=strict")
    }
}
