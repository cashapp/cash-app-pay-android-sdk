import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.paparazzi)
  alias(libs.plugins.maven.publish)
}

android {
  namespace = "app.cash.paykit.ui.compose"
  compileSdk = libs.versions.compileSdk.get().toInt()
  testOptions {
    unitTests {
      isIncludeAndroidResources = true
    }
  }

  defaultConfig {
    minSdk = libs.versions.minSdk.get().toInt()
    consumerProguardFiles("consumer-rules.pro")
  }

  buildTypes {
    release {
      isMinifyEnabled = false
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

  resourcePrefix = "cap_compose_"

  buildFeatures {
    compose = true
  }

  lint {
    abortOnError = true
    htmlReport = true
    checkAllWarnings = true
    warningsAsErrors = true
    baseline = file("lint-baseline.xml")
    lintConfig = file("lint.xml")
    disable += setOf("GradleDependency", "AndroidGradlePluginVersion", "NewerVersionAvailable")
  }
}

dependencies {
  api(libs.compose.ui)
  implementation(platform(libs.compose.bom))
  implementation(libs.compose.foundation)
  implementation(libs.compose.material3)
  implementation(libs.compose.ui.tooling.preview)

  debugImplementation(libs.compose.ui.tooling)

  lintChecks(libs.compose.lints)

  testImplementation(libs.junit)
  testImplementation(libs.paparazzi)
}

mavenPublishing {
  configure(AndroidSingleVariantLibrary("release", sourcesJar = true, publishJavadocJar = true))
}
