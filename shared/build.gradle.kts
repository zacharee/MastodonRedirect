import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.bugsnag)
    id("kotlin-parcelize")
}

val javaVersion = JavaVersion.toVersion(rootProject.extra["java.version"].toString().toInt())

android {
    namespace = "dev.zwander.shared"
    compileSdk = rootProject.extra["compile.sdk"].toString().toInt()

    defaultConfig {
        minSdk = rootProject.extra["min.sdk"].toString().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        all {
            buildConfigField("int", "VERSION_CODE", rootProject.extra["version.code"].toString())
            buildConfigField("String", "VERSION_NAME", "\"${rootProject.extra["version.name"]}\"")
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

bugsnag {
    buildUuid = project.android.defaultConfig.manifestPlaceholders["build_uuid"].toString()
}

dependencies {
    api(libs.androidx.core.ktx)
    api(libs.androidx.lifecycle.runtime.ktx)
    api(libs.androidx.lifecycle.runtime.compose)
    api(libs.androidx.preferences)
    api(libs.androidx.datastore.preferences)

    api(platform(libs.compose.bom))
    api(libs.compose.activity)
    api(libs.compose.ui)
    api(libs.compose.ui.graphics)
    api(libs.compose.ui.tooling.preview)
    api(libs.compose.material3)

    api(libs.kotlin.reflect)
    api(libs.kotlinx.serialization)
    api(libs.ktor.client.android)
    api(libs.ktor.client.auth)

    api(libs.shizuku.api)
    api(libs.shizuku.provider)

    api(libs.patreonSupportersRetrieval)
    api(libs.linkSheet)
    api(libs.material.components)
    api(libs.hiddenApiBypass)
    api(libs.bugsnag.android)
    api(libs.mvvm.flow.compose)

    api(libs.apk.parser)

    debugApi(libs.compose.ui.tooling)
}
