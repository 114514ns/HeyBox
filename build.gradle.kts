import org.gradle.api.publish.maven.MavenPublication
import org.gradle.internal.impldep.org.eclipse.jgit.lib.ObjectChecker.encoding
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform") version "2.0.20"
    kotlin("plugin.lombok") version "2.0.0"
    id("io.freefair.lombok") version "8.6"
    kotlin("plugin.serialization") version "2.0.0"
    id("maven-publish")
}
kotlin {
    // 配置目标平台
    jvm() // For JVM
    js(IR) { // JavaScript 目标，IR 是目前推荐的后端
        browser() // 支持浏览器运行
        nodejs()  // 支持 Node.js 运行（根据需要选择）
        binaries.executable()
    }
    mingwX64()


    @OptIn(ExperimentalWasmDsl::class)
    // 其他平台例如 macOS, watchOS, tvOS 等
    sourceSets {
        val commonMain by getting {
            dependencies {
                // 在所有平台共享的依赖
                implementation("io.ktor:ktor-client-core:3.0.0-beta-2")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
                implementation("com.fleeksoft.ksoup:ksoup:0.1.6-alpha1")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
                implementation("com.soywiz.korge:korlibs-crypto:5.4.0")

            }
        }

        val jvmMain by getting {
            dependencies {
                // 仅用于 JVM 平台的依赖
                implementation("io.ktor:ktor-client-okhttp:3.0.0-beta-2")
            }
        }

        val jsMain by getting {
            dependencies {
                // 仅用于 JavaScript 平台的依赖
                implementation("io.ktor:ktor-client-js:3.0.0-beta-2")
            }
        }
        val mingwX64Main by getting {
            dependencies {
                implementation("io.ktor:ktor-client-curl:3.0.0-beta-2")
            }
        }

    }
}
group = "cn.pprocket"
version = "240903-6"

repositories {
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")

}



kotlin {
    jvmToolchain(17)
}
/*
val sourceJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

 */
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


    /*
    publications {
        register<MavenPublication>("heybox") {
            artifact(sourceJar.get())
            artifactId = "heybox"
            from(components["java"])
        }
    }

     */
}




