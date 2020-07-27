plugins {
    kotlin("jvm")
    id(Plugins.serial)
}

serialPlugin {
    inlineClasses = true
    packageName = "org.knowledger.ledger.core.data"
    module = "ledger/core/data"
}
