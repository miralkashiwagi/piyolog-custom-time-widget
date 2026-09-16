plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.piyologtimewidget"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.piyologtimewidget"
        minSdk = 26
        targetSdk = 34
        versionCode = 2
        versionName = "1.1"
    }

    signingConfigs {
        create("release") {
            // These are provided as environment variables by the GitHub Actions
            // workflow (decoded from secrets). This keystore is fixed, so every
            // build produces an APK signed with the same key and can be
            // reinstalled/updated on a device without a signature conflict.
            // Note: this keystore is PKCS12 (the keytool default since JDK 9),
            // which requires the store password and key password to be
            // identical, so we reuse the same value for both.
            val ksPath = System.getenv("RELEASE_KEYSTORE_PATH")
            if (ksPath != null) {
                storeFile = file(ksPath)
                storePassword = System.getenv("RELEASE_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("RELEASE_KEY_ALIAS")
                keyPassword = System.getenv("RELEASE_KEYSTORE_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            // Always sign with the fixed release key (falls back to debug only
            // for local builds where the env vars above aren't set).
            signingConfig = if (System.getenv("RELEASE_KEYSTORE_PATH") != null) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
}
