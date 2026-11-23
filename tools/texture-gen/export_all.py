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
import tools, components, armor, runes, caches, icons, menus  # noqa: E402  (v3)

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


def save_frames(frames, rel):
    """One PNG; several frames = a vertical strip + .mcmeta (animated texture)."""
    from pix import Pix
    path = os.path.join(PACK, 'textures', rel + '.png')
    os.makedirs(os.path.dirname(path), exist_ok=True)
    strip = Pix(16, 16 * len(frames))
    for i, f in enumerate(frames):
        strip.blit(f, 0, 16 * i)
    strip.save(path)
    out = ['assets/rune/textures/%s.png' % rel]
    if len(frames) > 1:
        with open(path + '.mcmeta', 'w') as f:
            f.write('{"animation": {"frametime": %d}}\n' % runes.FRAMETIME)
        out.append('assets/rune/textures/%s.png.mcmeta' % rel)
    return out


def rune_files():
    """Runes are layered (docs/art-direction.md, "Runes"): stone + logo (+ socket) + Rank numeral.
    Item definition rune:<id> picks the Rank model from custom_model_data floats[0] (the Rank)."""
    lines = []
    for r in runes.RARITIES:
        lines += save_frames(runes.legendary_frames() if r == 'legendary' else [runes.base(r)], 'item/rune/base/' + r)
    for rid in runes.RUNES:
        lines += save_frames([runes.logo(rid)], 'item/rune/logo/' + rid)
    for rid, vs in runes.VARIANTS.items():
        for v, mat in vs.items():
            lines += save_frames([runes.socket(rid, mat)], 'item/rune/socket/%s_%s' % (rid, v))
    for n in range(1, runes.MAX_NUMERAL + 1):
        lines += save_frames([runes.numeral(n)], 'item/rune/rank/%d' % n)
        lines += save_frames([runes.numeral(n, True)], 'item/rune/rank/%d_max' % n)

    def one(item_id, rid, variant):
        rarity, max_rank = runes.RUNES[rid][1], runes.RUNES[rid][2]
        layers = ['rune:item/rune/base/' + rarity, 'rune:item/rune/logo/' + rid]
        if variant:
            layers.append('rune:item/rune/socket/%s_%s' % (rid, variant))
        out = []

        def model(name, extra):
            tex = {'layer%d' % i: t for i, t in enumerate(layers + extra)}
            write_json(os.path.join(PACK, 'models', 'item', 'rune', name + '.json'),
                       {"parent": "minecraft:item/generated", "textures": tex})
            out.append('assets/rune/models/item/rune/%s.json' % name)
            return {"type": "minecraft:model", "model": "rune:item/rune/" + name}
        if max_rank == 1:
            definition = model(item_id, [])
        else:
            top = max_rank or runes.MAX_NUMERAL
            ranks = {n: model('%s_%d' % (item_id, n), ['rune:item/rune/rank/%d%s' % (n, '_max' if n == max_rank else '')])
                     for n in range(1, top + 1)}
            definition = {"type": "minecraft:range_dispatch", "property": "minecraft:custom_model_data", "index": 0,
                          "entries": [{"threshold": n, "model": m} for n, m in ranks.items() if n > 1],
                          "fallback": ranks[1]}
        write_json(os.path.join(PACK, 'items', item_id + '.json'), {"model": definition})
        out.append('assets/rune/items/%s.json' % item_id)
        return out

    os.makedirs(os.path.join(PACK, 'models', 'item', 'rune'), exist_ok=True)
    for rid in runes.RUNES:
        if rid in runes.VARIANTS:
            for v in runes.VARIANTS[rid]:
                lines += one('%s_%s' % (rid, v), rid, v)
        else:
            lines += one(rid, rid, None)
    # Rune Caches (legendary animated) and the Rune Codex
    for r in runes.RARITIES:
        key = 'rune_cache_' + r
        lines += save_frames(caches.cache_frames(r), 'item/' + key)
        write_json(os.path.join(PACK, 'items', key + '.json'), {"model": {"type": "minecraft:model", "model": "rune:item/" + key}})
        write_json(os.path.join(PACK, 'models', 'item', key + '.json'), {"parent": "minecraft:item/generated", "textures": {"layer0": "rune:item/" + key}})
        lines += ['assets/rune/items/%s.json' % key, 'assets/rune/models/item/%s.json' % key]
    lines += item_files('rune_codex', caches.codex().im, 'minecraft:item/generated')
    return lines


def icon_files():
    """Rarity icons: one bitmap row in the rune:glyphs font (\ue000 Common .. \ue004 Legendary)."""
    path = os.path.join(PACK, 'textures', 'font', 'rarity.png')
    icons.strip().save(path)
    font = {"providers": [
        {"type": "bitmap", "file": "rune:font/placeholder.png", "height": 8, "ascent": 7, "chars": ["\ue0ff"]},
        {"type": "bitmap", "file": "rune:font/rarity.png", "height": 8, "ascent": 7, "chars": [icons.chars()]},
    ]}
    write_json(os.path.join(PACK, 'font', 'glyphs.json'), font)
    return ['assets/rune/textures/font/rarity.png', 'assets/rune/font/glyphs.json']


def menu_files():
    """Menu backgrounds v3 + live images (font rune:menus), menu text fonts, the invisible and reroll items."""
    from PIL import Image
    import menufont
    out = []
    gui = os.path.join(PACK, 'textures', 'gui', 'menu')
    os.makedirs(gui, exist_ok=True)
    for name in menus.BACKGROUNDS:
        menus.BACKGROUND_DRAW[name]().save(os.path.join(gui, name + '.png'))
        out.append('assets/rune/textures/gui/menu/%s.png' % name)
    for name, im, _top, _ch in menus.images():
        im.save(os.path.join(gui, name + '.png'))
        out.append('assets/rune/textures/gui/menu/%s.png' % name)
    write_json(os.path.join(PACK, 'font', 'menus.json'), menus.font())
    out.append('assets/rune/font/menus.json')
    out += menufont.export(os.path.join(PACK, 'textures'), os.path.join(ROOT, '..', 'menu_font.json'))
    out += item_files('menu_blank', Image.new('RGBA', (16, 16), (0, 0, 0, 0)), 'minecraft:item/generated')
    out += item_files('menu_empty', menus.empty_item(), 'minecraft:item/generated')
    return out


def append_listed(lines):
    listed = os.path.join(ROOT, 'files.txt')
    have = set(open(listed).read().split())
    missing = [l for l in dict.fromkeys(lines) if l not in have]
    if missing:
        with open(listed, 'a') as f:
            f.write('\n'.join(missing) + '\n')
    return missing


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
    # Runes, Rune Caches, Rune Codex (v3 runes.py, caches.py)
    lines += rune_files()
    lines += icon_files()
    lines += menu_files()
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
