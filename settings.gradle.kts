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

        // PENTING: Library Grafik (Jitpack)
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "sofcurrency"
include(":app")