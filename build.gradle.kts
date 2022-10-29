plugins {
    id("com.android.application") version "7.3.0" apply false
    @Suppress("DSL_SCOPE_VIOLATION", "UnstableApiUsage")
    kotlin("jvm") version "1.7.20" apply false
}

buildscript {
    dependencies {
        classpath(kotlin("gradle-plugin", version = "1.7.20"))
    }
}
