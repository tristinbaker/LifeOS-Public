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
include(":modules:lifeos_habittracker")
include(":modules:lifeos_medialogger")
include(":modules:lifeos_sleeptracker")
include(":modules:lifeos_journal")
include(":modules:lifeos_sports")
include(":modules:lifeos_physicalmedia")
include(":modules:lifeos_financetracker")
include(":modules:lifeos_aiinsights")
