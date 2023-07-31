plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinSerialization)
}

apply {
    plugin("com.bugsnag.android.gradle")
}

android {
    namespace = "dev.zwander.lemmyredirect"
    compileSdk = 33

    defaultConfig {
        applicationId = "dev.zwander.lemmyredirect"
        minSdk = 24
        targetSdk = 33
        versionCode = 5
        versionName = "1.3.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("int", "VERSION_CODE", "${defaultConfig.versionCode}")
            buildConfigField("String","VERSION_NAME","\"${defaultConfig.versionName}\"")
        }

        debug {
            buildConfigField("int", "VERSION_CODE", "${defaultConfig.versionCode}")
            buildConfigField("String","VERSION_NAME","\"${defaultConfig.versionName}\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
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
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(project(":shared"))
}