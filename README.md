# Edge Window Patches

An experimental universal patch source for Android apps, compatible with Morphe.

Repository: <https://github.com/ymshin-dev/edge-window-patches>

## Patch

**Hide status bar and ignore display cutouts** changes every app-defined `Activity` window when it resumes:

- Android 9 and newer: allows the window to extend into display cutout areas.
- Hides the status bar independently of the app's or Samsung's immersive-mode setting. The navigation bar remains visible.
- Reapplies the window settings once after `onResume`, because some apps change them during that callback.

The patch is disabled by default. Enable it only for apps where the content should use the notch or punch-hole area.

## Add the source in Morphe

After the first GitHub release is published, open **Sources → + → Remote** in Morphe Manager and enter:

```text
github.com/ymshin-dev/edge-window-patches
```

The patch is optional and must be selected in Expert mode.

## Limits

This changes Android's window policy. An app can still add its own cutout or system-bar padding through its layout, inset listeners, or rendering engine, which a universal patch cannot reliably remove. Content may then remain inset or overlap important controls. Below Android 9, the patch can hide the status bar but there is no display-cutout window API.

## Build

Build the Morphe patch bundle with:

```sh
./gradlew buildAndroid
```

The `.mpp` file is written to `patches/build/libs/`. Morphe's patcher artifacts are served through GitHub Packages; builds need a token with `read:packages` access configured as `gpr.user` and `gpr.key` in your Gradle user properties.

For GitHub Actions releases, add a `GPR_KEY` repository secret with a GitHub token that can read packages. `GPR_USER` is optional and defaults to the workflow actor. Keep package tokens out of committed files.

The patch changes Android window policy but has not been validated against a target APK or Galaxy device. Apps can retain their own inset padding, and content may overlap controls around the cutout.

## Patch catalog

<!-- PATCHES_START EXPANDED -->
> **[v1.0.1](https://github.com/ymshin-dev/edge-window-patches/releases/tag/v1.0.1)**&nbsp;&nbsp;•&nbsp;&nbsp;`main`&nbsp;&nbsp;•&nbsp;&nbsp;1 patches total
<details open>
<summary>🌐 Universal&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Hide status bar and ignore display cutouts](#hide-status-bar-and-ignore-display-cutouts) | Hides the status bar and extends app windows into display cutouts. |  |

</details>

<!-- PATCHES_END -->
