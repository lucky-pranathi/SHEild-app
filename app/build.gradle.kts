plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.lucky.sheild"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.lucky.sheild"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("com.google.firebase:firebase-database:22.0.1")
    implementation("com.google.firebase:firebase-auth:24.0.1")
    implementation("com.google.firebase:firebase-firestore:25.1.3")
    implementation("androidx.datastore:datastore-preferences:1.1.1")


    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    implementation("com.karumi:dexter:6.2.3")
    implementation("com.google.firebase:firebase-storage:21.0.1")
    implementation("com.squareup.picasso:picasso:2.8")

    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.google.android.gms:play-services-location:21.0.1")

    implementation(platform("com.google.firebase:firebase-bom:32.2.0")) // use latest BoM

    // Firebase Functions
    implementation("com.google.firebase:firebase-functions-ktx") // Kotlin-friendly version, works with Java too
//    implementation("androidx.appcompat:appcompat:1.7.1")
//    implementation("com.google.android.material:material:1.13.0")
//    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
//    testImplementation("junit:junit:4.13.2")
//    androidTestImplementation("androidx.test.ext:junit:1.3.0")
//    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
//
//    implementation("com.google.firebase:firebase-bom:33.3.0")
//
//    // --- Firebase services you need ---
//    implementation("com.google.firebase:firebase-database")     // Realtime DB
//    implementation("com.google.firebase:firebase-auth")        // Optional: login
//    implementation("com.google.firebase:firebase-storage")       // Optional: image/data storage
//    implementation("com.google.firebase:firebase-analytics")    // Analytics
//
//    // --- Bluetooth communication ---
//    implementation("androidx.appcompat:appcompat:1.7.0")
//    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
//    implementation("com.google.android.material:material:1.12.0")
//
//    // Optional libraries (if you want clean threading / async work)
//    implementation("androidx.lifecycle:lifecycle-runtime:2.8.4")
//    implementation("androidx.activity:activity:1.9.2")
}