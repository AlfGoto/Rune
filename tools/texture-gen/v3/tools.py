"""Pickaxes and axes: declarative list -> 16x16 PNGs."""
import os
from pix import Pix, OUT, item, darken
from palettes import R
from kit import base, apply_rarity, honeycomb, recolor_cells, cells_handle, cells_knot, cells_pommel, finial, double_bit, heavy_head

COPPER = [('copper', ''), ('copper_exposed', 'exposed_'), ('copper_weathered', 'weathered_'), ('copper_oxidized', 'oxidized_')]

# (file, display, rarity, head, options)
PICKAXES = [
    ('flint_pickaxe', 'Flint Pickaxe', 'common', 'flint', {'knot': 'string'}),
    ('coal_pickaxe', 'Coal Pickaxe', 'common', 'coal', {'knot': 'cobble'}),
    ('ironbound_pickaxe', 'Ironbound Pickaxe', 'uncommon', 'iron', {}),
    ('lichen_pickaxe', 'Lichen Pickaxe', 'uncommon', 'moss', {'post': 'lichen'}),
    ('bone_pickaxe', 'Bone Pickaxe', 'uncommon', 'bone', {}),
    ('magnetite_pickaxe', 'Magnetite Pickaxe', 'uncommon', 'raw_iron', {'post': 'magnet'}),
    ('fiery_pickaxe', 'Fiery Pickaxe', 'rare', 'magma', {'handle': 'blaze', 'gem': 'flame'}),
    ('redstone_pickaxe', 'Redstone Pickaxe', 'rare', 'redstone', {'gem': 'redstone'}),
    ('geode_pickaxe', 'Geode Pickaxe', 'rare', 'amethyst', {'gem': 'amethyst'}),
    ('emerald_pickaxe', 'Emerald Pickaxe', 'rare', 'emerald', {'gem': 'emerald'}),
    ('prismarine_pickaxe', 'Prismarine Pickaxe', 'rare', 'prismarine', {'gem': 'sea_lantern'}),
    ('echo_pickaxe', 'Echo Pickaxe', 'rare', 'sculk', {'gem': 'soul', 'handle': 'deepslate', 'post': 'echo'}),
    ('obsidian_pickaxe', 'Obsidian Pickaxe', 'epic', 'obsidian', {'band': 'crying', 'gem': 'crying'}),
    ('tunneler', 'Tunneler', 'epic', 'redstone', {'band': 'iron', 'gem': 'redstone'}),
    ('ender_pickaxe', 'Ender Pickaxe', 'epic', 'purpur', {'band': 'ender', 'gem': 'ender'}),
    ('worldpick', 'Worldpick', 'legendary', 'emerald', {'band': 'prismarine', 'gem': 'magma', 'pommel_gem': 'sculk', 'post': 'worldpick'}),
    ('briseroc', 'Briseroc', 'legendary', 'obsidian', {'band': 'netherite', 'gem': 'crying', 'pommel_gem': 'sea_lantern'}),
    ('warden_pickaxe', 'Warden Pickaxe', 'relic', 'sculk', {'post': 'warden'}),
    ('starbreaker', 'Starbreaker', 'relic', 'star', {'post': 'star'}),
    ('worldheart', 'Worldheart', 'relic', 'sculk', {'post': 'worldheart'}),
]
for mat, pre in COPPER:
    PICKAXES.insert(3, (f'{pre}copper_pickaxe', f'{pre.replace("_", " ").title()}Copper Pickaxe', 'uncommon', mat, {}))
    PICKAXES.insert(4, (f'waxed_{pre}copper_pickaxe', f'Waxed {pre.replace("_", " ").title()}Copper Pickaxe', 'uncommon', mat, {'wax': True}))

