plugins {
    id("com.android.application")
}

android {
    namespace = "com.drhowdydoo.appinfo"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.drhowdydoo.appinfo"
        minSdk = 27
        targetSdk = 33
        versionCode = 3
        versionName = "1.0.0"

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

    implementation("androidx.lifecycle:lifecycle-viewmodel:2.6.1")
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.8.0")) // To fix duplicate class error (org.jetbrains.kotlin:kotlin-stdlib:1.8.10)
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.12") // To detect memory leaks
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.recyclerview:recyclerview:1.3.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("com.github.bumptech.glide:glide:4.15.1")
    implementation("io.reactivex.rxjava3:rxjava:3.1.6")
    implementation("io.reactivex.rxjava3:rxandroid:3.0.2")
    implementation("me.zhanghai.android.fastscroll:library:1.2.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}