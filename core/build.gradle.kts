import com.vanniktech.maven.publish.AndroidSingleVariantLibrary

plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.ksp)
  id("project-report") // run ./gradlew htmlDependencyReport
  alias(libs.plugins.maven.publish)
}

val sourceJar by tasks.registering(Jar::class) {
  from(android.sourceSets["main"].java.srcDirs)
  archiveClassifier.set("sources")
}

android {
  namespace = "app.cash.paykit.core"
  compileSdk = libs.versions.compileSdk.get().toInt()

  defaultConfig {
    minSdk = libs.versions.minSdk.get().toInt()

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    consumerProguardFiles("consumer-rules.pro")
    resValue("string", "cap_version", version.toString())
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }

  compileOptions {
    isCoreLibraryDesugaringEnabled = true
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  kotlinOptions {
    jvmTarget = "17"
  }

  kotlin {
    jvmToolchain(17)
  }

  resourcePrefix = "cap_"

  lint {
    abortOnError = true
    htmlReport = true
    checkAllWarnings = true
    warningsAsErrors = true
    baseline = file("lint-baseline.xml")
    // Disable version checks - versions are intentionally pinned
    disable += setOf("GradleDependency", "AndroidGradlePluginVersion", "NewerVersionAvailable")
  }

  testOptions {
    unitTests {
      isReturnDefaultValues = true
      isIncludeAndroidResources = true
    }
  }

  buildFeatures {
    buildConfig = true
  }
}

dependencies {
  coreLibraryDesugaring(libs.desugar.jdk.libs)

  // We want to lock this dependency at a lower than latest version to not force transitive updates onto merchants.
  ksp(libs.moshi.kotlin.codegen)
  implementation(libs.moshi.kotlin)

  // Provides a lifecycle for the whole application process.
  implementation(libs.lifecycle.process)

  // AndroidX Startup.
  implementation(libs.startup.runtime)

  implementation(libs.okhttp)

  implementation(project(":logging"))
  implementation(project(":analytics-core"))

  // TEST RELATED.

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
  testImplementation(libs.truth)
  androidTestImplementation(libs.junit.androidx)
  testImplementation(libs.okhttp.mockwebserver)
  // Robolectric environment.
  testImplementation(libs.robolectric)
  // Coroutines test support.
  testImplementation(libs.coroutines.test)
  // Test helpers for Lifecycle runtime
  testImplementation(libs.lifecycle.runtime.testing)
}

mavenPublishing {
  // AndroidMultiVariantLibrary(publish a sources jar, publish a javadoc jar)
  configure(AndroidSingleVariantLibrary("release", sourcesJar = true, publishJavadocJar = true))
}
