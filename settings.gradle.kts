rootProject.name = "Knowledger"
include(
    "agent", "agent:ontologies", "agent:publish", "annotations", "base64-extensions",
    "benchmarks", "collections-extensions", "example", "ledger", "ledger:core",
    "ledger:core:crypto", "ledger:core:data", "ledger:core:db", "ledger:core:kserial",
    "ledger:orient", "ledger:storage", "generation", "results", "testing"
)

pluginManagement {
    repositories {
        gradlePluginPortal()
        jcenter()
    }
}