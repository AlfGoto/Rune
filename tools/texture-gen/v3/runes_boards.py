"""Design-canvas artboards for the layered Rune textures (see runes.py)."""
import os, html, json, sys
from PIL import Image
from pix import Pix, OUT, sheet
import runes as R

RCOL = {'common': '#FFFFFF', 'uncommon': '#55FF55', 'rare': '#55FFFF', 'epic': '#FF55FF', 'legendary': '#FFAA00'}
TILES = {}  # name -> Pix


def add(name, p):
    TILES[name] = p
    return name


def build_tiles():
    for r in R.RARITIES:
        add(f'base_{r}', R.animated_base(r))
    for rid in R.RUNES:
        add(f'logo_{rid}', R.logo(rid))
        add(f'bare_{rid}', R.compose(rid))
    for rid, vs in R.VARIANTS.items():
        for v, mat in vs.items():
            add(f'sock_{rid}_{v}', R.socket(rid, mat))
            add(f'var_{rid}_{v}', R.compose(rid, 1, v))
    for n in range(1, R.MAX_NUMERAL + 1):
        add(f'num_{n}', R.numeral(n))
        add(f'nummax_{n}', R.numeral(n, True))
    for rid, (_, _, mr, *_ ) in R.RUNES.items():
        v = list(R.VARIANTS[rid])[0] if rid in R.VARIANTS else None
        for n in range(1, (mr or 10) + 1):
            add(f'rank_{rid}_{n}', R.compose(rid, n, v))


def css_rules(names, pos):
    out = []
    for k, cls in ((8, 'i'), (4, 'j')):
        for n, (x, y) in zip(names, pos):
            out.append(f'.{cls}-{n}{{background-position:-{x * k}px -{y * k}px}}')
    return '\n'.join(out)


def slot(name, size=144, cls='i', label=None):
    lab = html.escape(label or name)
    inner = size - 16
    return (f'<div style="width: {size}px; height: {size}px; flex: none; background: #8b8b8b; border-top: 4px solid #373737; '
            f'border-left: 4px solid #373737; border-right: 4px solid #ffffff; border-bottom: 4px solid #ffffff; '
            f'box-sizing: border-box; display: flex; align-items: center; justify-content: center">'
            f'<div class="{"ic" if cls == "i" else "ic4"} {cls}-{name}" role="img" aria-label="{lab}" style="width: {inner}px; height: {inner}px"></div></div>')


def caption(title, color='#f2f2f2', sub=None, mono=None):
    s = f'<div style="font-size: 15px; font-weight: 600; color: {color}; text-align: center; line-height: 1.2">{html.escape(title)}</div>'
    if sub:
        s += f'<div style="font-size: 12px; color: #b4b4b4; text-align: center">{html.escape(sub)}</div>'
    if mono:
        s += f'<div style="font-size: 11px; color: #9a9a9a; font-family: \'JetBrains Mono\', monospace; text-align: center">{html.escape(mono)}</div>'
    return s


def tile(name, title, color='#f2f2f2', sub=None, mono=None, size=144, cls='i'):
    return (f'<div style="width: {max(size, 152)}px; display: flex; flex-direction: column; align-items: center; gap: 8px">'
            f'{slot(name, size, cls, title)}{caption(title, color, sub, mono)}</div>')


def section(title, sub, body, wrap=True, gap=20):
    return (f'<section style="display: flex; flex-direction: column; gap: 16px">'
            f'<div style="display: flex; align-items: baseline; gap: 16px; flex-wrap: wrap"><h2 style="margin: 0; font-size: 24px; color: #f2f2f2">{html.escape(title)}</h2>'
            f'<span style="font-size: 14px; color: #b4b4b4">{sub}</span></div>'
            f'<div style="display: flex; {"flex-wrap: wrap;" if wrap else "flex-direction: column;"} gap: {gap}px">{body}</div></section>')