AXES = [
    ('flint_hatchet', 'Flint Hatchet', 'common', 'flint', {'knot': 'string'}),
    ('stone_hatchet', 'Stone Hatchet', 'common', 'cobble', {'knot': 'string'}),
    ('woodsmans_axe', "Woodsman's Axe", 'uncommon', 'iron', {}),
    ('birch_axe', 'Birch Axe', 'uncommon', 'birch', {'post': 'feather'}),
    ('spruce_axe', 'Spruce Axe', 'uncommon', 'spruce', {'post': 'snow'}),
    ('poplar_axe', 'Poplar Axe', 'uncommon', 'poplar', {'post': 'poplar'}),
    ('orchard_axe', 'Orchard Axe', 'uncommon', 'iron', {'post': 'apple'}),
    ('jungle_axe', 'Jungle Axe', 'rare', 'jungle', {'edge': 'diamond', 'handle': 'jungle'}),
    ('crimson_axe', 'Crimson Axe', 'rare', 'crimson', {'edge': 'diamond', 'handle': 'crimson', 'gem': 'glow'}),
    ('warped_axe', 'Warped Axe', 'rare', 'warped', {'edge': 'diamond', 'handle': 'warped', 'gem': 'ender'}),
    ('breeze_axe', 'Breeze Axe', 'rare', 'breeze', {'handle': 'copper_exposed', 'gem': 'breeze'}),
    ('golden_bough', 'Golden Bough', 'rare', 'gold', {'handle': 'oak_log', 'gem': 'apple', 'post': 'leaf'}),
    ('acacia_axe', 'Acacia Axe', 'rare', 'acacia', {'edge': 'diamond', 'handle': 'acacia', 'gem': 'candle'}),
    ('cherry_axe', 'Cherry Axe', 'rare', 'cherry', {'edge': 'diamond', 'handle': 'cherry', 'gem': 'pink_petals'}),
    ('mangrove_axe', 'Mangrove Axe', 'rare', 'mangrove', {'edge': 'diamond', 'handle': 'mangrove', 'gem': 'moss'}),
    ('pale_oak_axe', 'Pale Oak Axe', 'rare', 'pale_oak', {'edge': 'diamond', 'handle': 'pale_oak', 'gem': 'resin'}),
    ('heartwood_axe', 'Heartwood Axe', 'epic', 'dark_oak', {'band': 'obsidian', 'gem': 'apple'}),
    ('chorus_axe', 'Chorus Axe', 'epic', 'purpur', {'band': 'ender', 'gem': 'ender'}),
    ('tempest_axe', 'Tempest Axe', 'epic', 'breeze', {'band': 'copper_oxidized', 'gem': 'breeze', 'handle': 'copper_oxidized'}),
    ('verdant_axe', 'Verdant Axe', 'epic', 'emerald', {'band': 'gold', 'gem': 'apple', 'handle': 'oak_log', 'post': 'leaf'}),
    ('yggdrasil', 'Yggdrasil', 'legendary', 'gold', {'band': 'leaves_green', 'gem': 'emerald', 'handle': 'dark_oak_log', 'pommel_gem': 'leaves_green', 'post': 'leaf'}),
    ('elder_axe', 'Elder Axe', 'legendary', 'dark_oak', {'band': 'netherite', 'gem': 'apple', 'pommel_gem': 'emerald'}),
    ('timberfall', 'Timberfall', 'relic', 'netherite', {'post': 'timberfall'}),
    ('evokers_hatchet', "Evoker's Hatchet", 'relic', 'fang', {'post': 'fang'}),
    ('creaking_axe', 'Creaking Axe', 'relic', 'pale_oak', {'post': 'creaking'}),
    ('motherroot', 'Motherroot', 'relic', 'mangrove', {'post': 'roots'}),
    ('worldfeller', 'Worldfeller', 'relic', 'netherite', {'post': 'worldfeller'}),
    ('grovewarden', 'Grovewarden', 'relic', 'pale_oak', {'post': 'grovewarden'}),
]
for mat, pre in COPPER:
    AXES.insert(3, (f'{pre}copper_axe', f'{pre.replace("_", " ").title()}Copper Axe', 'uncommon', mat, {}))
    AXES.insert(4, (f'waxed_{pre}copper_axe', f'Waxed {pre.replace("_", " ").title()}Copper Axe', 'uncommon', mat, {'wax': True}))


