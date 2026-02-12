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
uploaded to : https://central.sonatype.com/repository/maven-snapshots/

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