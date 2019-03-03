import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.noarg")
//    id("kotlinx-serialization")
}

repositories {
    maven("https://kotlin.bintray.com/ktor")
    maven("http://jade.tilab.com/maven")
}

dependencies {
    //Regular dependencies
    implementation(kotlin("stdlib", Versions.kotlin))
    implementation(project(":blockchain"))
    //implementation(Libs.arrowK)
    implementation(Libs.coroutines)
    implementation(Libs.eclipsePaho)
    Libs.jade.forEach {
        implementation(it)
    }
    implementation(Libs.klog)
    Libs.ktor.forEach {
        implementation(it)
    }
    //implementation(Libs.serialization)
    Libs.slf4j.forEach {
        runtimeOnly(it)
    }

    //Test dependencies
    testImplementation(Libs.assertK)
    testImplementation(Libs.jUnitApi)
    Libs.jUnitRuntime.forEach {
        testRuntimeOnly(it)
    }
}


configure<ApplicationPluginConvention> {
    mainClassName = "pt.um.lei.masb.agent.Container"
}

tasks.withType<Test> {
    useJUnitPlatform {
        includeEngines("junit-jupiter")
    }
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}