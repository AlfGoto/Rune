"""Rune item textures, built from stacked layers (all 16x16, same canvas):

  layer0  base/<rarity>.png        the rune stone, one per Rarity
  layer1  logo/<rune>.png          the engraved symbol, one per Rune (variants share it)
  layer2  socket/<rune>_<v>.png    only for variant Runes (Specialist, Affinity, Sanctuary): the material in the socket
  layer3  rank/<n>[_max].png       Roman numeral Rank, I..XX; gold when the Rune is at its Max Rank

A Rune at a Rank is just `item/generated` with those layers, so no per-Rank PNG is ever drawn.
"""
import os
from pix import Pix, OUT, hx, darken, lighten, block, ramp_from
from palettes import R, _cache

# extra ramps for variants
for _n, _src in [('lapis', 'lapis_block'), ('dripstone', 'dripstone_block'), ('snow', 'snow')]:
    _cache[_n] = ramp_from(block(_src), 4, lo=0.1, hi=0.98)

# -- layer 0: bases -------------------------------------------------------------
# tablet: outline x2..13 / y1..14, rounded corners, inner bevel (top-left light, bottom-right dark)
def _tablet(body, rim=None, outline=None):
    p = Pix()
    d, m, l, h = R(body)
    o = outline or darken(d, 0.45)
    rd, rm, rl, rh = R(rim) if rim else (d, m, l, h)
    for y in range(1, 15):
        for x in range(2, 14):
            if (x, y) in [(2, 1), (13, 1), (2, 14), (13, 14)]:
                continue
            if x in (2, 13) or y in (1, 14):
                c = o
            elif y == 2 or x == 3:
                c = rh if (y == 2 and x == 3) else rl
            elif y == 13 or x == 12:
                c = rd
            else:
                c = m
            p.set(x, y, c)
    # worn-stone speckles near the edges only (keeps the logo area clean)
    for x, y in [(4, 12), (10, 3), (11, 11), (4, 4), (9, 12)]:
        p.set(x, y, l if (x + y) % 2 else d)
    return p


def _corners(p, mat, size=2):
    d, m, l, h = R(mat)
    o = darken(d, 0.45)
    for cx, cy, sx, sy in [(2, 1, 1, 1), (13, 1, -1, 1), (2, 14, 1, -1), (13, 14, -1, -1)]:
        pts = [(cx + sx, cy), (cx + 2 * sx, cy), (cx, cy + sy), (cx, cy + 2 * sy)]
        p.put(pts, o)
        p.set(cx + sx, cy + sy, l if sy > 0 else m)
        if size > 2:
            p.set(cx + 2 * sx, cy + sy, m)
            p.set(cx + sx, cy + 2 * sy, m)
    return p


def _gem_top(p, gem='diamond', setting='gold'):
    gd, gm, gl, gh = R(setting)
    p.put([(6, 1), (9, 1)], darken(gd, 0.45))
    p.put([(6, 2), (9, 2)], gm)
    e = R(gem)
    p.set(7, 1, e[3]); p.set(8, 1, e[2]); p.set(7, 2, e[1]); p.set(8, 2, e[0])
    return p


def base(rarity):
    if rarity == 'common':
        return _tablet('stone')
    if rarity == 'uncommon':
        return _corners(_tablet('stone'), 'copper')
    if rarity == 'rare':
        return _gem_top(_corners(_tablet('deepslate'), 'gold'))
    if rarity == 'epic':
        return _corners(_tablet('obsidian', rim='crying'), 'netherite', 3)
    if rarity == 'legendary':
        g = R('gold')
        p = _tablet('netherite', rim='gold', outline=darken(g[0], 0.5))
        _corners(p, 'gold', 3)
        p.put([(6, 1), (9, 1)], darken(g[0], 0.5))
        p.put([(7, 1), (8, 1)], g[3])  # gold cap, no gem
        p.put([(7, 0), (8, 0)], g[2])  # finial
        return p
    raise ValueError(rarity)


