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
    implementation ("androidx.activity:activity-compose:1.9.0")
    implementation ("io.coil-kt:coil-compose:2.2.2") // 画像ライブラリの追加
    implementation ("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.wear.compose:compose-material:1.3.1") // Navigationの依存関係
    implementation ("androidx.compose.material:material-icons-extended:x.x.x")// さらに必要な依存関係を追加

    implementation(platform("com.google.firebase:firebase-bom:32.4.1"))
    debugImplementation("androidx.compose.ui:ui-tooling:1.6.7")
    implementation ("androidx.preference:preference-ktx:1.2.0")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")

    implementation ("com.google.firebase:firebase-storage-ktx")
    implementation ("com.google.firebase:firebase-storage:20.2.0")

    implementation ("androidx.compose.foundation:foundation:1.3.1")
    implementation ("com.google.accompanist:accompanist-swiperefresh:0.23.1")

    implementation ("com.google.zxing:core:3.4.1")
    implementation ("com.google.zxing:android-core:3.3.0")
    implementation ("androidx.camera:camera-core:1.2.1")
    implementation ("androidx.camera:camera-camera2:1.2.1")

    implementation ("androidx.camera:camera-lifecycle:1.2.1")
    implementation ("androidx.camera:camera-view:1.2.1")
    implementation ("androidx.camera:camera-extensions:1.2.1")
    implementation ("com.google.accompanist:accompanist-permissions:0.28.0")
    implementation ("androidx.compose.animation:animation:1.3.0")

    implementation ("com.google.firebase:firebase-firestore-ktx")

    implementation("com.google.android.exoplayer:exoplayer:2.17.1")
    implementation("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")
}
