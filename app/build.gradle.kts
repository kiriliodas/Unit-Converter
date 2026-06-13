plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.blood.unitconverter"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.blood.unitconverter"
        minSdk = 24
        targetSdk = 35
        versionCode = 10
        versionName = "1.5.2"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            // Code shrinking (R8) for a lean, fast APK. The only custom keep
            // rules are for kotlinx-serialization (see proguard-rules.pro).
            //
            // Resource shrinking is intentionally DISABLED: it was stripping the
            // launcher-icon resources from the release APK (debug was unaffected
            // because debug never shrinks), causing the default Android icon to
            // show. The app has very few resources, so the size win wasn't worth
            // the breakage.
            isMinifyEnabled = true
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Signed with the debug key so the GitHub Actions build produces an
            // installable APK without managing secrets. Replace for Play Store.
            signingConfig = signingConfigs.getByName("debug")
        }
        debug {
            applicationIdSuffix = ".debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.graphics.shapes)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.serialization.json)
    debugImplementation(libs.androidx.ui.tooling)
}
