import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// Load secrets from local.properties (gitignored)
val localProps = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.reader()?.use { load(it) }
}
fun local(key: String) = localProps.getProperty(key, "")

android {
    namespace = "iss.nus.edu.sg.weather"
    compileSdk = 37

    defaultConfig {
        applicationId = "iss.nus.edu.sg.weather"
        minSdk = 24
        targetSdk = 36
        versionCode = 2
        versionName = "1.103"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "QWEATHER_KID", "\"${local("qweatherKid")}\"")
        buildConfigField("String", "QWEATHER_SUB", "\"${local("qweatherSub")}\"")
        buildConfigField("String", "QWEATHER_PRIVATE_KEY", "\"${local("qweatherPrivateKey")}\"")
        buildConfigField("String", "QWEATHER_API_HOST", "\"${local("qweatherApiHost")}\"")
    }

    signingConfigs {
        create("release") {
            storeFile = file(local("KEYSTORE_PATH"))
            storePassword = local("KEYSTORE_PASSWORD")
            keyAlias = local("KEY_ALIAS")
            keyPassword = local("KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.gson)
    implementation(libs.okhttp)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.eddsa)
    implementation(libs.kotlinx.coroutines.android)
}

// Copy release APK to specified output folder
tasks.register<Copy>("exportReleaseApk") {
    dependsOn("assembleRelease")
    from("build/outputs/apk/release")
    include("*.apk")
    into("D:/AndroidAppOutput/release")
}
