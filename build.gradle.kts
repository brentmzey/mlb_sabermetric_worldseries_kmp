plugins {
    kotlin("multiplatform") version "2.1.10"
    idea
    jacoco
}

group = "com.sabermetrics.worldseries"
version = "1.0.0"

repositories {
    mavenCentral()
    google()
}

kotlin {
    jvmToolchain(17)

    // 1. JVM Target (Desktop CLI, Data Pipeline, & Server)
    jvm {
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    // 2. JS / Web Browser Target
    js(IR) {
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
        }
        nodejs()
    }

    // 3. iOS & Apple Native Targets
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(kotlin("stdlib"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        jvmMain.dependencies {
            implementation("ch.qos.logback:logback-classic:1.4.14")
            implementation("org.slf4j:slf4j-api:2.0.11")
            implementation("org.xerial:sqlite-jdbc:3.45.1.0")
        }
        jvmTest.dependencies {
            implementation(kotlin("test-junit5"))
            implementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
            implementation("org.junit.jupiter:junit-jupiter-engine:5.10.1")
        }
    }
}

tasks.register<Jar>("fatJar") {
    group = "build"
    description = "Assembles a runnable Fat JAR for the MLB Sabermetric World Series Simulator."
    archiveBaseName.set("mlb_sabermetric_worldseries_kmp")
    archiveClassifier.set("all")
    archiveVersion.set("1.0.0")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes["Main-Class"] = "com.sabermetrics.worldseries.MainKt"
    }

    val jvmTarget = kotlin.targets.getByName("jvm") as org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
    val runtimeClasspath = jvmTarget.compilations.getByName("main").runtimeDependencyFiles
    from(runtimeClasspath.map { if (it.isDirectory) it else zipTree(it) })
    with(tasks.named<Jar>("jvmJar").get())
}

tasks.register<JavaExec>("run") {
    group = "application"
    description = "Runs the MLB Sabermetric World Series Prediction Simulator."
    mainClass.set("com.sabermetrics.worldseries.MainKt")
    val jvmTarget = kotlin.targets.getByName("jvm") as org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
    classpath = jvmTarget.compilations.getByName("main").runtimeDependencyFiles + jvmTarget.compilations.getByName("main").output.allOutputs
}

tasks.register<JacocoReport>("jacocoTestReport") {
    group = "verification"
    description = "Generates JaCoCo test coverage report for JVM/Common Kotlin code."
    dependsOn("jvmTest")
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
    val jvmTarget = kotlin.targets.getByName("jvm") as org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
    val mainCompilation = jvmTarget.compilations.getByName("main")
    classDirectories.setFrom(mainCompilation.output.classesDirs)
    sourceDirectories.setFrom(files("src/commonMain/kotlin", "src/jvmMain/kotlin"))
    executionData.setFrom(fileTree(layout.buildDirectory.dir("jacoco")).matching { include("*.exec") })
}