RARITIES = ['common', 'uncommon', 'rare', 'epic', 'legendary']

# -- Legendary magic: animated sparkles (item textures can animate with a .mcmeta) ----
# Each sparkle: (x, y, start frame). Life over 6 frames: dot, plus, big plus, plus, dot, off.
SPARKLES = [(1, 4, 0), (14, 9, 2), (12, 0, 4), (0, 11, 6), (15, 2, 8), (3, 15, 10), (11, 15, 1), (14, 13, 7)]
FRAMES, FRAMETIME = 12, 2
_LIFE = ['dot', 'plus', 'big', 'plus', 'dot']


def _sparkle(p, x, y, stage):
    core, arm, tip = hx('fffbe0'), hx('f2d36b'), hx('b58cff')
    if stage == 'dot':
        p.set(x, y, arm)
    elif stage == 'plus':
        p.set(x, y, core)
        for dx, dy in [(1, 0), (-1, 0), (0, 1), (0, -1)]:
            if not p.get(x + dx, y + dy)[3]:
                p.set(x + dx, y + dy, tip)
    elif stage == 'big':
        p.set(x, y, core)
        for dx, dy in [(1, 0), (-1, 0), (0, 1), (0, -1)]:
            if not p.get(x + dx, y + dy)[3]:
                p.set(x + dx, y + dy, arm)
            if not p.get(x + 2 * dx, y + 2 * dy)[3]:
                p.set(x + 2 * dx, y + 2 * dy, tip)


def legendary_frames():
    frames = []
    for f in range(FRAMES):
        p = base('legendary')
        for x, y, start in SPARKLES:
            age = (f - start) % FRAMES
            if age < len(_LIFE):
                _sparkle(p, x, y, _LIFE[age])
        frames.append(p)
    return frames


def animated_base(rarity):
    """Static frame used in previews (frame 0) for Legendary; plain base otherwise."""
    return legendary_frames()[0] if rarity == 'legendary' else base(rarity)


# -- layer 1: logos -------------------------------------------------------------
# 8x8 grids drawn at x4..11 / y3..10.  H highlight, X light, D mid (primary ramp);
# h / x / d same on the secondary ramp; S = socket cell (layer 2 fills it).
# A 1px engraving shadow is added below-right of every logo pixel.
LX, LY = 4, 3

