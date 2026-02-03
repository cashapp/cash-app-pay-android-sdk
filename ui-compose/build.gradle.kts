import com.vanniktech.maven.publish.AndroidSingleVariantLibrary

plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.maven.publish)
}

android {
  namespace = "app.cash.paykit.ui.compose"
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

  kotlinOptions {
    jvmTarget = "17"
  }

  resourcePrefix = "cap_"

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
  implementation(platform(libs.compose.bom))
  implementation(libs.compose.foundation)
  implementation(libs.compose.material)
  implementation(libs.compose.ui)
  implementation(libs.compose.ui.tooling.preview)

  debugImplementation(libs.compose.ui.tooling)

  lintChecks(libs.compose.lints)

  testImplementation(libs.junit)
}

mavenPublishing {
  configure(AndroidSingleVariantLibrary("release", sourcesJar = true, publishJavadocJar = true))
}
