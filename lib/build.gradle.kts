plugins {
    `maven-publish`
    id("kotlin")
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation("com.google.truth:truth:1.1.3")
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("library") {
                from(components["java"])
                group = LibraryInfo.group
                groupId = LibraryInfo.group
                version = LibraryInfo.version
                artifactId = LibraryInfo.artifactId

                pom {
                    name.set(LibraryInfo.artifactId)
                    description.set(LibraryInfo.description)
                    url.set(LibraryInfo.url)

                    scm {
                        connection.set("scm:git@github.com:tkhskt/glsl-tokenizer-kt.git")
                        developerConnection.set("scm:git@github.com:tkhskt/glsl-tokenizer-kt.git")
                        url.set(LibraryInfo.url)
                    }

                    developers {
                        developer {
                            id.set("tkhskt")
                            name.set("Keita Takahashi")
                        }
                    }
                }
            }
        }
    }
}

project(":lib") {
    version = LibraryInfo.version
}

object LibraryInfo {
    const val artifactId = "shiirudo"
    const val description = "Sealed Class"
    const val displayName = "shiirudo"
    const val group = "com.tkhskt"
    const val url = "https://github.com/tkhskt/shiirudo"
    const val version = "1.0.0"
}
