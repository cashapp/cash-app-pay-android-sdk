import com.android.tools.analytics.AnalyticsSettings
import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.maven.publish)
}

// https://issuetracker.google.com/issues/226095015
AnalyticsSettings.optedIn = false

android {
  namespace = "app.cash.paykit.logging"
  compileSdk = libs.versions.compileSdk.get().toInt()

  defaultConfig {
    minSdk = libs.versions.minSdk.get().toInt()

    consumerProguardFiles("consumer-rules.pro")
  }

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

  kotlin {
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_17)
    }
  }

  lint {
    abortOnError = true
    htmlReport = true
    warningsAsErrors = true
    checkAllWarnings = true
    baseline = file("lint-baseline.xml")
  }
}

dependencies {
  testImplementation(libs.junit)
  testImplementation(libs.truth)
  testImplementation(libs.robolectric)
}

mavenPublishing {
  configure(AndroidSingleVariantLibrary("release", sourcesJar = true, publishJavadocJar = true))
}