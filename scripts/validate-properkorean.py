#!/usr/bin/env python3
"""Validate a real Morphe output APK without committing proprietary fixtures.

Usage: python3 scripts/validate-properkorean.py ORIGINAL.apk PATCHED.apk
Requires LuaJIT on PATH. Does not install APKs or claim device/rendering coverage.
"""

from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import zipfile

ROOT = Path(__file__).resolve().parents[1]
RESOURCES = ROOT / "patches/src/main/resources/balatro/properkorean"
EDITED_LUA = {
    "assets/game.lua", "assets/cardarea.lua", "assets/blind.lua", "assets/card.lua",
    "assets/tag.lua", "assets/functions/UI_definitions.lua",
    "assets/functions/button_callbacks.lua", "assets/functions/misc_functions.lua",
    "assets/functions/state_events.lua",
}
NEW_ASSETS = {"assets/properkorean/" + name for name in ("ko.lua", "NOTICE.md", "OFL.txt")}
FONT = "assets/resources/fonts/NotoSansKR-Bold.ttf"


def main():
    luajit = shutil.which("luajit")
    if not luajit:
        raise SystemExit("LuaJIT is required")
    with zipfile.ZipFile(sys.argv[1]) as original, zipfile.ZipFile(sys.argv[2]) as patched:
        orig_names, new_names = set(original.namelist()), set(patched.namelist())
        # Signing metadata may be removed/replaced; every other byte outside the
        # translation changes (including existing Morphe DEX/native patches) must survive.
        keep = lambda name: not name.startswith("META-INF/") and not name.endswith("/")
        assert {n for n in orig_names - new_names if keep(n)} == set(), "APK entries lost"
        assert {n for n in new_names - orig_names if keep(n)} == NEW_ASSETS, "unexpected new entries"
        changed = {n for n in orig_names & new_names if keep(n) and original.read(n) != patched.read(n)}
        assert changed == EDITED_LUA | {FONT}, f"unexpected changes: {changed}"
        for name in ("ko.lua", "NOTICE.md", "OFL.txt"):
            assert patched.read("assets/properkorean/" + name) == (RESOURCES / name).read_bytes()
        assert patched.read(FONT) == (RESOURCES / "NotoSansKR-Bold.ttf").read_bytes()
        with tempfile.TemporaryDirectory(prefix="properkorean-validation-") as temp:
            directory = Path(temp)
            lua_files = [n for n in new_names if n.startswith("assets/") and n.endswith(".lua")]
            for name in sorted(new_names):
                if name.startswith("assets/") and (name.endswith(".lua") or name.endswith(".ttf")):
                    target = directory / name
                    target.parent.mkdir(parents=True, exist_ok=True)
                    target.write_bytes(patched.read(name))
            # LuaJIT bytecode compilation does not execute any app code.
            for name in sorted(lua_files):
                subprocess.run([luajit, "-b", str(directory / name), str(directory / "syntax-check.luac")], check=True)
            subprocess.run([luajit, str(ROOT / "scripts/validate-properkorean.lua"), str(directory / "assets")], check=True)
    print(f"PASS: {len(lua_files)} Lua files compile; exactly 9 Lua files and the Korean font changed; 3 notice/translation assets added; original DEX, manifest and other assets preserved")


if __name__ == "__main__":
    main()