def board(title, intro, sections, css, url, w, h):
    leg = ''.join(f'<span style="color: {c}; font-size: 14px; font-weight: 600">{r.title()}</span>' for r, c in RCOL.items())
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
.ic{{background-image:url({url});background-size:{css['sw'] * 8}px {css['sh'] * 8}px;background-repeat:no-repeat;image-rendering:pixelated}}
.ic4{{background-image:url({url});background-size:{css['sw'] * 4}px {css['sh'] * 4}px;background-repeat:no-repeat;image-rendering:pixelated}}
{css['rules']}
</style>
</helmet>
<div style="width: {w}px; box-sizing: border-box; padding: 56px; background: #1d1d1f; font-family: Inter, sans-serif; color: #f2f2f2; display: flex; flex-direction: column; gap: 44px">
<header style="display: flex; justify-content: space-between; align-items: flex-end; gap: 24px">
<div style="display: flex; flex-direction: column; gap: 8px; max-width: 1300px"><h1 style="margin: 0; font-size: 44px; font-weight: 700">{html.escape(title)}</h1>
<p style="margin: 0; font-size: 16px; color: #b4b4b4; line-height: 1.5">{intro}</p></div>
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


def op(sym):
    return f'<div style="font-size: 40px; color: #8a8a8a; font-weight: 700; padding-top: 44px" aria-hidden="true">{sym}</div>'


def stack_row(rid, rank, variant, label):
    name, rarity, mr = R.RUNES[rid][:3]
    parts = [tile(f'base_{rarity}', f'base/{rarity}', RCOL[rarity], 'layer0')]
    parts.append(op('+'))
    parts.append(tile(f'logo_{rid}', f'logo/{rid}', sub='layer1'))
    layer = 2
    if variant:
        parts.append(op('+'))
        parts.append(tile(f'sock_{rid}_{variant}', f'socket/{rid}_{variant}', sub=f'layer{layer}'))
        layer += 1
    maxed = rank == mr
    parts.append(op('+'))
    parts.append(tile(f'{"nummax" if maxed else "num"}_{rank}', f'rank/{rank}{"_max" if maxed else ""}', sub=f'layer{layer}'))
    parts.append(op('='))
    parts.append(tile(f'rank_{rid}_{rank}' if not variant else f'res_{rid}_{variant}_{rank}', label, RCOL[rarity], f'{rarity.title()} · Rank {R.roman(rank)}'))
    return f'<div style="display: flex; align-items: flex-start; gap: 18px">{"".join(parts)}</div>'


