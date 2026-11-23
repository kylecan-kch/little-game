# 益智合集

这是一套 Android 小游戏的第一款：**2048**。界面默认简体中文。棋盘、背景、空格和每种数字都跟着画风一起变，不只是换个颜色。

当前画风：水墨画、水彩画、梵高星空、莫奈水粉、赛博朋克、浮世绘、像素复古、敦煌壁画。画面用 Compose 绘制，Android 13 及以上还会给星空和赛博朋克背景加上着色器；更低版本自动退回同样风格的画布绘制。

## 环境

- JDK 17 或更高（这里用 JDK 21 编译，字节码目标是 17）
- Android SDK，`compileSdk` / `targetSdk` 为 37，`minSdk` 为 24
- 设置 `ANDROID_HOME`，或在项目根目录的 `local.properties` 里写 `sdk.dir=/你的/sdk路径`

## 构建、测试、安装

```bash
./gradlew test
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

调试包在 `app/build/outputs/apk/debug/app-debug.apk`。

## 怎么玩

上下左右滑动，相同数字会叠在一起。一次滑动里，`2 2 2 2` 会变成 `4 4`，不会连成 `8`。每走一步有效的，空位上会出现一个新方块：九成是 2，一成是 4。叠出 2048 之后可以选择继续。没有空位、也没有相邻的相同数字时，这局结束。

当前分数和最高分会保存。最高分用 DataStore 记下来，当前棋盘和上一步也会保存，关掉应用再打开还能接着下。可以新开一局，也可以撤销一步。

## 目录

- `games/engine2048`：纯 Kotlin 规则，不依赖 Android。移动、合并、生成、胜利、结束和存档编码的单元测试都在这里。
- `games/game2048`：2048 的界面、动画、八种画风和 DataStore 存档。
- `app`：合集外壳。首页列出游戏，系统深色模式只影响这层外壳；进入 2048 之后由选定的画风决定整屏颜色。

## 再加一个游戏

1. 如果规则能脱离界面测试，在 `games/` 下加一个纯 Kotlin 模块。
2. 再加一个 Android 库模块放这个游戏的界面。
3. 在 `app` 的 `HomeScreen` / `AppRoot` 里登记入口。

## 再加一种画风

1. 新建一个 `ArtStyle`，在 `prepareBackdrop`、`prepareBoard`、`prepareEmpty`、`prepareTile` 里把图形先算好，再交给绘制函数。滑动时只移动方块，不要在每一帧里重新建路径。
2. 把这个对象加进 `ArtStyles.all`。选择页会自动多一张预览，选中的 id 会写进 DataStore。

画风都是程序绘制的，没有使用受版权保护的图片。
