# DexFile Control Flow Flattening · BlackObfuscator-ASPlugin

![](https://img.shields.io/badge/language-java-brightgreen.svg)

This project is an Android Studio plugin version of [BlackObfuscator](https://github.com/CodingGay/BlackObfuscator), it supports obfuscating code automatically. More information about this project are in [BlackObfuscator](https://github.com/CodingGay/BlackObfuscator).

## Matters Need Attention

- Please check the information of [BlackObfuscator](https://github.com/CodingGay/BlackObfuscator).
- If you got errors in building, please provide the the ``` ./gradlew tasks --all ``` output for me.

## Usage

#### Step 1. Configure your build.gradle (in top level directory)
```gradle
repositories {
    ...
    maven { url 'https://jitpack.io' }
}
dependencies {
    ...
    classpath "com.github.CodingGay:BlackObfuscator-ASPlugin:3.7"
}
```
#### Step 2. Apply the plugin in your app module
```gradle
...
apply plugin: 'com.android.application'
// Add
apply plugin: 'top.niunaijun.blackobfuscator'
```
or you can do it like this
```gradle
plugins {
    id 'com.android.application'
    // Add
    id 'top.niunaijun.blackobfuscator'
}
```
#### Step 3. Add configuration in your build.gradle (Module: app)
```gradle
android {
    ...

    defaultConfig {
       ...
    }
}

// Configuration
BlackObfuscator {
    // Enabled state
    enabled true
    // Obfuscation depth
    depth 2
    // The classes which need to be obfuscated
    obfClass = ["top.niunaijun", "com.abc"]
    // It will not obfuscate the classes that in blackClass
    blackClass = ["top.niunaijun.black"]
}

dependencies {
    ...
}
```
#### Step 4. Clean your project, it will obfuscate code automatically while you are building project


### License

> ```
> Copyright 2021 Milk
>
> Licensed under the Apache License, Version 2.0 (the "License");
> you may not use this file except in compliance with the License.
> You may obtain a copy of the License at
>
>    http://www.apache.org/licenses/LICENSE-2.0
>
> Unless required by applicable law or agreed to in writing, software
> distributed under the License is distributed on an "AS IS" BASIS,
> WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
> See the License for the specific language governing permissions and
> limitations under the License.
> ```
