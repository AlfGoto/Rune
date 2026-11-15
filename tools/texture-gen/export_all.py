"""Rebuild every Rune texture into the plugin's resource pack.

    pip install pillow
    MC_ASSETS=/path/to/extracted/26.x/assets/minecraft/textures python3 tools/texture-gen/export_all.py

MC_ASSETS is only needed for the menu backgrounds (they start from vanilla gui/container/generic_54.png).
Writes PNGs, item definitions and models under src/main/resources/pack/assets/rune/ and appends
any missing line to src/main/resources/pack/files.txt.
"""
import json, os, sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import pickaxes, axes, components, gui  # noqa: E402

ROOT = os.path.abspath(os.path.join(HERE, '..', '..', 'src', 'main', 'resources', 'pack'))
PACK = os.path.join(ROOT, 'assets', 'rune')
MODS = [(pickaxes, True), (axes, True), (components, False), (gui, False)]


def write_json(path, data):
    with open(path, 'w') as f:
        json.dump(data, f, indent=2)
        f.write('\n')


def main():
    for d in ('textures/item', 'items', 'models/item', 'textures/gui'):
        os.makedirs(os.path.join(PACK, d), exist_ok=True)
    lines = []
    for mod, tool in MODS:
        for key, item in mod.P.items():
            item['img'].save(os.path.join(PACK, 'textures', 'item', key + '.png'))
            write_json(os.path.join(PACK, 'items', key + '.json'),
                       {"model": {"type": "minecraft:model", "model": "rune:item/" + key}})
            write_json(os.path.join(PACK, 'models', 'item', key + '.json'),
                       {"parent": "minecraft:item/handheld" if tool else "minecraft:item/generated",
                        "textures": {"layer0": "rune:item/" + key}})
            lines += ['assets/rune/items/%s.json' % key, 'assets/rune/models/item/%s.json' % key,
                      'assets/rune/textures/item/%s.png' % key]
    if os.path.exists(gui.V):
        for name, draw in (('codex_main', gui.codex_main), ('rune_loadout', gui.rune_loadout)):
            draw().save(os.path.join(PACK, 'textures', 'gui', name + '.png'))
            lines.append('assets/rune/textures/gui/%s.png' % name)
    else:
        print('MC_ASSETS not set or generic_54.png missing: menu backgrounds skipped')
    listed = os.path.join(ROOT, 'files.txt')
    have = set(open(listed).read().split())
    missing = [l for l in lines if l not in have]
    if missing:
        with open(listed, 'a') as f:
            f.write('\n'.join(missing) + '\n')
    print('%d files written, %d new lines in files.txt' % (len(lines), len(missing)))


if __name__ == '__main__':
    main()
