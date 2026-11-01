plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "ics.rem"
    compileSdk = 37

    defaultConfig {
        applicationId = "ics.rem"
        minSdk = 23
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":rem-core"))
    implementation(project(":rem-compose"))
    implementation(libs.activity.compose)
}
