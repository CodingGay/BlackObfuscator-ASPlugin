# DEX控制流混淆插件版 · BlackObfuscator-ASPlugin

**[English Version](README_EN.md)**

![](https://img.shields.io/badge/language-java-brightgreen.svg)

本项目为 [BlackObfuscator](https://github.com/CodingGay/BlackObfuscator) 的Android Studio插件版，支持打包自动化混淆。功能及介绍方面请查看 [BlackObfuscator](https://github.com/CodingGay/BlackObfuscator) 源项目

## 注意事项
- 首要注意：[BlackObfuscator](https://github.com/CodingGay/BlackObfuscator) 内的注意事项
- 若打包报错或者无效请提供 ```./gradlew tasks --all``` 信息

## 使用方式

### 准备

#### Step 1. 根目录Gradle文件加入
```gradle
repositories {
    ...
    // 加入仓库
    maven { url 'https://jitpack.io' }
}
dependencies {
    ...
    classpath "com.github.CodingGay:BlackObfuscator-ASPlugin:3.7"
}
```
#### Step 2. app模块加入plugin
```gradle
...
apply plugin: 'com.android.application'
// 加入
apply plugin: 'top.niunaijun.blackobfuscator'
```
或者你的是这样的
```gradle
plugins {
    id 'com.android.application'
    // 加入
    id 'top.niunaijun.blackobfuscator'
}
```
#### Step 3. 添加混淆配置
```gradle
android {
    ...

    defaultConfig {
       ...
    }
}

// 加入混淆配置
BlackObfuscator {
    // 是否启用
    enabled true
    // 混淆深度
    depth 2
    // 需要混淆的包或者类(匹配前面一段)
    obfClass = ["top.niunaijun", "com.abc"]
    // blackClass中的包或者类不会进行混淆(匹配前面一段)
    blackClass = ["top.niunaijun.black"]
}

dependencies {
    ...
}
```
#### Step 4. Clean一次项目，打包即可自动混淆


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