RUNES = {
    # id: (name, rarity, max rank or None for no cap, primary, secondary, grid)
    # ---- Mining
    'haste': ('Haste', 'common', 5, 'candle', None, [
        '.....HX.', '....HX..', '...HX...', '..HXXXX.', '....XD..', '...XD...', '..XD....', '.XD.....']),
    'sturdy': ('Sturdy', 'common', 4, 'iron', None, [
        'HHHHHHXD', 'HXXXXXXD', 'HXXHHXXD', 'HXXHHXXD', '.HXXXXD.', '.HXXXXD.', '..HXXD..', '...XD...']),
    'specialist': ('Specialist: <ore / wood>', 'common', 3, 'star', None, [
        '..HHXX..', '.H....D.', 'H..SS..D', 'H.SSSS.D', 'X.SSSS.D', 'X..SS..D', '.X....D.', '..XDDD..']),
    'fortune': ('Fortune', 'uncommon', 3, 'emerald', None, [
        '.HX..HX.', 'HXXX.XXD', 'HXXXXXXD', '..XXXX..', '..XXXX..', 'HXXXXXXD', 'HXXD.XXD', '.XD..XD.']),
    'magnet': ('Magnet', 'uncommon', 3, 'redstone', 'iron', [
        '.HXXXXD.', 'HXXXXXXD', 'HXD..HXD', 'HXD..HXD', 'HXD..HXD', 'HXD..HXD', 'hxd..hxd', 'hxd..hxd']),
    'ore_sense': ('Ore Sense', 'uncommon', 3, 'soul', 'sculk', [
        '........', '..HXXX..', '.HX..XD.', 'HX.hx.XD', 'HX.xd.XD', '.XX..XD.', '..XXXD..', '........']),
    'affinity': ('Affinity: <place>', 'uncommon', 3, 'amethyst', None, [
        '..HXXD..', '.HXXXXD.', '.HSSSSD.', '.HSSSSD.', '.XSSSSD.', '..XSSD..', '...XD...', '...XD...']),
    'deepstride': ('Deepstride', 'uncommon', None, 'soul', None, [
        '...HX...', '...HX...', '...HX...', '...HX...', 'HXXHXXXD', '.HXXXXD.', '..HXXD..', '...XD...']),
    'smelt': ('Smelt', 'rare', 3, 'flame', None, [
        '...H....', '...HX...', '..HXX.H.', '..HXXDX.', '.HXHXXXD', '.HXHHXXD', '.HXHHXXD', '..XXXXD.']),
    'cache_hunter': ('Cache Hunter', 'rare', 3, 'gold', None, [
        '.HXXD...', 'HX..XD..', 'HX..XD..', '.XXXD...', '..XD....', '..XXD...', '..XD....', '..XXD...']),
    'ore_dash': ('Ore Dash', 'rare', 3, 'breeze', None, [
        '....H...', 'HX..HX..', '...HXXX.', 'HXXXXXXD', '...XXXD.', 'HX..XD..', '....D...', '........']),
    'blast_mine': ('Blast Mine', 'epic', 3, 'redstone', 'flame', [
        'H..H..X.', '.H.hx.D.', '..hxxd..', 'HhxHxxdD', '..xxxd..', '.X.xd.D.', 'X..D..D.', '........']),
    'ore_bloom': ('Ore Bloom', 'epic', 5, 'pink_petals', 'leaves_green', [
        '...HX...', '.HXhxXD.', '.HXxdXD.', '...XD...', '...d....', '.h.d.h..', '..dd.d..', '...d....']),
    'silk_touch': ('Silk Touch', 'epic', 1, 'oak', 'purpur', [
        '.HXXXXD.', '..HXXD..', '.hxxxxd.', '.hxhxxd.', '.hxxhxd.', '.hxxxxd.', '..HXXD..', '.HXXXXD.']),
    'great_blast': ('Great Blast', 'legendary', 3, 'flame', 'star', [
        'X..H..X.', '.H.HX.D.', '..HhxD..', 'HHhHHxDD', '..xxxD..', '.X.XD.D.', 'X..D..D.', '........']),
    'veinminer': ('Veinminer', 'legendary', 3, 'diamond', None, [
        'H.......', '.H...H..', '..X.H...', '...XX...', '...X.X..', '..X...XD', '.H....D.', 'H.....D.']),
    # ---- Woodcutting (Specialist and Affinity above are shared)
    'leaf_fall': ('Leaf Fall', 'uncommon', 3, 'leaves_green', 'leaves_green', [
        '.....HX.', '...HXXXD', '..HXXXXD', '.HXXXDD.', '.XXXD...', '.XD.....', 'X..h.h..', '...h..h.']),
    'orchard': ('Orchard', 'uncommon', 3, 'apple', 'leaves_green', [
        '....dh..', '...d.hx.', '.HXdXX..', 'HXXXXXXD', 'HXHXXXXD', 'HXXXXXXD', '.XXXXXD.', '..XD.D..']),
    'canopy_stride': ('Canopy Stride', 'uncommon', None, 'leather', 'leaves_green', [
        '.hxd....', '..HXD...', '..HXD...', '..HXD...', '..HXXXD.', '..HXXXXD', '..XXXXXD', '..DDDDDD']),
    'replant': ('Replant', 'rare', 1, 'leaves_green', 'oak_log', [
        '.HX..HX.', 'HXXDHXXD', '.HXDXXD.', '...hx...', '...hx...', '..dhxd..', '.dddddd.', '........']),
    'tree_dash': ('Tree Dash', 'rare', 3, 'leaves_green', 'oak_log', [
        '....HXX.', '...HXXXD', '...HXXXD', 'H...XXD.', '.....hd.', 'HX...hd.', '.....hd.', '....dddd']),
    'sanctuary': ('Sanctuary: <biome>', 'rare', 3, 'pink_petals', None, [
        '.HX..HX.', 'HXXXHXXD', 'HXSSSSXD', 'HXSSSSXD', '.XSSSSD.', '..XSSD..', '...XD...', '........']),
    'treefeller': ('Treefeller', 'epic', 3, 'leaves_green', 'oak_log', [
        '....HXXD', '...HXXXD', '..HXXXXD', '..XXXXD.', '.hdDD...', '.hd.....', 'hd......', 'dd......']),
    'boomerang': ('Boomerang', 'epic', 3, 'oak', None, [
        'HXXD....', 'HXXXD...', '.HXXD...', '..HXXD..', '..HXXD..', '.HXXD...', 'HXXD....', 'HXD.....']),
    'great_fell': ('Great Fell', 'legendary', 3, 'leaves_green', 'oak_log', [
        '.HX..HX.', 'HXXDHXXD', '.hd..hd.', '..HXXD..', '.HXXXXD.', '...hd...', '...hd...', '........']),
}

