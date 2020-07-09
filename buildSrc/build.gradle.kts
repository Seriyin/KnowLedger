repositories {
    mavenCentral()
    google()
    jcenter()
}

plugins {
    `kotlin-dsl`
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}

dependencies {
    val kotlinVersion by extra {
        "1.3.72"
    }
    val dokkaVersion by extra {
        "0.10.1"
    }
    val dokkaPlugin by extra {
        "org.jetbrains.dokka:dokka-gradle-plugin:${dokkaVersion}"
    }

    implementation(kotlin("gradle-plugin", kotlinVersion))
    implementation(dokkaPlugin)
    implementation(gradleApi())
    implementation(localGroovy())
}