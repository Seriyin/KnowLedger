import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id(Plugins.serial)
}

serialPlugin {
    packageName = "org.knowledger.ledger.testing"
    module = "testing"
}

group = "org.knowledger"
version = "0.1"

dependencies {
    implementation(project(":ledger-core"))
    implementation(Libs.commonsRNG)
}

tasks.withType<KotlinCompile> {
    kotlinOptions.freeCompilerArgs += "-Xuse-experimental=kotlin.Experimental"
}