# --- post effects (small themed pixels) -------------------------------------
def post(p: Pix, kind, name, head):
    g = R('gold'); s = R('soul'); c = R('sculk')
    if name == 'lichen':
        l = R('lichen'); p.set(8, 3, l[3]); p.set(12, 8, l[2]); p.set(6, 3, l[2])
    elif name == 'magnet':
        p.set(5, 3, R('redstone')[2]); p.set(13, 11, R('iron')[3])
    elif name == 'echo':
        p.set(8, 3, s[3]); p.set(12, 8, s[2])
    elif name == 'worldpick':
        p.set(9, 3, R('magma')[2]); p.set(13, 9, R('sculk')[2])
    elif name == 'warden':
        # sculk head, soul-fire glow, two antennae rising from the head
        p.set(8, 3, s[3]); p.set(12, 8, s[2]); p.set(13, 9, s[1])
        p.put([(6, 1), (10, 1)], c[1]); p.put([(5, 0), (11, 0)], s[2])
        recolor_cells(p, cells_handle(kind), 'deepslate'); recolor_cells(p, cells_knot(kind), 'sculk')
        recolor_cells(p, cells_pommel(kind), 'netherite'); p.set(3, 13, s[2])
    elif name in ('star', 'worldheart'):
        core = R('diamond') if name == 'star' else s
        body = R('star') if name == 'star' else c
        # star rays around a heavy head: up, left tip, right tip, plus sparkles
        p.put([(8, 1)], body[2]); p.put([(8, 0)], core[3]); p.put([(7, 1), (9, 1)], darken(body[0], 0.45))
        p.set(4, 3, body[2]); p.set(3, 3, core[3])
        p.set(13, 12, body[2]); p.set(13, 13, core[3])
        p.put([(4, 1), (12, 1)], core[3])
        p.set(8, 3, core[2]); p.set(12, 8, core[2])
        recolor_cells(p, cells_handle(kind), 'netherite' if name == 'star' else 'deepslate')
        recolor_cells(p, cells_knot(kind), 'gold'); p.set(10, 6, core[2])
        recolor_cells(p, cells_pommel(kind), 'gold'); p.set(3, 13, core[2])
        if name == 'worldheart':
            p.put([(6, 1), (10, 1)], c[1]); p.put([(5, 0), (11, 0)], s[2])
    elif name == 'feather':
        f = R('feather'); p.set(9, 2, f[3]); p.set(8, 3, f[2])
    elif name == 'snow':
        p.set(9, 2, (240, 248, 255, 255)); p.set(8, 3, (210, 230, 245, 255))
    elif name == 'poplar':
        p.set(9, 2, R('leaves_green')[3]); p.set(8, 3, R('candle')[2]); p.set(10, 3, R('apple')[2])
    elif name == 'apple':
        a = R('apple'); p.set(9, 3, a[2]); p.set(9, 2, a[3]); p.set(10, 2, R('leaves_green')[2])
    elif name == 'leaf':
        l = R('leaves_green'); p.set(10, 2, l[2]); p.set(11, 3, l[1]); p.set(5, 10, l[2])
    elif name == 'timberfall':
        heavy_head(p, kind, head); double_bit(p, head)
        p.put([(11, 6)], g[2]); p.put([(8, 2)], R('netherite')[3])
        recolor_cells(p, cells_handle(kind), 'dark_oak_log'); recolor_cells(p, cells_knot(kind), 'gold')
        p.set(9, 7, R('emerald')[2]); recolor_cells(p, cells_pommel(kind), 'gold'); p.set(3, 13, R('emerald')[2])
        finial(p, kind)
    elif name in ('fang', 'worldfeller'):
        f = R('fang')
        # jagged fang edge: notch pixels on the cutting edge
        out = darken(f[0], 0.5)
        p.set(7, 3, f[3]); p.set(6, 4, out); p.set(6, 5, f[2]); p.set(5, 5, out); p.set(6, 6, out); p.set(5, 6, f[3]); p.set(4, 6, out)
        p.set(7, 2, out); p.set(8, 2, f[3])
        recolor_cells(p, cells_handle(kind), 'dark_handle'); recolor_cells(p, cells_knot(kind), 'netherite')
        p.set(9, 7, R('amethyst')[2]); recolor_cells(p, cells_pommel(kind), 'netherite'); p.set(3, 13, R('amethyst')[2])
        if name == 'worldfeller':
            heavy_head(p, kind, 'fang'); double_bit(p, 'netherite')
            recolor_cells(p, cells_handle(kind), 'dark_oak_log'); recolor_cells(p, cells_knot(kind), 'gold')
            finial(p, kind); p.set(13, 6, R('amethyst')[2])
    elif name in ('creaking', 'grovewarden'):
        e = R('creaking_eye')
        p.set(8, 3, e[2]); p.set(10, 4, e[2])
        recolor_cells(p, cells_handle(kind), 'dark_oak_log'); recolor_cells(p, cells_knot(kind), 'dark_oak_log')
        recolor_cells(p, cells_pommel(kind), 'pale_moss')
        p.set(7, 10, R('pale_moss')[3]); p.set(5, 11, R('pale_moss')[2])
        if name == 'grovewarden':
            heavy_head(p, kind, 'pale_oak'); double_bit(p, 'pale_oak')
            p.set(13, 6, e[2]); roots(p); finial(p, kind)
            recolor_cells(p, cells_knot(kind), 'gold'); p.set(9, 7, e[2])
    elif name == 'roots':
        roots(p)
        recolor_cells(p, cells_handle(kind), 'mangrove'); recolor_cells(p, cells_knot(kind), 'moss')
        recolor_cells(p, cells_pommel(kind), 'mangrove'); p.set(3, 13, R('leaves_green')[2])
        heavy_head(p, kind, 'mangrove')


def roots(p):
    rt = R('dark_oak_log'); l = R('leaves_green')
    p.set(6, 7, rt[1]); p.set(6, 8, rt[0]); p.set(5, 9, rt[1])
    p.set(11, 9, rt[1]); p.set(12, 9, rt[0]); p.set(12, 10, rt[1])
    p.set(13, 5, l[2]); p.set(14, 6, l[1])


def build(kind, spec):
    file, disp, rarity, head, o = spec
    p = base(kind, head)
    if rarity == 'relic':
        p = apply_rarity(p, kind, 'epic', head, handle=o.get('handle'), gem=o.get('gem', 'diamond'), band=o.get('band'))
        post(p, kind, o['post'], head)
    else:
        p = apply_rarity(p, kind, rarity, head, knot=o.get('knot'), handle=o.get('handle'), gem=o.get('gem', 'diamond'),
                         band=o.get('band'), edge=o.get('edge'), pommel_gem=o.get('pommel_gem', 'diamond'))
        if o.get('post'):
            post(p, kind, o['post'], head)
    if o.get('wax'):
        honeycomb(p, kind)
    return p


def render(outdir):
    res = {}
    for kind, specs in (('pickaxe', PICKAXES), ('axe', AXES)):
        for spec in specs:
            p = build(kind, spec)
            p.save(os.path.join(outdir, 'items', spec[0] + '.png'))
            res[spec[0]] = (p, spec)
    return res


if __name__ == '__main__':
    render(OUT)
