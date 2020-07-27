plugins {
    kotlin("jvm")
    id(Plugins.base)
    id(Plugins.jmh)
}

basePlugin {
    module = "collections-extensions"
    experimentalContracts = true
}

jmh {
    jvmArgs.plusAssign("-Xms1024m")
    jvmArgs.plusAssign("-Xmx2048m")
    duplicateClassesStrategy = DuplicatesStrategy.WARN
    isIncludeTests = true
}

dependencies {
    jmhImplementation(Libs.jmh)
    testImplementation(project(":testing"))
}


version = "0.3"