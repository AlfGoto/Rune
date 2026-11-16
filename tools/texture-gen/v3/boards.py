"""Sprite sheets + Design-canvas artboards (.dc.html) for the contact sheets."""
import os, html, json
from PIL import Image
from pix import Pix, OUT, item, sheet
import tools, components, armor

RCOL = {'common': '#FFFFFF', 'uncommon': '#55FF55', 'rare': '#55FFFF', 'epic': '#FF55FF', 'legendary': '#FFAA00',
        'relic': '#FFAA00', 'fused': '#FFAA00', 'vanilla': '#d0d0d0', 'component': '#d0d0d0'}

PICK_GROUPS = [
    ('Vanilla reference', '26.1 item art', [('vanilla_iron_pickaxe', 'Iron Pickaxe', 'vanilla'), ('vanilla_iron_axe', 'Iron Axe', 'vanilla')]),
    ('Sturdy', 'Flint → Ironbound → Obsidian → Briseroc', ['flint_pickaxe', 'ironbound_pickaxe', 'obsidian_pickaxe', 'briseroc']),
    ('Speed', 'Coal → Copper → Redstone → Tunneler', ['coal_pickaxe', 'copper_pickaxe', 'redstone_pickaxe', 'tunneler']),
    ('Copper oxidation', '4 stages, waxed copies carry a honeycomb gloss', ['copper_pickaxe', 'waxed_copper_pickaxe', 'exposed_copper_pickaxe', 'waxed_exposed_copper_pickaxe', 'weathered_copper_pickaxe', 'waxed_weathered_copper_pickaxe', 'oxidized_copper_pickaxe', 'waxed_oxidized_copper_pickaxe']),
    ('Detection', 'Bone → Echo', ['bone_pickaxe', 'echo_pickaxe']),
    ('Loot', 'Magnetite → Geode → Ender', ['magnetite_pickaxe', 'geode_pickaxe', 'ender_pickaxe']),
    ('Biomes', 'Lichen + Prismarine + Emerald + Fiery + Echo → Worldpick', ['lichen_pickaxe', 'prismarine_pickaxe', 'emerald_pickaxe', 'fiery_pickaxe', 'echo_pickaxe', 'worldpick']),
    ('Relics', 'Warden Pickaxe + Starbreaker → Worldheart', ['warden_pickaxe', 'starbreaker', 'worldheart']),
]
AXE_GROUPS = [
    ('Sturdy', "Flint Hatchet → Woodsman's → Heartwood → Elder Axe", ['flint_hatchet', 'woodsmans_axe', 'heartwood_axe', 'elder_axe']),
    ('Speed', 'Stone Hatchet → Copper → Breeze → Tempest', ['stone_hatchet', 'copper_axe', 'breeze_axe', 'tempest_axe']),
    ('Copper oxidation', '4 stages, then waxed', ['copper_axe', 'waxed_copper_axe', 'exposed_copper_axe', 'waxed_exposed_copper_axe', 'weathered_copper_axe', 'waxed_weathered_copper_axe', 'oxidized_copper_axe', 'waxed_oxidized_copper_axe']),
    ('Harvest', 'Orchard → Golden Bough → Verdant', ['orchard_axe', 'golden_bough', 'verdant_axe']),
    ('Essences', 'Spruce + Jungle + Cherry + Crimson + Chorus → Yggdrasil', ['spruce_axe', 'jungle_axe', 'cherry_axe', 'crimson_axe', 'chorus_axe', 'yggdrasil']),
    ('Standalone', 'Birch, Poplar, Warped, Acacia, Mangrove, Pale Oak', ['birch_axe', 'poplar_axe', 'warped_axe', 'acacia_axe', 'mangrove_axe', 'pale_oak_axe']),
    ('Relics', "Timberfall + Evoker's Hatchet → Worldfeller · Creaking Axe + Motherroot → Grovewarden", ['timberfall', 'evokers_hatchet', 'worldfeller', 'creaking_axe', 'motherroot', 'grovewarden']),
]
LADDER = ("Fanciness ladder: <b>Common</b> vanilla shape + material + string/stone knot · <b>Uncommon</b> + Reinforced Handle "
          "(leather grip, iron cap), metal knot · <b>Rare</b> + gold Gem Setting knot with a gem, sharper tips, themed handle "
          "· <b>Epic</b> + heavier head, second material on the back, dark handle, netherite pommel · <b>Legendary</b> + gold "
          "finial, filigree, gem pommel (double-bit axes) · <b>Relic</b> unique silhouette. Shown at ×8, nearest-neighbour, on a vanilla slot.")


