[![release](https://github.com/cashapp/android-cash-paykit-sdk/actions/workflows/release.yaml/badge.svg)](https://github.com/cashapp/android-cash-paykit-sdk/actions/workflows/release.yaml) ![License](https://img.shields.io/github/license/cashapp/cash-pay-kit-sdk-android-sample-app?style=plastic) 

# About

Cash App Pay Android SDK is a wrapper library around our public APIs that allows merchants to easily
integrate payments with Cash on their native Android checkout flows. Similar SDK projects existing
for Web and iOS.

# Integrate the SDK

For detailed documentation on how to integrate this SDK please visit
our [Cash App Developers webpage](https://developers.cash.app/docs/api/technical-documentation/sdks/pay-kit/android-getting-started)
.

## Sample App

A Sample App that showcases our demo merchant can be found
in [here](https://github.com/cashapp/cash-pay-pay-sdk-android-sample-app).

## Sandbox App

For convenience, we recommend developers to leverage
our [Sandbox App](https://developers.cash.app/docs/api/technical-documentation/sandbox/sandbox-app)
to simulate the necessary interactions with Cash App during development stages.
The Sandbox App can be particularly helpful for those who do not possess a Cash App account or live

# Maven

The latest version of the SDK can be found here: https://search.maven.org/search?q=g:app.cash.paykit


# NOTES FOR KSTATEMACHINE

When I bring in the dependency, I got a kotlin stdlib error...

/Users/cpetzel/.gradle/caches/modules-2/files-2.1/org.jetbrains.kotlin/kotlin-stdlib-common/1.8.10/a61b182458550492c12aee66157d7b524a63a5ec/kotlin-stdlib-common-1.8.10.jar!/META-INF/kotlin-stdlib-common.kotlin_module: Module was compiled with an incompatible version of Kotlin. The binary version of its metadata is 1.8.0, expected version is 1.6.0.

it looks like we are using kotlin 1.6, but this lib is using 1.8...

So I brought over the entire source for now until we figure it out

License
=======

    Copyright 2023 Cash App

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.