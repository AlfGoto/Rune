"""Rebuild every Rune texture into the plugin's resource pack.

    pip install pillow
    (cd tools/texture-gen && npm install minecraft-assets)      # vanilla 26.1 textures for the v3 generator
    MC_ASSETS=/path/to/extracted/26.x/assets/minecraft/textures python3 tools/texture-gen/export_all.py

MC_ASSETS is only needed for the menu backgrounds (gui.py starts from vanilla gui/container/generic_54.png).
Items, armor icons and worn armor layers come from v3/ (tools.py, components.py, armor.py).
Writes PNGs, item definitions, models and equipment definitions under src/main/resources/pack/assets/rune/
and appends any missing line to src/main/resources/pack/files.txt.
"""
import json, os, sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
sys.path.insert(0, os.path.join(HERE, 'v3'))
import gui  # noqa: E402  (v2 kit/pix: menus)
import tools, components, armor  # noqa: E402  (v3)

ROOT = os.path.abspath(os.path.join(HERE, '..', '..', 'src', 'main', 'resources', 'pack'))
PACK = os.path.join(ROOT, 'assets', 'rune')


def write_json(path, data):
    with open(path, 'w') as f:
        json.dump(data, f, indent=2)
        f.write('\n')


def item_files(key, img, parent):
    img.save(os.path.join(PACK, 'textures', 'item', key + '.png'))
    write_json(os.path.join(PACK, 'items', key + '.json'),
               {"model": {"type": "minecraft:model", "model": "rune:item/" + key}})
    write_json(os.path.join(PACK, 'models', 'item', key + '.json'),
               {"parent": parent, "textures": {"layer0": "rune:item/" + key}})
    return ['assets/rune/items/%s.json' % key, 'assets/rune/models/item/%s.json' % key,
            'assets/rune/textures/item/%s.png' % key]


def main():
    for d in ('textures/item', 'items', 'models/item', 'textures/gui', 'equipment',
              'textures/entity/equipment/humanoid', 'textures/entity/equipment/humanoid_leggings'):
        os.makedirs(os.path.join(PACK, d), exist_ok=True)
    lines = []
    # tools (handheld)
    for kind, specs in (('pickaxe', tools.PICKAXES), ('axe', tools.AXES)):
        for spec in specs:
            lines += item_files(spec[0], tools.build(kind, spec).im, 'minecraft:item/handheld')
    # components (generated)
    for key, (disp, rows, k) in components.COMPONENTS.items():
        lines += item_files(key, components.Pix().grid(rows, k).im, 'minecraft:item/generated')
    # armor icons + one equipment asset per set
    for job in ('mining', 'woodcutting'):
        for rank in armor.RANKS:
            for i, slot in enumerate(armor.SLOTS):
                key = armor.slug(armor.NAMES[job][rank][i])
                lines += item_files(key, armor.build_icon(job, rank, slot).im, 'minecraft:item/generated')
            l1, l2 = armor.build_layers(job, rank)
            set_key = '%s_%s' % (job, rank)
            l1.save(os.path.join(PACK, 'textures', 'entity', 'equipment', 'humanoid', set_key + '.png'))
            l2.save(os.path.join(PACK, 'textures', 'entity', 'equipment', 'humanoid_leggings', set_key + '.png'))
            write_json(os.path.join(PACK, 'equipment', set_key + '.json'),
                       {"layers": {"humanoid": [{"texture": "rune:" + set_key}],
                                   "humanoid_leggings": [{"texture": "rune:" + set_key}]}})
            lines += ['assets/rune/equipment/%s.json' % set_key,
                      'assets/rune/textures/entity/equipment/humanoid/%s.png' % set_key,
                      'assets/rune/textures/entity/equipment/humanoid_leggings/%s.png' % set_key]
    # menu icons (v2 gui.py)
    for key, item in gui.P.items():
        lines += item_files(key, item['img'], 'minecraft:item/generated')
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
