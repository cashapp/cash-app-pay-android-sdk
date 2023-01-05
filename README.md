# android-cash-paykit-sdk

Cash Android PayKit SDK for merchant integrations with Cash App Pay.

### CI

We use Kochiku to build or SDK and our sample app. 
#### Sample App

The sample app is build via [this job](https://kochiku.sqprod.co/squareup/android-cash-paykit-sdk), and uploads the APK to [go/mr](https://mobile-releases.squareup.com/cash-apps)


### RELEASING
The SDK artifact will be deployed as an AAR to our public [artifactory repository](https://artifactory.global.square/ui/repos/tree/General/releases/)

#### Locally
1.) Ensure connection to SQ VPN
2.) Update `LIB_VERSION` in build.gradle
3.) Ensure your username and [Artifactory API key](https://artifactory.global.square/artifactory/webapp/#/profile) are available as SONATYPE_NEXUS_USERNAME and SONATYPE_NEXUS_PASSWORD  in ~/.gradle/gradle.properties (or you can add them with additional -P flags on the next step's command).
4.) Run ./gradlew publish -PRELEASE_REPOSITORY_URL="https://maven.global.square/artifactory/releases"