def tile(name, disp, rarity, file):
    return f'''<div style="width: 152px; display: flex; flex-direction: column; align-items: center; gap: 8px">
<div style="width: 144px; height: 144px; background: #8b8b8b; border-top: 4px solid #373737; border-left: 4px solid #373737; border-right: 4px solid #ffffff; border-bottom: 4px solid #ffffff; box-sizing: border-box; display: flex; align-items: center; justify-content: center"><div class="ic i-{name}" role="img" aria-label="{html.escape(disp)}"></div></div>
<div style="font-size: 15px; font-weight: 600; color: {RCOL[rarity]}; text-align: center; line-height: 1.2">{html.escape(disp)}</div>
<div style="font-size: 12px; color: #a8a8a8; font-family: 'JetBrains Mono', monospace">{rarity.title() if rarity != 'fused' else 'Fused Relic'}</div>
<div style="font-size: 11px; color: #8a8a8a; font-family: 'JetBrains Mono', monospace">{file}</div>
</div>'''


def section(title, sub, tiles):
    return f'''<section style="display: flex; flex-direction: column; gap: 16px">
<div style="display: flex; align-items: baseline; gap: 16px; flex-wrap: wrap"><h2 style="margin: 0; font-size: 24px; color: #f2f2f2">{html.escape(title)}</h2><span style="font-size: 14px; color: #b0b0b0">{html.escape(sub)}</span></div>
<div style="display: flex; flex-wrap: wrap; gap: 20px">{''.join(tiles)}</div>
</section>'''


def board(title, intro, sections, css, sheet_url, w, h, legend=True):
    leg = ''.join(f'<span style="color: {c}; font-size: 14px; font-weight: 600">{r}</span>' for r, c in
                  [('Common', '#FFFFFF'), ('Uncommon', '#55FF55'), ('Rare', '#55FFFF'), ('Epic', '#FF55FF'), ('Legendary', '#FFAA00')]) if legend else ''
    return f'''<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<title>{html.escape(title)}</title>
<script src="./support.js"></script>
</head>
<body>
<x-dc>
<helmet>
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&amp;family=JetBrains+Mono:wght@400&amp;display=swap" rel="stylesheet">
<style>
body{{margin:0}}
a{{color:#55FFFF}}a:hover{{color:#ffffff}}
.ic{{width:128px;height:128px;background-image:url({sheet_url});background-size:{css['sw']}px {css['sh']}px;background-repeat:no-repeat;image-rendering:pixelated}}
{css['rules']}
</style>
</helmet>
<div style="width: {w}px; box-sizing: border-box; padding: 56px; background: #1d1d1f; font-family: Inter, sans-serif; color: #f2f2f2; display: flex; flex-direction: column; gap: 44px">
<header style="display: flex; justify-content: space-between; align-items: flex-end; gap: 24px">
<div style="display: flex; flex-direction: column; gap: 8px; max-width: 1300px"><h1 style="margin: 0; font-size: 44px; font-weight: 700">{html.escape(title)}</h1>
<p style="margin: 0; font-size: 16px; color: #b0b0b0; line-height: 1.5">{intro}</p></div>
<div style="display: flex; gap: 20px">{leg}</div>
</header>
{''.join(sections)}
</div>
</x-dc>
<script type="text/x-dc" data-dc-script data-props='{{"$preview":{{"width":{w},"height":{h}}}}}'>
class Component extends DCLogic {{
renderVals() {{
return {{}};
}}
}}
</script>
</body>
</html>'''


def make_sheet(names, path):
    pixes = [Pix.load(os.path.join(OUT, 'items', n + '.png')) for n in names]
    im, pos = sheet(pixes, cols=16)
    im.save(path)
    rules = '\n'.join(f'.i-{n}{{background-position:-{x * 8}px -{y * 8}px}}' for n, (x, y) in zip(names, pos))
    return {'sw': im.width * 8, 'sh': im.height * 8, 'rules': rules}


