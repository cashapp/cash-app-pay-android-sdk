import com.vanniktech.maven.publish.AndroidSingleVariantLibrary

plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.maven.publish)
}

android {
  namespace = "app.cash.paykit.analytics"
  compileSdk = libs.versions.compileSdk.get().toInt()

  defaultConfig {
    minSdk = libs.versions.minSdk.get().toInt()

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    consumerProguardFiles("consumer-rules.pro")
  }

  resourcePrefix = "paykit_analytics_"

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  kotlinOptions {
    jvmTarget = "17"
  }

  kotlin {
    jvmToolchain(17)
  }

  lint {
    abortOnError = true
    htmlReport = true
    checkAllWarnings = true
    warningsAsErrors = true
    baseline = file("lint-baseline.xml")
  }

  buildFeatures {
    buildConfig = true
  }
}

dependencies {
  testImplementation(libs.junit)
  testImplementation(libs.mockk)

  implementation(project(":logging"))

  // Robolectric environment.
  testImplementation(libs.robolectric)
}

mavenPublishing {
  // AndroidMultiVariantLibrary(publish a sources jar, publish a javadoc jar)
  configure(AndroidSingleVariantLibrary("release", sourcesJar = true, publishJavadocJar = true))
}
