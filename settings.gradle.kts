@file:Suppress("UnstableApiUsage")

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
        maven(url = "https://jitpack.io")
        maven(url = "https://androidx.dev/storage/compose-compiler/repository")
    }
}

rootProject.name = "Fediverse Redirect"
include(":mastodonredirect", ":lemmyredirect", ":shared")
