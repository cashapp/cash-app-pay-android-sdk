#!/usr/bin/env bash

unset DISPLAY

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_DIR=$(dirname "$SCRIPT_DIR")
ANDROID_HOME="$HOME/android-sdk"
SHARED_CACHE="/mnt/nfs/shared-cache/android/cash"
GW_PROPERTIES="$REPO_DIR/gradle/wrapper/gradle-wrapper.properties"

set -e

if [[ "$TEST_RUNNER" == "on_success" ]]; then
  scripts/on-success
  exit
fi

export ANDROID_HOME
# Workaround for https://issuetracker.google.com/issues/148189425
rm -rf "$ANDROID_HOME"/ndk*

echo "sdk.dir=$ANDROID_HOME" >local.properties

mkdir -p "$ANDROID_HOME/licenses" || true
echo -e "24333f8a63b6825ea9c5514f83c2829b004d1fee" >"$ANDROID_HOME/licenses/android-sdk-license"

# On local builds, use gradle-all for debuggable sources.
# On CI builds, use gradle-bin for faster downloading.
echo "Swapping gradle-all for gradle-bin in ${GW_PROPERTIES}"
sed -i 's/-all/-bin/g' "${GW_PROPERTIES}"

####################################################################################################
#### MAIN BUILD
####################################################################################################
# Swap the Gradle properties -- which are tailored for local builds -- with a CI-specific config.
rm gradle.properties
mv gradle.properties.kochiku gradle.properties

# Point the build cache onto the shared NFS drive so it can be reused by all workers.
# echo "android.buildCacheDir=$SHARED_CACHE/build-cache" >> gradle.properties

# Set nonProxyHosts in gradle.properties so CI can access maven.global.square without going through cloudproxy
echo "systemProp.https.nonProxyHosts=*.square|*.squareup.com|*.sqcorp.co" >>gradle.properties
echo "systemProp.http.nonProxyHosts=*.square|*.squareup.com|*.sqcorp.co" >>gradle.properties

buildDebug() {
  JAVA_OPTS="-Xmx2048M" ./gradlew sample-app:assembleDebug --no-daemon
}

publishDebug() {
  echo "publishing"
  pwd

  source $SCRIPT_DIR/mobile-releases.sh

  # For signing with release cert
  # sign_apk "android/app/build/outputs/apk/release/app-release.apk" "android/app/build/outputs/apk/release/app-release-signed.apk"
  upload_artifact "sample-app/build/outputs/apk/debug/sample-app-debug.apk" "debug" "apk" "$PACKAGE_VERSION"

  # we are publishing when uploading. no need to set visibility
  # publish_artifact "release" "$GIT_COMMIT"
}

echo "Running ${TEST_RUNNER}..."

# TEST_RUNNER cases should match targets.type in kochiku.yml
case "${TEST_RUNNER}" in
buildDebug)
  buildDebug
  ;;
publishDebug)
  publishDebug
  ;;
*)
  echo "Unknown TEST_RUNNER value: ${TEST_RUNNER}"
  exit 1
  ;;
esac
