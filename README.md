# Edge Window Patches

An experimental universal patch source for Android apps, compatible with Morphe.

Repository: <https://github.com/ymshin-dev/edge-window-patches>

## Patch

**Force content into display cutouts** changes every app-defined `Activity` window when it resumes:

- Android 9–10: requests `LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES` and lays out behind the status bar.
- Android 11 and newer: requests `LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS` and disables decor fitting so the app's content can reach the screen edges.
- Reapplies the setting once after `onResume`, because some apps change window attributes during that callback.

The patch is disabled by default. Enable it only for apps where the content should use the notch or punch-hole area.

## Add the source in Morphe

After the first GitHub release is published, open **Sources → + → Remote** in Morphe Manager and enter:

```text
github.com/ymshin-dev/edge-window-patches
```

The patch is optional and must be selected in Expert mode.

## Limits

This changes Android's window policy. An app can still add its own cutout or system-bar padding through its layout, inset listeners, or rendering engine, which a universal patch cannot reliably remove. Content may then remain inset or overlap important controls. Devices below Android 9 do not have the display-cutout window API.

## Build

Build the Morphe patch bundle with:

```sh
./gradlew buildAndroid
```

The `.mpp` file is written to `patches/build/libs/`. Morphe's patcher artifacts are served through GitHub Packages; builds need a token with `read:packages` access configured as `gpr.user` and `gpr.key` in your Gradle user properties.

For GitHub Actions releases, add a `GPR_KEY` repository secret with a GitHub token that can read packages. `GPR_USER` is optional and defaults to the workflow actor. Keep package tokens out of committed files.

The patch changes Android window policy but has not been validated against a target APK or device. Apps can retain their own inset padding, and content may overlap controls around the cutout.