def build_all(outdir, urls=None):
    """urls: {sheet_name: blob_url}. First pass (urls=None) just writes the sheets."""
    os.makedirs(os.path.join(outdir, 'sheets'), exist_ok=True)
    os.makedirs(os.path.join(outdir, 'project'), exist_ok=True)
    # vanilla refs into items/
    Pix(img=item('iron_pickaxe')).save(os.path.join(OUT, 'items', 'vanilla_iron_pickaxe.png'))
    Pix(img=item('iron_axe')).save(os.path.join(OUT, 'items', 'vanilla_iron_axe.png'))
    Pix(img=item('iron_chestplate')).save(os.path.join(OUT, 'items', 'vanilla_iron_chestplate.png'))
    pick_spec = {s[0]: s for s in tools.PICKAXES}
    axe_spec = {s[0]: s for s in tools.AXES}
    boards = {}

    def tool_board(fname, title, groups, specs, extra_vanilla):
        names = extra_vanilla + [s[0] for s in specs]
        css = make_sheet(names, os.path.join(outdir, 'sheets', fname + '.png'))
        secs = []
        for g, sub, items in groups:
            ts = []
            for it in items:
                if isinstance(it, tuple):
                    ts.append(tile(it[0], it[1], it[2], it[0] + '.png'))
                else:
                    sp = specs_by[it]
                    r = sp[2]
                    if r == 'relic' and it in ('worldheart', 'worldfeller', 'grovewarden'):
                        r = 'fused'
                    ts.append(tile(it, sp[1], r, it + '.png'))
            secs.append(section(g, sub, ts))
        url = (urls or {}).get(fname, f'sheets/{fname}.png')
        boards[fname] = board(title, LADDER, secs, css, url, 1840, 3000)

    specs_by = pick_spec
    tool_board('Pickaxes', 'Rune · Pickaxes v3', PICK_GROUPS, tools.PICKAXES, ['vanilla_iron_pickaxe', 'vanilla_iron_axe'])
    specs_by = axe_spec
    tool_board('Axes', 'Rune · Axes v3', AXE_GROUPS, tools.AXES, [])

    # components
    names = list(components.COMPONENTS)
    css = make_sheet(names, os.path.join(outdir, 'sheets', 'Components.png'))
    ts = [tile(n, components.COMPONENTS[n][0], 'component', n + '.png') for n in names]
    url = (urls or {}).get('Components', 'sheets/Components.png')
    boards['Components'] = board('Rune · Components', 'Shared crafting intermediates, <code>minecraft:item/generated</code>. Reinforced Handle (leather + iron cap), Reinforced Lining (leather, wool, rivets), Gem Setting (gold), Echo Lens (sculk glass, amethyst rim), Deep Core (deepslate + amethyst + diamond), Abyssal Plate (obsidian, crying obsidian, netherite rivets), Legendary Core (nether star + diamond).', [section('Components', '7 items', ts)], css, url, 1500, 700, legend=False)

    # armor
    for job, title in (('mining', 'Rune · Mining armor'), ('woodcutting', 'Rune · Woodcutting armor')):
        names = ['vanilla_iron_chestplate'] + [armor.slug(n) for r in armor.RANKS for n in armor.NAMES[job][r]]
        fname = 'Armor_' + job.title()
        css = make_sheet(names, os.path.join(outdir, 'sheets', fname + '.png'))
        secs = [section('Vanilla reference', '26.1 item art', [tile('vanilla_iron_chestplate', 'Iron Chestplate', 'vanilla', 'vanilla_iron_chestplate.png')])]
        mats = armor.MINING if job == 'mining' else armor.WOODCUTTING
        for r in armor.RANKS:
            b, t, a = mats[r]
            sub = f'base {b}' + (f', trim {t}' if t else '') + f', accent {a}'
            secs.append(section(r.title(), sub, [tile(armor.slug(n), n, r, armor.slug(n) + '.png') for n in armor.NAMES[job][r]]))
        url = (urls or {}).get(fname, f'sheets/{fname}.png')
        intro = ('Icons on the vanilla 26.1 armor templates, recoloured per Rarity (Common leather · Uncommon chainmail · Rare iron + gold trim · '
                 'Epic obsidian/dark oak + Abyssal accents · Legendary netherite/gold crown). ' +
                 ('Mining motif: headlamp on the helmet (candle → lantern → gem → sea lantern → sculk), gem on the chest, knee studs.'
                  if job == 'mining' else 'Woodcutting motif: leaf sprig on the helmet, bark line on the chest, Stag\'s Hooves with dark hoof toes.') +
                 ' Worn layers (64×32) are in <code>equipment/humanoid[_leggings]/&lt;job&gt;_&lt;rarity&gt;.png</code>, one per set.')
        boards[fname] = board(title, intro, secs, css, url, 1500, 2200)

    for k, v in boards.items():
        with open(os.path.join(outdir, 'project', k + '.dc.html'), 'w') as f:
            f.write(v)
    return boards


if __name__ == '__main__':
    import sys
    urls = json.loads(sys.argv[1]) if len(sys.argv) > 1 else None
    build_all(OUT, urls)