def build(outdir, url=None):
    build_tiles()
    # composites for the layer examples with a variant
    add('res_specialist_diamond_2', R.compose('specialist', 2, 'diamond'))
    add('res_affinity_lush_caves_3', R.compose('affinity', 3, 'lush_caves'))
    names = list(TILES)
    im, pos = sheet([TILES[n] for n in names], cols=16)
    os.makedirs(os.path.join(outdir, 'sheets'), exist_ok=True)
    os.makedirs(os.path.join(outdir, 'project'), exist_ok=True)
    im.save(os.path.join(outdir, 'sheets', 'runes_sheet.png'))
    css = {'sw': im.width, 'sh': im.height, 'rules': css_rules(names, pos)}
    url = url or 'sheets/runes_sheet.png'
    boards = {}

    # -- Main: how it stacks
    intro = ('A Rune texture is never drawn whole. It is 3 or 4 layers of one <code>minecraft:item/generated</code> model: '
             'the <b>stone</b> (one per Rarity), the <b>logo</b> (one per Rune), the <b>socket</b> material (only for '
             'Specialist / Affinity / Sanctuary variants) and the <b>Rank</b> numeral (I → XX, gold at Max Rank). '
             'So ~25 logos + 5 stones + 40 numerals cover every Rune at every Rank. Shown ×8 on a vanilla slot.')
    rows = [stack_row('haste', 3, None, 'Haste III'),
            stack_row('haste', 5, None, 'Haste V (max)'),
            stack_row('specialist', 2, 'diamond', 'Specialist: Diamond II'),
            stack_row('affinity', 3, 'lush_caves', 'Affinity: Lush Caves III (max)'),
            stack_row('veinminer', 3, None, 'Veinminer III (max)')]
    model = html.escape(json.dumps({"parent": "minecraft:item/generated", "textures": {
        "layer0": "rune:item/rune/base/uncommon", "layer1": "rune:item/rune/logo/affinity",
        "layer2": "rune:item/rune/socket/affinity_lush_caves", "layer3": "rune:item/rune/rank/3_max"}}, indent=2))
    code = (f'<pre style="margin: 0; padding: 24px; background: #111113; border: 1px solid #3a3a3d; border-radius: 6px; '
            f'font-family: \'JetBrains Mono\', monospace; font-size: 15px; line-height: 1.5; color: #d8d8d8; width: 720px; box-sizing: border-box; white-space: pre-wrap">{model}</pre>')
    boards['Main'] = board('Rune · Runes, built from layers', intro,
                           [section('How a Rune stacks', 'base + logo (+ socket) + Rank = the item', ''.join(rows), wrap=False, gap=28),
                            section('The model behind it', 'generated by the export script, one per Rune × Rank (× variant); no new PNG', code)],
                           css, url, 1840, 1900)

    # -- Bases
    GIFS = json.loads(os.environ.get('RUNE_GIFS', '{}'))

    def gif_tile(key, title, sub_):
        return (f'<div style="width: 152px; display: flex; flex-direction: column; align-items: center; gap: 8px">'
                f'<div style="width: 144px; height: 144px; flex: none; border-top: 4px solid #373737; border-left: 4px solid #373737; border-right: 4px solid #ffffff; border-bottom: 4px solid #ffffff; box-sizing: border-box">'
                f'<img src="{GIFS.get(key, "")}" alt="{html.escape(title)}, animated" style="display: block; width: 136px; height: 136px; image-rendering: pixelated"></div>'
                f'{caption(title, RCOL["legendary"], sub_)}</div>')
    magic = ''.join([gif_tile('legendary_base', 'base/legendary', '12 frames · 2 ticks each'),
                     gif_tile('great_blast', 'Great Blast III', 'in game'),
                     gif_tile('veinminer', 'Veinminer III', 'in game'),
                     gif_tile('great_fell', 'Great Fell III', 'in game')])
    sub = {'common': 'stone', 'uncommon': 'stone + copper corners', 'rare': 'deepslate + gold corners + diamond setting',
           'epic': 'obsidian + crying obsidian rim + netherite corners', 'legendary': 'netherite + gold frame and finial, animated sparkles'}
    boards['Bases'] = board('Rune · Stones (layer0)', 'One stone per Rarity, following the tool ladder (Common plain → Legendary gold finial). '
                            'The logo area (8×8, centre) and the numeral strip (bottom) stay clean on every stone.',
                            [section('Stones', '5 files · <code>base/&lt;rarity&gt;.png</code>',
                                     ''.join(tile(f'base_{r}', r.title(), RCOL[r], sub[r], f'base/{r}.png') for r in R.RARITIES)),
                             section('Legendary magic', 'animated with <code>base/legendary.png.mcmeta</code>: gold and violet sparkles twinkle around the stone, nothing on the logo', magic)],
                            css, url, 1300, 900)

    # -- Logos
    def logo_sec(title, ids):
        t = []
        for rid in ids:
            name, rarity, mr = R.RUNES[rid][:3]
            cap = 'no cap' if mr is None else f'max {R.roman(mr)}'
            t.append(tile(f'bare_{rid}', name, RCOL[rarity], f'{rarity.title()} · {cap}', f'logo/{rid}.png'))
        return section(title, f'{len(ids)} logos', ''.join(t))
    boards['Logos'] = board('Rune · Logos (layer1)', 'One engraved symbol per Rune, drawn on an 8×8 grid in the stone\'s centre with a 1px shadow. '
                            'Each logo has its own colour so Runes read at a glance; the stone still tells the Rarity. '
                            'Specialist, Affinity and Sanctuary have an empty socket that the variant layer fills. Shown without a Rank.',
                            [logo_sec('Mining', R.MINING), logo_sec('Woodcutting', [r for r in R.WOODCUTTING if r not in ('specialist', 'affinity')])],
                            css, url, 1840, 1500)

    # -- Variants
    vs = []
    titles = {'specialist': 'Specialist: <ore / wood>', 'affinity': 'Affinity: <place>', 'sanctuary': 'Sanctuary: <biome>'}
    for rid, vmap in R.VARIANTS.items():
        rarity = R.RUNES[rid][1]
        t = ''.join(tile(f'var_{rid}_{v}', v.replace('_', ' ').title(), RCOL[rarity], f'socket: {m}', f'socket/{rid}_{v}.png')
                    for v, m in vmap.items())
        vs.append(section(titles[rid], f'{len(vmap)} variants · one logo, the socket colour changes', t))
    boards['Variants'] = board('Rune · Variants (layer2)', 'Variant Runes share one logo; a tiny socket layer paints the material in. '
                               'A new ore, wood or place is one line in <code>runes.py</code>. Poplar colours are still invented.',
                               vs, css, url, 1840, 2300)

    # -- Numerals
    nums = ''.join(tile(f'num_{n}', R.roman(n), sub=f'rank/{n}.png') for n in range(1, R.MAX_NUMERAL + 1))
    maxn = ''.join(tile(f'nummax_{n}', R.roman(n), '#FFAA00', sub=f'rank/{n}_max.png') for n in range(1, 11))
    boards['Numerals'] = board('Rune · Rank numerals (layer3)', 'A 3-pixel Roman font (I, V, X) with a dark edge, centred on the bottom strip. '
                               'I → XX fit in 16 px (XVIII is the widest); Ranks above XX show XX. At Max Rank the numeral turns gold. '
                               'Runes with Max Rank I (Silk Touch, Replant) show no numeral.',
                               [section('Ranks', 'I → XX', nums), section('Max Rank', 'gold copy, used when Rank = Max Rank', maxn)],
                               css, url, 1840, 1500)

    # -- Every rank
    rows = []
    for rid in R.MINING + [r for r in R.WOODCUTTING if r not in ('specialist', 'affinity')]:
        name, rarity, mr = R.RUNES[rid][:3]
        cells = ''.join(f'<div style="display: flex; flex-direction: column; align-items: center; gap: 4px">{slot(f"rank_{rid}_{n}", 80, "j", f"{name} {R.roman(n)}")}'
                        f'<span style="font-size: 12px; color: #b4b4b4; font-family: \'JetBrains Mono\', monospace">{R.roman(n)}</span></div>'
                        for n in range(1, (mr or 10) + 1))
        rows.append(f'<div style="display: flex; align-items: center; gap: 16px"><div style="width: 220px; flex: none; font-size: 15px; font-weight: 600; color: {RCOL[rarity]}">{html.escape(name)}'
                    f'<div style="font-size: 12px; font-weight: 400; color: #b4b4b4">{"no Max Rank (I → X shown)" if mr is None else "Max Rank " + R.roman(mr)}</div></div>{cells}</div>')
    boards['Every_Rank'] = board('Rune · Every Rune at every Rank', 'All layers combined, ×4. This is what a player sees in a slot.',
                                 [section('Ranks', '25 Runes', ''.join(rows), wrap=False, gap=14)], css, url, 1500, 2900)

    for k, v in boards.items():
        with open(os.path.join(outdir, 'project', k + '.dc.html'), 'w') as f:
            f.write(v)
    return list(boards)


if __name__ == '__main__':
    print(build(sys.argv[1], sys.argv[2] if len(sys.argv) > 2 else None))
