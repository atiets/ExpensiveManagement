pluginManagement {
    repositories {
        google {
            content {
                includeGroup("com.android.*")
                includeGroup("com.google.*")
                includeGroup("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        kotlin("jvm") version "2.0.20"
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ExpensiveManagement"
include(":app")
 