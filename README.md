This is a Kotlin Multiplatform project targeting Android, iOS.

* `/composeApp` is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - `commonMain` is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    `iosMain` would be the right folder for such calls.

* `/iosApp` contains iOS applications. Even if you’re sharing your UI with Compose Multiplatform, 
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

* Aether业内首次创新性基于AST + Runtime 构建KMP+CMP的动态化方案，实现逻辑页面动态下发，
* 全流程覆盖开发至运维。提升发版效率与热修复能力，有效缓解KMP缺失的动态化，可大范围的推动Android KMP的生态发展
* 但因为个人精力有限，还有很多工程化的能力需要建设，期待社区一起未来后续将强化复杂语法支持与生态建设，降低开发成本、优化体验并扩大业务覆盖；推进大前端融合，实现跨终端一致性体验。
* 目前实现最小的实验原型，后续依赖于社区一起建设


