# android-cash-paykit-sdk

Cash Android PayKit SDK for merchant integrations with Cash App Pay.

### CI

Github Actions will build our artifacts. 
#### Sample App

The sample app is build via [this job](https://kochiku.sqprod.co/squareup/android-cash-paykit-sdk), and uploads the APK to [go/mr](https://mobile-releases.squareup.com/cash-apps)


### RELEASING
The SDK artifact will be uploaded to Maven Central (SonaType). Snapshots will be uploaded to the snapshots repository. 

The Github Actions build configuration determines which repository is used. If the version name contains "SNAPSHOT", it will be uploaded to the snapshots repository. If it contains a normal SEMVER, then it will upload to Maven Central.



#### Maven Publishing
Create a new tag with the format `v{SEMVER}` and publish the tag to git. 

Github actions should build and upload the AAR artifacts using the version declared in the root [build.gradle](./build.gradle)