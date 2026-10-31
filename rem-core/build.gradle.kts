plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
}

kotlin {
    android {
        namespace = "dev.rem.core"
        compileSdk = 37
        minSdk = 21
    }

    jvm {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }

    val iosTargets = listOf(iosArm64(), iosSimulatorArm64())
    iosTargets.forEach { target ->
        target.binaries.framework {
            baseName = "RemCore"
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.material.color.utilities)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
