"""Tool kit: vanilla 26.x pickaxe/axe templates + Tinkers-style parts (binding, grip, cap, pommel).

Fanciness ladder (same for every Job):
  Common    vanilla template, material speckle, string/flint binding knot
  Uncommon  + Reinforced Handle (leather grip + iron cap), metal binding, richer material
  Rare      + gold Gem Setting knot, sharpened tips, themed handle
  Epic      + special head shape, two-material head, dark handle with pommel
  Legendary + crest/finial, gold filigree, gem pommel, bespoke silhouette
  Relic     + unique silhouette built around its motif
"""
from legacy_pix import render, STICK

PICK = [
    "................",
    "................",
    "......OOOOO.....",
    ".....OHLMMLOef..",
    "......ODDDMMhi..",
    "..........eLMD..",
    ".........efiMLD.",
    "........ehi.DMD.",
    ".......efi..DMD.",
    "......ehi...DLD.",
    ".....efi....DHD.",
    "....ehi......D..",
    "...efi..........",
    "..ehi...........",
    "..ii............",
    "................",
]
PICK_HEAVY = [
    "................",
    "......OOOOO.....",
    ".....OHLLLMO....",
    "....OHLMMMLMef..",
    ".....ODDDDMMhiO.",
    "..........eLMMD.",
    ".........efiMLMD",
    "........ehi.DMMD",
    ".......efi..DMMD",
    "......ehi...DLMD",
    ".....efi....DHMD",
    "....ehi......DD.",
    "...efi..........",
    "..ehi...........",
    "..ii............",
    "................",
]
PICK_WIDE = [
    "................",
    "................",
    "....OOOOOOO.....",
    "...OHLLMMMLOef..",
    "....ODDDDDMMhi..",
    "..........eLMD..",
    ".........efiMLD.",
    "........ehi.DMD.",
    ".......efi..DMD.",
    "......ehi...DLD.",
    ".....efi....DLD.",
    "....ehi.....DMD.",
    "...efi......DHD.",
    "..ehi........D..",
    "..ii............",
    "................",
]
AXE = [
    "................",
    ".........OO.....",
    "........OHHO....",
    ".......OHMLO....",
    "......OHMMMef...",
    "......DHLMSMi...",
    ".......DDeMSMD..",
    "........efiMMD..",
    ".......ehi.DD...",
    "......efi.......",
    ".....efi........",
    "....ehi.........",
    "...efi..........",
    "..ehi...........",
    "..ii............",
    "................",
]
# bigger blade (Epic): blade grows one pixel down-left
AXE_BIG = [
    "................",
    "........OOO.....",
    ".......OHHHO....",
    "......OHHMLO....",
    ".....OHLMMMef...",
    ".....DHLMMSMi...",
    "......DDDeMSMD..",
    "........efiMMD..",
    ".......ehi.DD...",
    "......efi.......",
    ".....efi........",
    "....ehi.........",
    "...efi..........",
    "..ehi...........",
    "..ii............",
    "................",
]
# double-bit (Legendary / Fused): second, smaller blade behind the handle
AXE_DOUBLE = [
    "................",
    "........OOO.....",
    ".......OHHHO....",
    "......OHHMLO....",
    ".....OHLMMMef...",
    ".....DHLMMSMiO..",
    "......DDDeMSMHO.",
    "........efiMMLMD",
    ".......ehi.DMMD.",
    "......efi...DD..",
    ".....efi........",
    "....ehi.........",
    "...efi..........",
    "..ehi...........",
    "..ii............",
    "................",
]

# ------------------------------------------------------------------ materials
LEATHER = dict(dark='#3d1c10', mid='#542716', light='#893b25')
IRON = dict(hi='#d8d8d8', mid='#a8a8a8', lo='#5e5e5e', out='#353535')
GOLD = dict(hi='#fdf55f', mid='#e9b115', lo='#b26411', out='#752802')
NETH = dict(hi='#737173', mid='#5a575a', lo='#31292a', out='#111111')
COPPERM = dict(hi='#fc9982', mid='#d66d48', lo='#9c4e31', out='#5f2c1c')
STRING = dict(hi='#f7f7f7', mid='#dbdbdb', lo='#a0a0a0', out='#5a5a5a')