MINING = ['haste', 'sturdy', 'specialist', 'fortune', 'magnet', 'ore_sense', 'affinity', 'deepstride', 'smelt',
          'cache_hunter', 'ore_dash', 'blast_mine', 'ore_bloom', 'silk_touch', 'great_blast', 'veinminer']
WOODCUTTING = ['specialist', 'affinity', 'leaf_fall', 'orchard', 'canopy_stride', 'replant', 'tree_dash',
               'sanctuary', 'treefeller', 'boomerang', 'great_fell']


def _ramp_key(prim, sec):
    p = R(prim)
    k = {'H': p[3], 'X': p[2], 'D': p[1], 'S': darken(p[0], 0.6)}
    s = R(sec) if sec else p
    k.update({'h': s[3], 'x': s[2], 'd': s[1]})
    return k


def logo(rid):
    _, _, _, prim, sec, grid = RUNES[rid]
    key = _ramp_key(prim, sec)
    p = Pix()
    filled = {(LX + x, LY + y) for y, r in enumerate(grid) for x, ch in enumerate(r) if ch != '.'}
    shadow = darken(R(prim)[0], 0.35)
    for x, y in filled:
        if (x + 1, y + 1) not in filled:
            p.set(x + 1, y + 1, shadow)
    p.grid(grid, key, LX, LY)
    return p


def socket_cells(rid):
    return [(LX + x, LY + y) for y, r in enumerate(RUNES[rid][5]) for x, ch in enumerate(r) if ch == 'S']


# -- layer 2: sockets (variants) ------------------------------------------------
VARIANTS = {
    'specialist': {
        # Mining (ores)
        'coal': 'coal', 'copper': 'copper', 'iron': 'raw_iron', 'gold': 'gold', 'redstone': 'redstone',
        'lapis': 'lapis', 'diamond': 'diamond', 'emerald': 'emerald',
        # Woodcutting (woods)
        'oak': 'oak', 'spruce': 'spruce', 'birch': 'birch', 'jungle': 'jungle', 'acacia': 'acacia',
        'dark_oak': 'dark_oak', 'mangrove': 'mangrove', 'cherry': 'cherry', 'pale_oak': 'pale_oak',
        'poplar': 'poplar', 'crimson': 'crimson', 'warped': 'warped'},
    'affinity': {
        'lush_caves': 'moss', 'taiga': 'spruce', 'mineshaft': 'oak_log', 'deep_dark': 'sculk',
        'dripstone': 'dripstone', 'mountains': 'snow', 'jungle': 'jungle', 'crimson_forest': 'crimson'},
    'sanctuary': {'forest': 'leaves_green', 'cherry_grove': 'pink_petals', 'pale_garden': 'pale_moss',
                  'taiga': 'spruce', 'lush_caves': 'moss'},
}


