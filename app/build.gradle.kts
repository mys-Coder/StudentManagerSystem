plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.studentmanagersystem"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.studentmanagersystem"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }

    // 两个风味：standard=正式包（包名不变，覆盖旧版）；preview=适配预览包（包名加 .preview，可与旧版共存）
    flavorDimensions += "edition"
    productFlavors {
        create("standard") {
            dimension = "edition"
            manifestPlaceholders["appLabel"] = "StudentManagerSystem"
        }
        create("preview") {
            dimension = "edition"
            applicationIdSuffix = ".preview"
            manifestPlaceholders["appLabel"] = "学生信息管理系统(适配版)"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}