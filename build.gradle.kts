import com.vanniktech.maven.publish.SonatypeHost

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.binary.compatibility.validator) apply false
  alias(libs.plugins.spotless)
  alias(libs.plugins.maven.publish)
}

subprojects {
  group = "app.cash.paykit"
  version = "2.6.1-SNAPSHOT"

  apply(plugin = "com.diffplug.spotless")
  extensions.configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    kotlin {
      target("src/**/*.kt")
      ktlint("1.8.0").editorConfigOverride(
        mapOf(
          "insert_final_newline" to "true",
          "end_of_line" to "lf",
          "charset" to "utf-8",
          "indent_size" to "2",
          "ktlint_standard_max-line-length" to "disabled",
        ),
      )
      licenseHeaderFile(rootProject.file("gradle/license-header.txt"))
    }
    java {
      target("src/**/*.java")
    }
  }

  plugins.withId("com.vanniktech.maven.publish.base") {
    apply(plugin = "binary-compatibility-validator")
    extensions.configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
      publishToMavenCentral(SonatypeHost.DEFAULT, automaticRelease = true)
      signAllPublications()
      pom {
        description.set("Cash App Pay SDK")
        name.set(project.name)
        url.set("https://github.com/cashapp/android-cash-paykit-sdk/")
        licenses {
          license {
            name.set("The Apache Software License, Version 2.0")
            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            distribution.set("repo")
          }
        }
        scm {
          url.set("https://github.com/cashapp/android-cash-paykit-sdk/")
          connection.set("scm:git:git://github.com/cashapp/android-cash-paykit-sdk.git")
          developerConnection.set("scm:git:ssh://git@github.com/cashapp/android-cash-paykit-sdk.git")
        }
        developers {
          developer {
            id.set("square")
            name.set("Square, Inc.")
          }
        }
      }
    }
  }
}
