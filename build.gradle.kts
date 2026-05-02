// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.bugsnag) apply false
    alias(libs.plugins.apollo) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
