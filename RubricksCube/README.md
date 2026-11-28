This is a Kotlin Multiplatform project targeting Android, iOS.

* [/iosApp](./iosApp/iosApp) contains an iOS application. Even if you’re sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

* [/shared](./shared/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./shared/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./shared/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./shared/src/jvmMain/kotlin)
    folder is the appropriate location.

### Shared Material 3 UI

The shared `App` applies `QubeTheme` to Android and iOS. In a screen, use the
components from `metropolia.myapplication.ui` for labeled actions and headings.

```kotlin
QubeScreenTitle("Camera")
QubeButton("Take Photo", onCapture, enabled = canCapture)
QubeButton("Back", onBack, style = QubeButtonStyle.Text)
```

Use `Filled` for a primary action, `Tonal` or `Outlined` for supporting actions,
and `Text` for low-emphasis actions. Compose Material 3 cards, dialogs and text
fields inherit the shared theme. Use `MaterialTheme.colorScheme`, `typography`
and `shapes` for custom elements, and wrap standalone previews in `QubeTheme`.
Cube stickers use their puzzle colors.

### Running the apps

Use the run configurations provided by the run widget in your IDE's toolbar. You can also use these commands and options:

- Android app: `./gradlew :androidApp:assembleDebug`
- iOS app: open the [/iosApp](./iosApp) directory in Xcode and run it from there.

### Running tests

Use the run button in your IDE's editor gutter, or run tests using Gradle tasks:

- Android tests: `./gradlew :shared:testAndroidHostTest`
- iOS tests: `./gradlew :shared:iosSimulatorArm64Test`

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…
### Solve timer

On the Timer screen, hold the timer pad for at least 500 ms until it turns green,
then release to start. Touch it again to stop. A new scramble is generated after
each solve. Completed times and their scrambles are saved locally; the footer
shows the latest five times and rolling ao5, ao12, and ao100. Each average trims
5% from each end, rounded up (one result per end for ao5/ao12, five for ao100).
An unfinished solve is abandoned when the app leaves the foreground.

To generate a 3×3 scramble elsewhere, call the shared suspend API from a coroutine:

```kotlin
import metropolia.myapplication.timer.generate3x3Scramble

val scramble = generate3x3Scramble()
val startingCube = Cube.solved().applyMoves(scramble)
```

Android uses TNoodle-lib's random-state generator and WCA distance filtering,
with generation on a background dispatcher. The first Gradle sync must download
`org.worldcubeassociation.tnoodle:lib-scrambles:0.20.0` from Maven Central.
iOS bundles the MIT-licensed min2phase.js solver and runs it offline in Apple's
JavaScriptCore on a background dispatcher. It samples random legal states,
rejects solved/one-move states, and generates an inverse solution. Its solver
is bundled with Compose resources, so iPhone does not need TNoodle or a network
connection. See `files/min2phase-LICENSE.txt` for the upstream attribution.

Timer transitions, statistics, formatting, and history serialization are tested
in `SolveTimerTest`. `ScrambleGeneratorTest` checks Android scrambles against the
app's notation parser and TNoodle's minimum-distance filter. Run both with:

```sh
./gradlew :shared:testAndroidHostTest
```

The bundled iOS scramble scripts can also be checked without Gradle:

```sh
node scripts/test-ios-scrambler.cjs
swift scripts/test-ios-scrambler.swift
```

The Swift check runs the scripts in Apple's JavaScriptCore; both checks generate
100 scrambles and verify that the resulting states can be solved back to solved.
