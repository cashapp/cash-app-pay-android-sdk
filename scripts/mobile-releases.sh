#!/usr/bin/env bash

SECRETS_DIR="/data/app/kochiku-worker/secrets"

set -o errexit
set -o pipefail
set -o nounset
set -o xtrace

# Executed by APK-building shards on 'kochiku-worker' (EC2 workers)
function sign_apk() {
  UNSIGNED_APK="$1"
  SIGNED_APK="$2"

  echo "======================"
  echo "Signing APK"
  echo "======================"

  curl \
    -sS \
    -L \
    -v \
    --fail \
    --connect-timeout 120 \
    --max-time 120 \
    --cacert "$SECRETS_DIR/service2service.ca.pem" \
    --cert-type 'pem' \
    --cert "$SECRETS_DIR/kochiku-worker-s2s.crt" \
    --key "$SECRETS_DIR/kochiku-worker-s2s.key" \
    -o "$SIGNED_APK" \
    -X 'POST' \
    -H "X-Speleo-Trace-Id: $(uuidgen | sed 's/\-//g')" \
    -F 'organization=square' \
    -F "apk_data=@$UNSIGNED_APK" \
    -F 'v2=true' \
    'https://apk-signer.global.square/sign-apk'

  if [ $? -ne 0 ]; then
    echo "Error signing '$UNSIGNED_APK' with apksigner"
  else
    echo "Signed '$UNSIGNED_APK' with apksigner"
  fi
}

function sign_aab() {
  UNSIGNED_AAB="$1"
  SIGNED_AAB="$2"

  echo "======================"
  echo "Signing AAB"
  echo "======================"

  curl \
    -sS \
    -L \
    -v \
    --fail \
    --connect-timeout 120 \
    --max-time 120 \
    --cacert "$SECRETS_DIR/service2service.ca.pem" \
    --cert-type 'pem' \
    --cert "$SECRETS_DIR/kochiku-worker-s2s.crt" \
    --key "$SECRETS_DIR/kochiku-worker-s2s.key" \
    -o "$SIGNED_AAB" \
    -X 'POST' \
    -H "X-Speleo-Trace-Id: $(uuidgen | sed 's/\-//g')" \
    -F 'organization=square' \
    -F "aab_data=@$UNSIGNED_AAB" \
    'https://apk-signer.global.square/sign-aab'

  if [[ $? -ne 0 ]]; then
    echo "Error signing '$UNSIGNED_AAB' with apksigner"
  else
    echo "Signed '$UNSIGNED_AAB' with apksigner"
  fi
}

# Executed by artifact-building shards on 'kochiku-worker' (EC2 workers)
function upload_artifact() {
  SIGNED_ARTIFACT_PATH="$1"
  VARIANT="$2"
  ARTIFACT_TYPE="$3"
  #TODO version?

  echo "======================"
  echo "Uploading artifact"
  echo "======================"

  # if [[ "$GIT_BRANCH" == "main" ]]; then
  VERSION="main"
  # else
  # VERSION=$(echo "$GIT_BRANCH" | sed -n 's/^release\///p')
  # fi

  echo "Setting version for upload artifact to '$VERSION'"

  curl \
    -sS \
    -L \
    -v \
    --fail \
    --connect-timeout 20 \
    --max-time 100 \
    --cacert "$SECRETS_DIR/service2service.ca.pem" \
    --cert-type 'pem' \
    --cert "$SECRETS_DIR/kochiku-worker-s2s.crt" \
    --key "$SECRETS_DIR/kochiku-worker-s2s.key" \
    -X 'POST' \
    -H "X-Speleo-Trace-Id: $(uuidgen | sed 's/\-//g')" \
    -F 'application=cash-app-pay-sdk-dev-app' \
    -F 'platform=android' \
    -F "version=$VERSION" \
    -F "sha=$GIT_COMMIT" \
    -F "variant=$VARIANT" \
    -F "hidden=false" \
    -F "artifact_data=@$SIGNED_ARTIFACT_PATH" \
    -F "file_size=$(wc -c <$SIGNED_ARTIFACT_PATH | tr -d ' ')" \
    'https://mobile-releases.global.square/api/upload-artifact'

  if [ $? -ne 0 ]; then
    echo "Error uploading '$SIGNED_ARTIFACT_PATH' to mobile-releases"
  else
    echo "Uploaded '$SIGNED_ARTIFACT_PATH' to mobile-releases"
  fi
}
