plugins {
    alias(libs.plugins.android.application)
    `maven-publish`
}

android {
    namespace = "com.example.calendar3"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.calendar3"
        minSdk = 26
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

    packaging {
        resources {
            excludes += listOf(
                "META-INF/INDEX.LIST",
                "META-INF/DEPENDENCIES",
                "META-INF/ASL2.0",
                "META-INF/LGPL2.1"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.fragment:fragment:1.8.2")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation("androidx.activity:activity:1.9.1")
    implementation("com.google.protobuf:protobuf-java:3.21.12")
    implementation("com.google.android.gms:play-services-vision:20.1.3")
    implementation("com.google.http-client:google-http-client-jackson2:1.45.0")
    implementation("com.google.cloud:google-cloud-vision:3.46.0")
    implementation("com.google.api-client:google-api-client:2.6.0")
    implementation("com.google.api-client:google-api-client-android:2.6.0")
    implementation("com.google.api-client:google-api-client-gson:2.6.0")
    implementation("com.google.apis:google-api-services-vision:v1-rev451-1.25.0")
    implementation("com.fasterxml.jackson.core:jackson-core:2.17.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.17.2")
    implementation("com.google.cloud:google-cloud-core:2.42.0")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.google.android.gms:play-services-identity:18.1.0")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.24.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    implementation(platform("com.google.cloud:libraries-bom:26.44.0"))
}
