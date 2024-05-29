plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")

    id("com.google.firebase.crashlytics")
}

android {
    namespace = "app.nakanishi.daiki.magic"
    compileSdk = 34

    defaultConfig {
        applicationId = "app.nakanishi.daiki.magic"
        minSdk = 25
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

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
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packagingOptions {
        resources {
            excludes += "META-INF/androidx.compose.material3_material3.version"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/NOTICE.txt"
            excludes += "META-INF/NOTICE"
        }
    }

}

dependencies {

    implementation ("androidx.core:core-ktx:1.13.1")
    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation ("com.google.android.material:material:1.12.0")
    implementation ("androidx.compose.ui:ui:1.6.7")
    implementation ("androidx.compose.material3:material3:1.2.1")
    implementation ("androidx.compose.ui:ui-tooling-preview:1.6.7")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation ("androidx.activity:activity-compose:1.9.0")
    implementation ("io.coil-kt:coil-compose:2.2.2") // 画像ライブラリの追加
    implementation ("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.wear.compose:compose-material:1.3.1") // Navigationの依存関係
    implementation ("androidx.compose.material:material-icons-extended:x.x.x")// さらに必要な依存関係を追加

    implementation(platform("com.google.firebase:firebase-bom:32.4.1"))
    implementation("com.google.firebase:firebase-firestore-ktx")
    debugImplementation("androidx.compose.ui:ui-tooling:1.6.7")
    implementation ("androidx.preference:preference-ktx:1.2.0")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")

    implementation ("com.google.firebase:firebase-storage-ktx")
    implementation ("com.google.firebase:firebase-storage:20.2.0")
}