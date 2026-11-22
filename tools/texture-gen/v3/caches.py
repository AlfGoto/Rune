"""Rune Cache (one per Rarity) and Rune Codex item textures."""
from pix import Pix, hx, darken, item
from palettes import R, _cache
import runes

_cache['magenta'] = [hx(h) for h in ('7a1f7a', 'c23ac2', 'ff55ff', 'ffb3ff')]  # Epic name colour
_cache['codex'] = [hx(h) for h in ('1b1630', '2b2450', '3d3470', '524893')]  # Codex leather (deep indigo)

# -- Rune Cache: a sealed coffer. Body wood, metal bands, a wax-like seal in the Rarity's name colour.
CACHE = [
    '................',
    '................',
    '.oooooooooooooo.',
    '.oHLLALLLLALLDo.',
    '.oLMMaMMMMaMMDo.',
    '.oLMMaMMMMaMMDo.',
    '.oAAAAATTAAAAAo.',
    '.oeeeeSTSSeeeeo.',
    '.oLMMaSSSsaMMDo.',
    '.oLMMaMssMaMMDo.',
    '.oLMMaMMMMaMMDo.',
    '.oLMMaMMMMaMMDo.',
    '.oDDDeDDDDeDDDo.',
    '.oooooooooooooo.',
    '................',
    '................',
]

# Rarity -> (body, bands, seal). Same ladder as the Rune stones and the tools.
CACHE_MATS = {
    'common': ('oak', 'cobble', 'iron'),
    'uncommon': ('spruce', 'copper', 'emerald'),
    'rare': ('dark_oak', 'gold', 'diamond'),
    'epic': ('obsidian', 'crying', 'magenta'),
    'legendary': ('netherite', 'gold', 'flame'),
}
CACHE_SPARKLES = [(0, 4, 0), (15, 9, 2), (12, 1, 4), (0, 12, 6), (15, 2, 8), (3, 15, 10), (11, 14, 1), (14, 15, 7)]


def cache(rarity, frame=0):
    body, band, seal = CACHE_MATS[rarity]
    b, a, s = R(body), R(band), R(seal)
    key = {'o': darken(b[0], 0.4), 'H': b[3], 'L': b[2], 'M': b[1], 'D': b[0],
           'A': a[2], 'a': a[1], 'e': a[0], 'T': s[3], 'S': s[2], 's': s[1]}
    p = Pix().grid(CACHE, key)
    if rarity == 'legendary':
        for x, y, start in CACHE_SPARKLES:
            age = (frame - start) % runes.FRAMES
            if age < len(runes._LIFE):
                runes._sparkle(p, x, y, runes._LIFE[age])
    return p


def cache_frames(rarity):
    n = runes.FRAMES if rarity == 'legendary' else 1
    return [cache(rarity, f) for f in range(n)]


# -- Rune Codex: the vanilla book, indigo leather, gold corners, a glowing rune on the cover.
def codex():
    p = Pix(img=item('book'))
    c = R('codex')
    gold = R('gold')
    p.remap({'312104': darken(c[0], 0.6), '522e10': c[1], '654b17': c[2], '543e13': c[1], '44250a': c[0],
             '161005': darken(c[0], 0.5)})
    # gold corner caps on the cover
    p.put([(8, 1), (9, 1), (0, 5), (1, 5), (14, 5), (15, 6)], gold[2])
    # glowing rune on the cover (a small diamond with a cross stroke)
    glow = R('soul')
    p.put([(8, 3), (6, 5), (10, 5), (8, 7)], glow[2])
    p.put([(7, 4), (9, 4), (7, 6), (9, 6)], glow[1])
    p.set(8, 5, glow[3])
    return p


if __name__ == '__main__':
    import os
    from pix import OUT
    for r in runes.RARITIES:
        cache(r).save(os.path.join(OUT, 'caches', f'rune_cache_{r}.png'))
    codex().save(os.path.join(OUT, 'caches', 'rune_codex.png'))
