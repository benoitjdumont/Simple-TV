plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "ca.benoitjdumont.simpletv"
    compileSdk = 34

    defaultConfig {
        applicationId = "ca.benoitjdumont.simpletv"
        minSdk = 26
        targetSdk = 34
        versionCode = 3
        versionName = "2.1.0"
    }

    // Same test key for every build, so new APKs install over old ones without uninstalling.
    // This is a test key, not a release key: it is safe to commit.
    signingConfigs {
        getByName("debug") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.activity:activity-ktx:1.9.0")
    implementation("androidx.documentfile:documentfile:1.0.1")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
}
