import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar
plugins {
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.lombok") version "2.0.0"
    id("io.freefair.lombok") version "8.6"
    id("maven-publish")
}

group = "cn.pprocket"
version = "240709-114514"

repositories {
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }

}
sourceSets {
    main {
        resources {
            srcDirs("src/main/resources")
        }
    }
}
dependencies {
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.14")
    implementation("org.projectlombok:lombok:1.18.32")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC")
    implementation("org.graalvm.js:js:22.3.0")
    implementation("org.jsoup:jsoup:1.15.3")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(8)
}
val sourceJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
publishing {
    repositories {
        maven {

            name = "heybox"
            url = uri("https://maven.pkg.github.com/114514ns/heybox")
            credentials {
                username = "114514ns"
                password = project.findProperty("gpr.token").toString()
            }
        }
    }

    publications {
        register<MavenPublication>("heybox") {
            artifact(sourceJar.get())
            artifactId = "heybox"
            from(components["java"])
        }
    }
}

