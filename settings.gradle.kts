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
        maven(url = "https://androidx.dev/snapshots/builds/10466649/artifacts/repository")
        maven(url = "https://jitpack.io")
    }
}

rootProject.name = "Fediverse Redirect"
include(":mastodonredirect", ":lemmyredirect", ":shared", ":linksheet-interconnect")
project(":linksheet-interconnect").projectDir = File("../LinkSheet/interconnect")
 