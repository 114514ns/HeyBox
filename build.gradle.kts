plugins {
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.lombok") version "2.0.0"
    id("io.freefair.lombok") version "8.6"
}

group = "cn.pprocket"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.github.zhkl0228:unidbg-api:0.9.7")
    implementation("com.github.zhkl0228:unidbg-android:0.9.7")
    implementation("com.github.zhkl0228:unidbg-dynarmic:0.9.7")
    implementation("com.github.zhkl0228:unidbg-unicorn2:0.9.7")
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.14")
    implementation("org.projectlombok:lombok:1.18.32")
    implementation ("com.google.code.gson:gson:2.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}