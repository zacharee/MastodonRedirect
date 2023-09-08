import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.archivesName
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.bugsnagAndroid)
}

android {
    val pkg = "dev.zwander.mastodonredirect"

    namespace = pkg
    compileSdk = rootProject.extra["compile.sdk"].toString().toInt()

    defaultConfig {
        applicationId = pkg
        minSdk = rootProject.extra["min.sdk"].toString().toInt()
        targetSdk = rootProject.extra["target.sdk"].toString().toInt()
        versionCode = rootProject.extra["version.code"].toString().toInt()
        versionName = rootProject.extra["version.name"].toString()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        archivesName = "MastodonRedirect_$versionName"
    }

    buildTypes {
        val localProperties = Properties()
        localProperties.load(project.rootProject.file("local.properties").inputStream())

        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            isMinifyEnabled = false
        }

        all {
            buildConfigField("String", "INSTANCES_SOCIAL_KEY", "\"${localProperties.getOrDefault("instances_social_key", "")}\"")
        }
    }
    val javaVersion = JavaVersion.toVersion(rootProject.extra["java.version"].toString().toInt())
    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
    kotlinOptions {
        jvmTarget = javaVersion.majorVersion
    }
    buildFeatures {
        compose = true
        aidl = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
    packaging {
        resources.excludes.add("META-INF/versions/9/previous-compilation-data.bin")
    }
}

dependencies {
    implementation(project(":shared"))
}