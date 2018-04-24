group = "pt.um.lei.masb"
version = "1.0"


plugins {
    `java-library`
}

dependencies {
    //compile("io.vertx", "vertx-mongo-client", vertxVersion)
    testCompile("org.junit.jupiter", "junit-jupiter-api", project.ext["junitVersion"] as String)
    testRuntime("org.junit.jupiter", "junit-jupiter-params", project.ext["junitVersion"] as String)
    compile("com.google.code.gson", "gson", project.ext["gsonVersion"] as String)
    implementation("org.bouncycastle", "bcprov-jdk15on", project.ext["bouncyCastleVersion"] as String)
    implementation("org.openjdk.jol", "jol-core", project.ext["jolVersion"] as String)
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "1.10"
    targetCompatibility = "1.10"
}