def socket(rid, mat):
    cells = socket_cells(rid)
    xs = [c[0] for c in cells]; ys = [c[1] for c in cells]
    x0, x1, y0, y1 = min(xs), max(xs), min(ys), max(ys)
    d, m, l, h = R(mat)
    p = Pix()
    for x, y in cells:
        s = (x - x0) / max(1, x1 - x0) + (y - y0) / max(1, y1 - y0)  # 0 top-left .. 2 bottom-right
        p.set(x, y, h if s < 0.4 else l if s < 1.0 else m if s < 1.6 else d)
    return p


# -- layer 3: Roman numerals ----------------------------------------------------
FONT = {'I': ['X', 'X', 'X'], 'V': ['X.X', 'X.X', '.X.'], 'X': ['X.X', '.X.', 'X.X']}
MAX_NUMERAL = 20  # XVIII is the widest that fits (13 px + outline); Ranks above XX show XX


def roman(n):
    out = ''
    for v, s in [(10, 'X'), (9, 'IX'), (5, 'V'), (4, 'IV'), (1, 'I')]:
        while n >= v:
            out += s; n -= v
    return out


def numeral(n, maxed=False):
    s = roman(min(n, MAX_NUMERAL))
    w = sum(len(FONT[c][0]) for c in s) + len(s) - 1
    x0, y0 = (16 - w) // 2, 12
    fill = R('gold')[3] if maxed else hx('f2f2f2')
    edge = darken(R('gold')[0], 0.35) if maxed else hx('1c1c1c')
    pts = set()
    x = x0
    for c in s:
        g = FONT[c]
        for gy, row in enumerate(g):
            for gx, ch in enumerate(row):
                if ch == 'X':
                    pts.add((x + gx, y0 + gy))
        x += len(g[0]) + 1
    p = Pix()
    for px, py in pts:
        for dx, dy in [(-1, 0), (1, 0), (0, -1), (0, 1), (1, 1)]:
            if (px + dx, py + dy) not in pts:
                p.set(px + dx, py + dy, edge)
    p.put(pts, fill)
    return p


# -- composition ----------------------------------------------------------------
def compose(rid, rank=None, variant=None):
    name, rarity, max_rank = RUNES[rid][:3]
    p = animated_base(rarity).copy()
    p.blit(logo(rid))
    if variant:
        p.blit(socket(rid, VARIANTS[rid][variant]))
    if rank and max_rank != 1:
        p.blit(numeral(rank, maxed=(rank == max_rank)))
    return p


def export(out=None):
    out = out or os.path.join(OUT, 'runes')
    for r in RARITIES:
        if r == 'legendary':
            fr = legendary_frames()
            strip = Pix(16, 16 * len(fr))
            for i, f in enumerate(fr):
                strip.blit(f, 0, 16 * i)
            strip.save(f'{out}/base/{r}.png')
            with open(f'{out}/base/{r}.png.mcmeta', 'w') as fh:
                fh.write('{"animation": {"frametime": %d}}\n' % FRAMETIME)
        else:
            base(r).save(f'{out}/base/{r}.png')
    for rid in RUNES:
        logo(rid).save(f'{out}/logo/{rid}.png')
    for rid, vs in VARIANTS.items():
        for v, mat in vs.items():
            socket(rid, mat).save(f'{out}/socket/{rid}_{v}.png')
    for n in range(1, MAX_NUMERAL + 1):
        numeral(n).save(f'{out}/rank/{n}.png')
        numeral(n, True).save(f'{out}/rank/{n}_max.png')
    return out


if __name__ == '__main__':
    print(export())
