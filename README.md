# Edge Window Patches

An experimental universal patch source for Android apps, compatible with Morphe.

Repository: <https://github.com/ymshin-dev/edge-window-patches>

## Patch

**Hide status bar and ignore display cutouts** changes every app-defined `Activity` window:

- Android 9 and newer: allows the window to extend into display cutout areas.
- Hides the status bar directly, without enabling Android's sticky immersive mode. The navigation bar remains visible.
- Android 11 and newer: removes status-bar and display-cutout insets before dispatching them from the Activity content root to app views.
- Reapplies the settings after resume, window-focus, and configuration changes, and when drawing detects that the app has shown the status bar again.

The patch is disabled by default. Enable it only for apps where the content should use the notch or punch-hole area.

## Add the source in Morphe

After the first GitHub release is published, open **Sources → + → Remote** in Morphe Manager and enter:

```text
github.com/ymshin-dev/edge-window-patches
```

The patch is optional and must be selected in Expert mode.

## Limits

The inset filter reaches view layouts that use the normal window-inset dispatch path. An app can still add padding itself or read root window insets directly, which may leave the player below the punch-hole. Removing top insets can also move interactive controls close to the camera. Below Android 11, the patch can allow the window into the cutout but does not filter inset dispatch; below Android 9, it can hide the status bar but there is no display-cutout window API.

## Build

Build the Morphe patch bundle with:

```sh
./gradlew buildAndroid
```

The `.mpp` file is written to `patches/build/libs/`. Morphe's patcher artifacts are served through GitHub Packages; builds need a token with `read:packages` access configured as `gpr.user` and `gpr.key` in your Gradle user properties.

For GitHub Actions releases, add a `GPR_KEY` repository secret with a GitHub token that can read packages. `GPR_USER` is optional and defaults to the workflow actor. Keep package tokens out of committed files.

The patch changes Android window policy and inset dispatch but has not been validated against a target APK or Galaxy device.

## Patch catalog

<!-- PATCHES_START EXPANDED -->
> **[v1.0.2](https://github.com/ymshin-dev/edge-window-patches/releases/tag/v1.0.2)**&nbsp;&nbsp;•&nbsp;&nbsp;`main`&nbsp;&nbsp;•&nbsp;&nbsp;1 patches total
<details open>
<summary>🌐 Universal&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Hide status bar and ignore display cutouts](#hide-status-bar-and-ignore-display-cutouts) | Hides the status bar and extends app windows into display cutouts. |  |

</details>

<!-- PATCHES_END -->
