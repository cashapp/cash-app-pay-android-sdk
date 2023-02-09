[![release](https://github.com/cashapp/android-cash-paykit-sdk/actions/workflows/release.yaml/badge.svg)](https://github.com/cashapp/android-cash-paykit-sdk/actions/workflows/release.yaml)

# Android Cash PayKit SDK

Cash Android PayKit SDK for merchant integrations with Cash App Pay.

Project information can be found
here: https://www.notion.so/cashappcash/Android-PayKit-SDK-a45d3902660146bca1fe5ed3288236d3

## About this repo

This is the **private** repository for Cash App PayKit SDK for Android.

# CI

Github Actions will build our artifacts.

## Sample App

The sample app is build via [this job](https://kochiku.sqprod.co/squareup/android-cash-paykit-sdk),
and uploads the APK to [go/mr](https://mobile-releases.squareup.com/cash-apps)

## Dev App

Auxiliary Dev App can be found [here](https://github.com/squareup/cash-paykit-dev-app-android).

# RELEASING

The SDK artifact will be uploaded to Maven Central (SonaType). Snapshots will be uploaded to the
snapshots repository.

The Github Actions build configuration determines which repository is used. If the version name
contains "SNAPSHOT", it will be uploaded to the snapshots repository. If it contains a normal
SEMVER, then it will upload to Maven Central.

## Maven Publishing

Create a new tag with the format `v{SEMVER}` and publish the tag to git.

Github actions should build and upload the AAR artifacts using the version declared in the
root [build.gradle](./build.gradle)

### Releasing new SNAPSHOT build

To release a new snapshot build do the following steps:

- Open a PR where you increment build version properties `NEXT_VERSION` and `allprojects.version`,
  which can be found in the root `build.gradle` file
- After merging, switch back to `main` and create a tag for the new release and suffix it
  with `-SNAPSHOT`. Eg.: `git tag v0.0.1-SNAPSHOT`
- Push the tag (`git push origin tag_name`)

GitHub Actions will start a release job, and if everything goes well the SNAPSHOT will automatically
uploaded to
the [snapshots repository](https://oss.sonatype.org/index.html#view-repositories;snapshots~browsestorage~/app/cash/paykit/core/maven-metadata.xml)
.

# Development tasks

## Run Android lint on the project

```bash
./gradlew core:lint
```

## Apply Ktlint formatting via Spotless

```bash
./gradlew :core:spotlessApply
```

## Run all Unit Tests

```bash
./gradlew test
```