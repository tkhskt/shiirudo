plugins {
    kotlin("jvm")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {

    implementation(libs.stdlib)
    implementation(libs.bundles.kotlin.poet)

    implementation(libs.ksp)
}
