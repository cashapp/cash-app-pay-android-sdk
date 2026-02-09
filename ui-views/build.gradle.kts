import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.maven.publish)
}

android {
  namespace = "app.cash.paykit.ui.views"
  compileSdk = libs.versions.compileSdk.get().toInt()

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

  resourcePrefix = "cap_"

  lint {
    abortOnError = true
    htmlReport = true
    checkAllWarnings = true
    warningsAsErrors = true
    baseline = file("lint-baseline.xml")
    disable += setOf("GradleDependency", "AndroidGradlePluginVersion", "NewerVersionAvailable")
  }
}

dependencies {
  testImplementation(libs.junit)
  testImplementation(libs.robolectric)
}

mavenPublishing {
  configure(AndroidSingleVariantLibrary("release", sourcesJar = true, publishJavadocJar = true))
}
