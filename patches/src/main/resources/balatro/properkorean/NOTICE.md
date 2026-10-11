# ProperKorean for Balatro Android

Translation, grammar fixes, UI edits and merged font by **DVRP**.

- Website: https://dvrp-balatro-mods.pages.dev/properkorean
- Source: https://github.com/dvrp0/ProperKorean-Balatro
- Imported release: **v0.7.1**
- ZIP: https://github.com/dvrp0/ProperKorean-Balatro/releases/download/v0.7.1/ProperKorean.zip
- ZIP SHA-256: `ddfe0eeb8dddf974641cd6507a7d50aca431bfeb044c1682a557502fdcad5cc7`

The translation and merged font are preserved from that release. The PC Lovely
patterns are applied directly to Android Lua assets. The Android startup helper
merges translation dictionaries with the app's original language file, replaces
description lists, and restores challenge wording when switching languages.
The Steamodded achievement workaround and PC font/file loading are omitted.
No game APK, complete game source, Lovely binary or Steamodded is included.

Upstream does not declare a license for its Lua translation/mod files. They
remain attributed to DVRP; this repository's GPL notice does not relicense them.

The merged font includes Noto Sans KR (Google/Adobe, SIL Open Font License 1.1;
see OFL.txt) and m6x11 by Daniel Linssen, used by the upstream mod.
Font creator: https://managore.itch.io/m6x11
Noto source: https://github.com/notofonts/noto-cjk

Android adaptation and Morphe patch implementation: ymshin-dev, 2026.
