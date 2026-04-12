pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "LifeOS"
include(":app")
include(":modules:lifeos_core")
include(":modules:lifeos_mealtracker")
include(":modules:lifeos_notes")
