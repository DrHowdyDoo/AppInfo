plugins {
    id("com.android.application")
}

android {
    namespace = "com.drhowdydoo.appinfo"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.drhowdydoo.appinfo"
        minSdk = 27
        targetSdk = 36
        versionCode = 11
        versionName = "1.1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation("androidx.lifecycle:lifecycle-viewmodel:2.9.2")
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.8.0")) // To fix duplicate class error (org.jetbrains.kotlin:kotlin-stdlib:1.8.10)
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14") // To detect memory leaks
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.14.0-alpha04")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("com.github.bumptech.glide:glide:5.0.4")
    implementation("io.reactivex.rxjava3:rxjava:3.1.11")
    implementation("io.reactivex.rxjava3:rxandroid:3.0.2")
    implementation("me.zhanghai.android.fastscroll:library:1.3.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}