GOLD_WRAP = dict(dark='#752802', mid='#dc9613', light='#fdf55f')
DARK_WRAP = dict(dark='#111111', mid='#31292a', light='#4d494d')

H_STICK = STICK
H_NETH = dict(e='#31292a', f='#4d494d', h='#5a575a', i='#111111')
H_BLAZE = dict(e='#b9931c', f='#d8ae26', h='#fff32d', i='#953300')
H_BREEZE = dict(e='#576c9d', f='#7a82c4', h='#bfafc9', i='#2f527f')
H_DEEP = dict(e='#2f2f37', f='#3d3d43', h='#515151', i='#151519')
H_BONE = dict(e='#cbc6a5', f='#e8e5d2', h='#fffffd', i='#7b7e6b')


def logh(bark):
    """A log as a handle: 4 bark tones dark->light."""
    d, m, l, k = bark
    return dict(e=m, f=l, h=l, i=d)


GEM = dict(diamond='#4bede6', emerald='#17dd62', echo='#29dfeb', amethyst='#cfa0f3', ender='#b5e45a',
           star='#fdffa8', heart='#1dc2d1', beacon='#d6fffe', redstone='#ff0000', apple='#ff1c2b',
           resin='#fab326', totem='#41e67f', chorus='#eed6ee', breeze='#958dd3')


def pal(O, H, L, M, D, handle=H_STICK, S=None):
    p = dict(O=O, H=H, L=L, M=M, D=D, **handle)
    p['S'] = S or M
    return p


# ------------------------------------------------------------------ parts
def grip(m=LEATHER):
    return [(5, 10, m['mid']), (6, 10, m['light']), (7, 10, m['dark']),
            (4, 11, m['mid']), (5, 11, m['light']), (6, 11, m['dark'])]


def cap(m=IRON):
    return [(2, 13, m['lo']), (3, 13, m['hi']), (4, 13, m['out']), (2, 14, m['out']), (3, 14, m['out'])]


def pommel(m=GOLD, gem=None):
    """Bigger end piece (Epic+): 2x2 knob past the handle end."""
    out = [(2, 13, m['lo']), (3, 13, m['hi']), (4, 13, m['out']),
           (1, 14, m['out']), (2, 14, gem or m['mid']), (3, 14, m['lo']),
           (1, 15, m['out']), (2, 15, m['out'])]
    return out


def reinforced():
    return grip() + cap()


# junction knot cells (where the head meets the handle)
KNOT = dict(pick=[(12, 3), (13, 3), (12, 4), (13, 4)], axe=[(11, 4), (12, 4), (11, 5), (12, 5)])


def knot(kind, m, gem=None):
    a, b, c, d = KNOT[kind]
    return [(*a, m['hi']), (*b, m['mid']), (*c, gem or m['lo']), (*d, m['out'] if gem is None else m['lo'])]


def finial(kind, m=GOLD):
    """Legendary: the handle pokes out above the head as a little gold spike."""
    if kind == 'pick':
        return [(13, 2, m['mid']), (14, 1, m['hi']), (14, 2, m['lo'])]
    return [(13, 3, m['mid']), (14, 2, m['hi']), (13, 4, m['lo'])]


def sharpen(kind, c):
    """Rare+: one extra pixel on the cutting tips."""
    if kind == 'pick':
        return [(4, 4, c), (13, 12, c)]
    return [(5, 6, c)]


class Book:
    def __init__(self):
        self.items = {}

    def add(self, key, title, rarity, line, grid, palette, over=()):
        self.items[key] = dict(img=render(grid, palette, list(over)), title=title, rarity=rarity, line=line)
