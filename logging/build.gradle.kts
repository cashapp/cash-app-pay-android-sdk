import com.vanniktech.maven.publish.AndroidSingleVariantLibrary

plugins {
  id("com.android.library")
  id("org.jetbrains.kotlin.android")
  id("com.vanniktech.maven.publish.base")
}

//https://issuetracker.google.com/issues/226095015
com.android.tools.analytics.AnalyticsSettings.optedIn = false

android {
  namespace = "app.cash.paykit.logging"
  compileSdk = 33

  defaultConfig {
    minSdk = 21

    consumerProguardFiles("consumer-rules.pro")
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  kotlin {
    jvmToolchain(11)
  }

  lint {
    abortOnError = true
    htmlReport = true
    warningsAsErrors = true
    checkAllWarnings = true
    baseline = file("lint-baseline.xml")
  }
}

val junit_version = rootProject.extra["junit_version"] as String
val google_truth_version = rootProject.extra["google_truth_version"] as String
val robolectric_version = rootProject.extra["robolectric_version"] as String

dependencies {
  testImplementation("junit:junit:$junit_version")
  testImplementation("com.google.truth:truth:$google_truth_version")
  testImplementation("org.robolectric:robolectric:$robolectric_version")
}

mavenPublishing {
  // AndroidMultiVariantLibrary(publish a sources jar, publish a javadoc jar)
  configure(AndroidSingleVariantLibrary("release", true, true))
}