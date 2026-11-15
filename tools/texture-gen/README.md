# texture-gen

Python scripts that draw every Rune item texture (16×16) and menu background. Art rules: `docs/art-direction.md`.

```
pip install pillow
MC_ASSETS=/path/to/26.x/assets/minecraft/textures python3 tools/texture-gen/export_all.py
```

- `pix.py`: grid + palette → PNG.
- `kit.py`: vanilla 26.x pickaxe/axe templates and the Tinkers-style parts (knot, grip, cap, pommel, finial).
- `pickaxes.py`, `axes.py`, `components.py`: one `add(...)` per item. A colour change is a one-line edit.
- `gui.py`: chest-menu backgrounds (`codex_main`, `rune_loadout`) and the in-slot menu icons.
- `export_all.py`: writes PNGs, item definitions and models into `src/main/resources/pack/assets/rune/` and appends missing lines to `files.txt`.

`MC_ASSETS` (vanilla textures, not in this repo) is only needed for the menu backgrounds.
Not part of the Gradle build.
