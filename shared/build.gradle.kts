import org.jetbrains.kotlin.gradle.dsl.JvmTarget

@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.apollo)
    alias(libs.plugins.kotlin.compose)
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
        debug {
            isMinifyEnabled = false
        }
        all {
            buildConfigField("int", "VERSION_CODE", rootProject.extra["version.code"].toString())
            buildConfigField("String", "VERSION_NAME", "\"${rootProject.extra["version.name"]}\"")
        }
    }
    val javaVersion = JavaVersion.toVersion(rootProject.extra["java.version"].toString().toInt())
    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(javaVersion.majorVersion))
        }
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

apollo {
    service("fediverseObserver") {
        packageName.set("${android.namespace}.generated")

        introspection {
            endpointUrl.set("https://api.fediverse.observer")
            schemaFile.set(File("src/main/graphql/schema.json"))
        }
    }
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

    api(libs.apollo.runtime)

    debugApi(libs.compose.ui.tooling)
}