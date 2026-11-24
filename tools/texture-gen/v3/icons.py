"""Font glyph icons: one 7x7 gem per Rarity, drawn in the Rarity's name colour (font rune:glyphs).

Shapes grow with Rarity: Common pebble, Uncommon rhombus, Rare cut gem, Epic crystal cluster, Legendary crown.
Cells are 8x8 (row 7 empty = below the baseline); the font uses height 8, ascent 7 like vanilla text.
"""
from PIL import Image
from pix import hx

# Rarity name colours (vanilla chat colours) as 4-tone ramps: dark, mid, light (= the name colour), highlight
RAMPS = {
    'common': ('8c8c8c', 'bdbdbd', 'e8e8e8', 'ffffff'),
    'uncommon': ('1f8a1f', '3cc43c', '55ff55', 'c4ffc4'),
    'rare': ('1f8f8f', '3cc9c9', '55ffff', 'd4ffff'),
    'epic': ('8f2a8f', 'cc44cc', 'ff55ff', 'ffd0ff'),
    'legendary': ('a35f00', 'db8700', 'ffaa00', 'ffe39a'),
}
RARITIES = list(RAMPS)
# H highlight, X light, M mid, D dark
SHAPES = {
    'common': ['.......', '..HXX..', '.HXXXM.', '.XXXMM.', '.XXMMD.', '..MDD..', '.......'],
    'uncommon': ['...H...', '..HXX..', '.HXXXM.', 'HXXXXMD', '.XXXMD.', '..XMD..', '...D...'],
    'rare': ['.......', '.HHXXM.', 'HHXXXMD', '.XXXMD.', '..XMD..', '...D...', '.......'],
    'epic': ['...H...', '..HXM.H', '.HXXMHM', '.HXXMXM', 'HXXXMXD', 'HXXMMDD', '.MMMDD.'],
    'legendary': ['.......', 'H..X..M', 'HX.H.XM', 'HXXXXXM', 'HXXXXXM', 'MDDDDDD', '.......'],
}
FIRST_CHAR = 0xE000  # private use:  common ..  legendary


def icon(rarity):
    d, m, l, h = (hx(c) for c in RAMPS[rarity])
    key = {'H': h, 'X': l, 'M': m, 'D': d}
    im = Image.new('RGBA', (8, 8), (0, 0, 0, 0))
    for y, row in enumerate(SHAPES[rarity]):
        for x, ch in enumerate(row):
            if ch != '.':
                im.putpixel((x, y), key[ch])
    return im


def strip():
    """All 5 icons side by side: one bitmap provider row."""
    im = Image.new('RGBA', (8 * len(RARITIES), 8), (0, 0, 0, 0))
    for i, r in enumerate(RARITIES):
        im.paste(icon(r), (8 * i, 0))
    return im


def chars():
    return ''.join(chr(FIRST_CHAR + i) for i in range(len(RARITIES)))
