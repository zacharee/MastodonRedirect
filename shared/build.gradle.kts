@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    id("kotlin-parcelize")
}

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
        kotlinCompilerExtensionVersion = rootProject.extra["compose.compiler.extension"].toString()
    }
    packaging {
        resources.excludes.add("META-INF/versions/9/previous-compilation-data.bin")
    }
}

dependencies {
    api(libs.core.ktx)
    api(libs.lifecycle.runtime.ktx)
    api(libs.activity.compose)
    api(platform(libs.compose.bom))
    api(libs.ui)
    api(libs.ui.graphics)
    api(libs.ui.tooling.preview)
    api(libs.material3)
    api(libs.materialComponents)
    api(libs.preferences)
    api(libs.hiddenApiBypass)
    api(libs.lifecycleRuntimeCompose)
    api(libs.ktor.client.android)
    api(libs.ktor.client.auth)
    api(libs.kotlinx.serialization)
    api(libs.bugsnag.android)
    api(libs.accompanist.themeadapter.material3)
    api(libs.kotlin.reflect)

    api(libs.shizuku.api)
    api(libs.shizuku.provider)

    api(libs.patreonSupportersRetrieval)

    api(project(":linksheet-interconnect"))

    debugApi(libs.ui.tooling)
}