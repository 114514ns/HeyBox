plugins {
    kotlin("multiplatform") version "2.0.20"
    id("maven-publish")
}
repositories {
    mavenLocal()
    mavenCentral()
}
kotlin {
    jvm()
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }
    sourceSets {
        val ktor_version = "3.0.0-beta-2"
        val commonMain by getting {
            dependencies {
                compileOnly("cn.pprocket:HeyBase:241207-7")
                implementation("io.ktor:ktor-client-core:3.0.0-beta-2")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
                implementation("com.fleeksoft.ksoup:ksoup:0.1.8")
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
        val wasmJsMain by getting {
            dependencies {
                // 仅用于 wasmJs 平台的依赖
                implementation("io.ktor:ktor-client-js-wasm-js:$ktor_version")
                implementation("org.jetbrains.kotlinx:kotlinx-browser:0.1")
            }
        }
    }
}
group = "cn.pprocket"
version = "241207-4"


publishing {

}

