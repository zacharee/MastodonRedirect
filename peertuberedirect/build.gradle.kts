import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.UUID

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.bugsnagAndroid)
    alias(libs.plugins.kotlin.compose)
}

val javaVersion = JavaVersion.toVersion(rootProject.extra["java.version"].toString().toInt())

android {
    val pkg = "dev.zwander.peertuberedirect"

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

        manifestPlaceholders["build_uuid"] = UUID.nameUUIDFromBytes("PeerTubeRedirect_${versionCode}".toByteArray()).toString()
    }

    buildTypes {
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
    }
    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
    buildFeatures {
        compose = true
        aidl = true
        buildConfig = true
    }
    packaging {
        resources.excludes.add("META-INF/versions/9/previous-compilation-data.bin")
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(javaVersion.majorVersion))
    }
}

base {
    archivesName.set("PeerTubeRedirect_${project.android.defaultConfig.versionName}")
}

dependencies {
    implementation(project(":shared"))
}