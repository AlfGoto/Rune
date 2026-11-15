"""Rune axes. Same ladder and parts as the pickaxes (kit.py)."""
from kit import *

B = Book()
add = B.add
K = 'axe'
EDGE = [(9, 2), (10, 2), (8, 3), (7, 4), (7, 5)]          # cutting-edge highlight cells (AXE)


def edge(c, cells=EDGE):
    return [(x, y, c) for x, y in cells]


DIAMOND_EDGE = edge('#d5fff6')                              # Rare+: diamond bit honed into the edge


def wood(planks, bark):
    """Head in planks colours (light->dark: H L M D) with outline, handle = that tree's log."""
    H, L, M, D = planks
    return pal(D, H, L, M, '#1e140a', logh(bark), S=D)


# ================= COMMON
FLINT = pal('#2e2d2d', '#a8a8a8', '#7f7f7f', '#565656', '#0e0e0e', S='#3d3c3c')
add('flint_hatchet', 'Flint Hatchet', 'C', 'Sturdy', AXE, FLINT,
    [(9, 3, '#3d3c3c'), (10, 2, '#7f7f7f'), (8, 4, '#a8a8a8'), (12, 7, '#3d3c3c')] + knot(K, STRING))

STONE = pal('#494949', '#9a9a9a', '#898989', '#7f7f7f', '#181818', S='#616161')
add('stone_hatchet', 'Stone Hatchet', 'C', 'Speed', AXE, STONE,
    [(9, 3, '#616161'), (8, 4, '#a6a6a6'), (10, 4, '#6e6d6d'), (11, 6, '#525252'),
     (9, 2, '#a8a8a8')]                                              # flint chip set into the edge
    + knot(K, STRING))

# ================= UNCOMMON
COPPER_STAGES = [
    ('copper', 'Copper', pal('#5f2c1c', '#fc9982', '#e3826c', '#d66d48', '#421c11', S='#9c4e31'), []),
    ('exposed_copper', 'Exposed Copper', pal('#5a3a2c', '#ce8d83', '#ba8277', '#a87762', '#3a2620', S='#7f7257'),
     [(9, 4, '#7a9869'), (11, 6, '#7a9869')]),
    ('weathered_copper', 'Weathered Copper', pal('#2f4a3a', '#7ab799', '#66a977', '#6c975c', '#1e2e26', S='#497164'),
     [(8, 4, '#a87762'), (12, 7, '#8d8770')]),
    ('oxidized_copper', 'Oxidized Copper', pal('#24493c', '#6ec59f', '#59b292', '#4fab90', '#15302a', S='#3e816b'), []),
]
for key, title, p, ov in COPPER_STAGES:
    base = ov + [(9, 3, p['S'])] + knot(K, COPPERM) + reinforced()
    add(key + '_axe', title + ' Axe', 'U', 'Speed', AXE, p, base)
    add('waxed_' + key + '_axe', 'Waxed ' + title + ' Axe', 'U', 'Speed', AXE, p,
        base + [(9, 2, '#fffde0'), (7, 5, '#fffde0'), (10, 3, '#fabf29'), (12, 6, '#fabf29')])

IRONH = pal('#444444', '#ffffff', '#d8d8d8', '#c1c1c1', '#181818', S='#969696')
add('woodsmans_axe', "Woodsman's Axe", 'U', 'Sturdy', AXE, IRONH,
    [(10, 3, '#3d3c3c'), (9, 4, '#565656'), (10, 4, '#7f7f7f'),     # flint core still showing
     (8, 1, '#444444'), (11, 3, '#a8a8a8')]                          # beard rivet
    + knot(K, IRON) + reinforced())

BIRCH = wood(('#d7cb8d', '#c8b77a', '#ae9f76', '#7a6e4c'), ('#36342a', '#bebeae', '#eef1ea', '#ffffff'))
add('birch_axe', 'Birch Axe', 'U', 'Birch', AXE, BIRCH,
    [(9, 3, '#9e8b61'), (11, 6, '#605e54'),
     (4, 11, '#36342a'), (6, 9, '#36342a'), (3, 12, '#36342a'),      # black bark marks on the white handle
     (12, 1, '#d3d3d3'), (13, 1, '#ffffff'), (12, 2, '#969696')]     # feather tucked in the binding
    + knot(K, IRON) + cap())

SPRUCE = wood(('#886539', '#7a5a34', '#614b2e', '#3a2a14'), ('#2e1608', '#3b2713', '#553a1f', '#4d3317'))
add('spruce_axe', 'Spruce Axe', 'U', 'Spruce', AXE, SPRUCE,
    [(9, 1, '#ffffff'), (10, 1, '#f4fcfc'), (8, 2, '#ecfbfb'), (11, 2, '#c8dcff'),   # snow cap on the head
     (9, 2, '#bcd4ff'), (10, 2, '#c8dcff'), (8, 3, '#a1c3ff'), (7, 4, '#a1c3ff'), (7, 5, '#86aefd'),  # frosted edge
     (9, 4, '#553a1f')]
    + knot(K, IRON) + reinforced())

POPLAR = wood(('#d8d2c0', '#bdb6a2', '#a29b88', '#6a6556'), ('#3c3a30', '#7d7a6c', '#b7b3a4', '#9e9a8b'))
add('poplar_axe', 'Poplar Axe', 'U', 'Poplar', AXE, POPLAR,
    [(8, 0, '#b4131e'), (9, 0, '#ff5e1a'), (10, 0, '#f4c020'),       # one leaf of each colour
     (7, 1, '#ff1c2b'), (8, 1, '#c03d0a'), (11, 1, '#e9b115'),
     (9, 3, '#a29b88'), (12, 7, '#6a6556')]
    + knot(K, IRON) + reinforced())

ORCH = pal('#444444', '#ffffff', '#d8d8d8', '#c1c1c1', '#181818', S='#969696')
add('orchard_axe', 'Orchard Axe', 'U', 'Harvest', AXE, ORCH,
    [(13, 4, '#542409'), (14, 3, '#5cd160'),                          # apple stem + leaf on the poll
     (13, 5, '#ff5e69'), (14, 5, '#ff1c2b'), (13, 6, '#dd1725'), (14, 6, '#9c1017'), (15, 6, '#54090e'),
     (13, 7, '#b4131e'), (14, 7, '#54090e'),
     (9, 4, '#64922d'), (8, 4, '#48a54b')]                            # sapling leaf etched on the cheek
    + knot(K, IRON) + reinforced())

# ================= RARE (gold knot + gem, diamond-honed edge)
JUNG = wood(('#bf8e6b', '#aa7954', '#976a44', '#68462f'), ('#3e3013', '#59461a', '#766626', '#635820'))
add('jungle_axe', 'Jungle Axe', 'R', 'Yggdrasil', AXE, JUNG,
    DIAMOND_EDGE + [(9, 4, '#4c2b13'), (10, 4, '#976746'), (11, 6, '#704425'),       # cocoa pods
                    (6, 9, '#4fc353'), (5, 10, '#48a54b'), (8, 7, '#4fc353'), (4, 12, '#48a54b')]  # vines on the log
    + knot(K, GOLD, GEM['diamond']))

CRIM = wood(('#b0476f', '#863e5a', '#6a344b', '#3f1e2d'), ('#442131', '#7b0000', '#b12727', '#961515'))
add('crimson_axe', 'Crimson Axe', 'R', 'Yggdrasil', AXE, CRIM,
    DIAMOND_EDGE + [(9, 4, '#5c3042'), (12, 7, '#4b2737'),
                    (12, 5, '#fedd9f'), (13, 5, '#fe8738'), (13, 6, '#d7510b')]       # shroomlight bead
    + knot(K, GOLD, GEM['diamond']))

WARP = wood(('#3a8e8c', '#287067', '#1f5752', '#113835'), ('#452d5c', '#136c68', '#14956f', '#118066'))
add('warped_axe', 'Warped Axe', 'R', 'Standalone', AXE, WARP,
    DIAMOND_EDGE + [(9, 4, '#14b485'), (10, 4, '#1e4340'), (11, 6, '#14b485'),
                    (13, 5, '#349988'), (13, 6, '#105e51'), (14, 5, '#8cf4e2')]       # ender-pearl stud
    + knot(K, GOLD, GEM['diamond']))

BREEZE = pal('#2f527f', '#bfafc9', '#958dd3', '#7a82c4', '#1b2f4f', H_BREEZE, S='#576c9d')
add('breeze_axe', 'Breeze Axe', 'R', 'Speed', AXE, BREEZE,
    [(9, 3, '#fc9982'), (10, 4, '#d66d48'), (8, 4, '#b5a5ca'),        # copper core still inside
     (13, 2, '#bfafc9'), (14, 3, '#958dd3'), (13, 4, '#576c9d'),      # wind swirl off the poll
     (14, 5, '#bfafc9')]
    + DIAMOND_EDGE + knot(K, GOLD, GEM['breeze']))

GBOUGH = pal('#752802', '#fffde0', '#fdf55f', '#e9b115', '#3f2e0e', S='#b26411')
add('golden_bough', 'Golden Bough', 'R', 'Harvest', AXE, GBOUGH,
    [(13, 4, '#542409'), (14, 3, '#5cd160'),                          # golden apple on the poll
     (13, 5, '#f6f892'), (14, 5, '#eaee57'), (13, 6, '#eccb45'), (14, 6, '#dba213'), (15, 6, '#752802'),
     (13, 7, '#b26411'), (14, 7, '#752802'),
     (9, 4, '#5cd160'), (8, 4, '#48a54b')]                            # leaf etching kept
    + DIAMOND_EDGE + knot(K, GOLD, GEM['apple']))

ACAC = wood(('#c26d3f', '#ad5d32', '#99502b', '#6a3a1f'), ('#504b40', '#696259', '#8d8477', '#7b7368'))
add('acacia_axe', 'Acacia Axe', 'R', 'Standalone', AXE, ACAC,
    DIAMOND_EDGE + [(9, 4, '#884728'),
                    (13, 4, '#fdf55f'), (12, 3, '#e9b115'), (14, 3, '#e9b115'), (13, 2, '#e9b115'),
                    (14, 5, '#e9b115'), (12, 5, '#b26411'), (13, 3, '#5a3010')]       # sunflower on the poll
    + knot(K, GOLD, GEM['diamond']))

CHER = wood(('#e7cac5', '#e6b3ad', '#dd9d97', '#a45f66'), ('#271620', '#3b232d', '#543543', '#462c39'))
add('cherry_axe', 'Cherry Axe', 'R', 'Yggdrasil', AXE, CHER,
    DIAMOND_EDGE + [(9, 4, '#cd8580'),
                    (12, 1, '#f6a1d4'), (13, 2, '#ee68aa'), (14, 1, '#fccbe7'),       # petals drifting off
                    (5, 10, '#f396cd'), (7, 8, '#fccbe7')]
    + knot(K, GOLD, GEM['diamond']))

MANG = wood(('#8b4d3a', '#773934', '#6f2a2d', '#4a1a1c'), ('#3c2f23', '#4e3f27', '#675230', '#5a482c'))
add('mangrove_axe', 'Mangrove Axe', 'R', 'Standalone', AXE, MANG,
    DIAMOND_EDGE + [(9, 4, '#5d1c1e'),
                    (13, 5, '#5cd160'), (13, 6, '#4fc353'), (13, 7, '#488238'), (13, 8, '#82914a'),  # hanging propagule
                    (1, 14, '#443522'), (1, 15, '#3c2f23'), (4, 15, '#443522'), (3, 15, '#5a482c')]  # root tips at the end
    + knot(K, GOLD, GEM['diamond']))

PALE = wood(('#fffbf8', '#e7e3e1', '#d2c6c7', '#8f8584'), ('#3a3331', '#4e4340', '#7d7270', '#6e6462'))
add('pale_oak_axe', 'Pale Oak Axe', 'R', 'Standalone', AXE, PALE,
    [(9, 4, '#c0b2b2'), (10, 4, '#ef8737'), (11, 6, '#ec7214'),       # creaking-heart eye + resin seep
     (12, 7, '#fab326'), (12, 8, '#e05504'),                           # resin drip
     (13, 4, '#696e67'), (14, 5, '#585d57')]                           # hanging pale moss
    + DIAMOND_EDGE + knot(K, GOLD, GEM['resin']))

# ================= EPIC (big blade, two materials, dark handle + pommel)
HEART = pal('#3a1f0e', '#c58742', '#a05b23', '#85400f', '#1a0d05', H_NETH, S='#5e2f0c')
add('heartwood_axe', 'Heartwood Axe', 'E', 'Sturdy', AXE_BIG, HEART,
    [(8, 4, '#d1a75d'), (9, 4, '#a05b23'), (8, 5, '#85400f'), (9, 5, '#d1a75d'),      # growth rings
     (6, 4, '#8308e4'), (5, 5, '#6006ab'), (7, 2, '#ffffff'), (8, 1, '#d8d8d8'),     # abyssal + steel edge
     (6, 3, '#d8d8d8'), (5, 4, '#a8a8a8'),
     (10, 3, '#41e67f')]                                                              # totem emerald eye
    + knot(K, NETH, GEM['totem']) + grip(DARK_WRAP) + pommel(NETH))

CHOR = pal('#3a143a', '#eed6ee', '#ab8aab', '#7c5b7c', '#1f0a1f', H_NETH, S='#562e56')
add('chorus_axe', 'Chorus Axe', 'E', 'Yggdrasil', AXE_BIG, CHOR,
    [(8, 4, '#562e56'), (9, 5, '#562e56'), (8, 5, '#d6bbd6'),
     (12, 2, '#fbf3fe'), (13, 2, '#d6bbd6'), (12, 3, '#997c99'), (13, 3, '#5b345b'),  # chorus flower bud
     (13, 5, '#976997'), (14, 6, '#523460'), (13, 6, '#845289')]                      # shulker plate
    + knot(K, NETH, GEM['chorus']) + grip(dict(dark='#06030b', mid='#271e3d', light='#3b2754')) + pommel(NETH))

TEMP = pal('#1b2f4f', '#ffffff', '#bfafc9', '#7a82c4', '#0c1628', H_BREEZE, S='#3e5e87')
add('tempest_axe', 'Tempest Axe', 'E', 'Speed', AXE_BIG, TEMP,
    [(12, 1, '#bfafc9'), (13, 2, '#958dd3'), (14, 2, '#bfafc9'), (14, 3, '#576c9d'),  # storm swirl
     (13, 4, '#958dd3'), (14, 5, '#bfafc9'), (15, 4, '#7a82c4'),
     (8, 4, '#fdf55f'), (9, 5, '#fdf55f'), (8, 5, '#e9b115'),                         # lightning fork
     (6, 4, '#8308e4'), (6, 5, '#6006ab')]                                            # abyssal plates
    + knot(K, NETH, GEM['breeze']) + grip(DARK_WRAP) + pommel(NETH, '#bfafc9'))

VERD = pal('#0e4a1c', '#dbffeb', '#5cd160', '#2f9e3a', '#062a0e', H_NETH, S='#1c6d2a')
add('verdant_axe', 'Verdant Axe', 'E', 'Harvest', AXE_BIG, VERD,
    [(13, 4, '#542409'), (14, 3, '#5cd160'), (12, 2, '#64922d'), (11, 1, '#5cd160'),  # sapling sprouting from the poll
     (13, 5, '#f6f892'), (14, 5, '#eaee57'), (13, 6, '#c51339'), (14, 6, '#dba213'),  # enchanted golden apple
     (8, 4, '#fdf55f'), (9, 5, '#e9b115')]                                            # gold leaf veins
    + knot(K, GOLD, GEM['emerald']) + grip(GOLD_WRAP) + pommel(NETH, '#5cd160'))

# ================= LEGENDARY (double-bit, finial, gem pommel)
YGG = pal('#1d140a', '#ffffff', '#59b292', '#2e7d5a', '#0c0806', H_NETH, S='#3a2a14')
add('yggdrasil', 'Yggdrasil', 'L', 'Yggdrasil', AXE_DOUBLE, YGG,
    [   # blade = five essence woods in bands, back blade = the world-tree canopy
        (7, 3, '#886539'), (8, 2, '#ffffff'), (6, 4, '#553a1f'), (7, 4, '#886539'),      # spruce (snow-tipped)
        (8, 3, '#bf8e6b'), (8, 4, '#976a44'), (9, 3, '#e7cac5'), (9, 2, '#f6a1d4'),      # jungle, cherry
        (10, 2, '#fccbe7'), (9, 4, '#b0476f'), (10, 3, '#863e5a'), (9, 5, '#6a344b'),    # crimson
        (10, 4, '#ab8aab'), (8, 5, '#7c5b7c'), (7, 5, '#eed6ee'), (6, 5, '#562e56'),     # chorus
        (13, 5, '#5cd160'), (14, 6, '#48a54b'), (13, 7, '#2f9e3a'), (14, 7, '#4fc353'),
        (14, 8, '#1c6d2a'), (13, 6, '#82f6ad'), (15, 7, '#0e4a1c'), (13, 8, '#0e4a1c'),
    ] + knot(K, GOLD, GEM['beacon']) + finial(K) + grip(GOLD_WRAP) + pommel(GOLD, '#5cd160'))

ELDER = pal('#1a0d05', '#d1a75d', '#a05b23', '#85400f', '#0a0502', H_NETH, S='#5e2f0c')
add('elder_axe', 'Elder Axe', 'L', 'Sturdy', AXE_DOUBLE, ELDER,
    [(8, 4, '#d1a75d'), (9, 4, '#a05b23'), (8, 5, '#5e2f0c'), (9, 5, '#d1a75d'),      # growth rings
     (6, 4, '#d5fff6'), (5, 5, '#4bede6'), (6, 3, '#d5fff6'), (7, 2, '#ffffff'),     # diamond edge
     (8, 1, '#4bede6'), (5, 4, '#9efeeb'),
     (10, 3, '#ef8737'), (13, 7, '#ef8737'), (14, 7, '#fab326'),                     # creaking-heart eyes
     (11, 2, GOLD['hi']), (14, 6, GOLD['mid']), (15, 7, GOLD['lo'])]                 # gold filigree
    + knot(K, GOLD, GEM['resin']) + finial(K) + grip(GOLD_WRAP) + pommel(GOLD, '#ef8737'))

# ================= RELICS
TIMBER = [
    "................",
    "........OOO.....",
    ".......OHHHO..l.",
    "......OHHMLO.lgl",
    ".....OHLMMMef.l.",
    "....OHLLMMSMiO..",
    "....DDHLMMSMMMO.",
    "......DDDeMSMMD.",
    "........efiMMD..",
    ".......ehi.DD...",
    "......efi.......",
    ".....efi........",
    "....ehi.........",
    "...efi..........",
    "..ehi...........",
    "..ii............",
]
TB = pal('#3a2a14', '#ffffff', '#d8d8d8', '#a8a8a8', '#181818', H_STICK, S='#5e5e5e')
TB.update(l='#4fc353', g='#2f9e3a')
add('timberfall', 'Timberfall', 'Relic', 'Relics', TIMBER, TB,
    [(7, 5, '#c26d3f'), (8, 6, '#99502b'), (6, 6, '#6a3a1f'),                          # mansion dark-oak inlay
     (9, 4, '#53381a'), (10, 5, '#3e2912'), (8, 4, '#d8d8d8'),
     (6, 7, '#7e6237'), (7, 7, '#53381a')]
    + knot(K, GOLD, GEM['emerald']) + [(10, 4, GOLD['hi']), (11, 4, GOLD['mid']), (10, 5, GEM['emerald']), (11, 5, GOLD['lo'])]
    + grip(dict(dark='#291a0c', mid='#53381a', light='#7e6237')) + pommel(GOLD, '#17dd62'))

EVOK = [
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
EV = pal('#2a2435', '#e6e6e6', '#a8a8a8', '#6c716b', '#121016', dict(e='#3b2a3f', f='#5a3f5e', h='#7a5a7e', i='#1a1220'), S='#454a42')
add('evokers_hatchet', "Evoker's Hatchet", 'Relic', 'Relics', EVOK, EV,
    [   # the blade IS an evoker fang: jaw teeth along the edge
        (9, 0, '#c1c1c1'), (6, 3, '#e6e6e6'), (5, 4, '#a8a8a8'), (5, 5, '#e6e6e6'), (5, 6, '#6c716b'),
        (8, 3, '#2a2435'), (7, 5, '#2a2435'), (9, 2, '#2a2435'),               # gaps between teeth
        (9, 4, '#3b6aa0'), (10, 3, '#5aa8e0'),                                # vex-blue rune
        (13, 5, '#00aa2c'), (14, 6, '#17dd62'), (13, 7, '#005300'),           # emerald trophy
    ] + knot(K, GOLD, GEM['emerald']) + grip(dict(dark='#1a1220', mid='#5a1a1a', light='#8a2a2a'))
    + pommel(dict(hi='#e6e6e6', mid='#a8a8a8', lo='#6c716b', out='#121016'), '#5aa8e0'))

CREAK = [
    "................",
    "........OO......",
    ".......OHHO.m...",
    "......OHMLO.mm..",
    ".....OHMMMMef...",
    ".....DHLMSMMi...",
    "......DDDeMSMD..",
    "........efiMMDm.",
    ".......ehi.DD.m.",
    "......efi.....m.",
    ".....efi........",
    "....ehi.........",
    "...efi..........",
    "..ehi...........",
    "..ii............",
    "................",
]
CK = pal('#3a3331', '#e7e3e1', '#7d7270', '#5e5350', '#1c1817', dict(e='#4e4340', f='#6e6462', h='#7d7270', i='#2a2422'), S='#433b39')
CK['m'] = '#696e67'
add('creaking_axe', 'Creaking Axe', 'Relic', 'Relics', CREAK, CK,
    [(8, 4, '#ef8737'), (9, 4, '#3a3331'), (8, 5, '#3a3331'), (9, 5, '#ec7214'),      # two glowing creaking eyes
     (10, 3, '#fab326'), (7, 5, '#e7e3e1'),
     (12, 8, '#fab326'), (12, 9, '#e05504')]                                          # resin drip
    + knot(K, dict(hi='#efa337', mid='#ec7214', lo='#af4b16', out='#6e2b17'), '#fdf4b4')
    + grip(dict(dark='#2a2422', mid='#433b39', light='#696e67'))
    + pommel(dict(hi='#efa337', mid='#ec7214', lo='#af4b16', out='#6e2b17'), '#ef8737'))

MOTHER = [
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
    "....rrfi........",
    "...r.ehi........",
    "...efi.r........",
    "..ehi...........",
    ".rii.r..........",
    "r..r............",
]
MR = pal('#3c2f23', '#d4ccc3', '#a6846a', '#8a6a53', '#1e1610', dict(e='#4e3f27', f='#675230', h='#7d6b3e', i='#2e2418'), S='#6d533f')
MR['r'] = '#5a482c'
add('motherroot', 'Motherroot', 'Relic', 'Relics', MOTHER, MR,
    [(9, 4, '#5cd160'), (10, 3, '#48a54b'), (8, 4, '#73c676'),                       # moss on the head
     (13, 4, '#7fbf56'), (14, 3, '#5cd160'), (13, 3, '#488238'),                     # sprout from the poll
     (8, 3, '#1dc2d1'), (10, 4, '#d4ccc3')]                                           # buried-treasure: heart of the sea fleck
    + knot(K, GOLD, GEM['heart']))

# ================= FUSED
WFELL = [
    "........OOO.....",
    ".......OHHHO..l.",
    "......OHHMLO.lgl",
    ".....OHLMMMLO.l.",
    "....OHLMMMMef...",
    "....DHLLMMSMiO..",
    "...DDHLMMMSMMHO.",
    ".....DDDDeMSMLMD",
    "........efiMMMD.",
    ".......ehi.DMD..",
    "......efi...D...",
    ".....efi........",
    "....ehi.........",
    "...efi..........",
    "..ehi...........",
    "..ii............",
]
WF = pal('#121016', '#ffffff', '#c1c1c1', '#6c716b', '#08070a', H_NETH, S='#454a42')
WF.update(l='#4fc353', g='#2f9e3a')
add('worldfeller', 'Worldfeller', 'Fused', 'Relics', WFELL, WF,
    [(4, 5, '#e6e6e6'), (3, 6, '#a8a8a8'), (5, 6, '#2a2435'), (6, 3, '#2a2435'),     # fang teeth on the edge
     (8, 4, '#53381a'), (9, 5, '#3e2912'), (7, 6, '#7e6237'),                        # dark-oak inlay
     (9, 3, '#5aa8e0'), (10, 4, '#3b6aa0'),
     (14, 7, '#00aa2c'), (13, 7, '#17dd62')]
    + [(11, 4, GOLD['hi']), (12, 4, GOLD['mid']), (11, 5, '#17dd62'), (12, 5, GOLD['lo'])]
    + finial(K) + grip(GOLD_WRAP) + pommel(GOLD, '#5aa8e0'))

GROVE = [
    "................",
    "........OOO..m..",
    ".......OHHHO.mm.",
    "......OHHMLO..m.",
    ".....OHLMMMef...",
    ".....DHLMMSMiO..",
    "......DDDeMSMHO.",
    "........efiMMLMD",
    ".......ehi.DMMDm",
    "......efi...DD.m",
    "....rrfi........",
    "...r.ehi........",
    "...efi.r........",
    "..ehi...........",
    ".rii.r..........",
    "r..r............",
]
GW = pal('#3a3331', '#e7e3e1', '#7d7270', '#5e5350', '#1c1817', dict(e='#4e3f27', f='#675230', h='#7d6b3e', i='#2e2418'), S='#433b39')
GW.update(m='#5cd160', r='#5a482c')
add('grovewarden', 'Grovewarden', 'Fused', 'Relics', GROVE, GW,
    [(8, 4, '#ef8737'), (9, 4, '#3a3331'), (8, 5, '#3a3331'), (9, 5, '#ec7214'),     # creaking eyes
     (7, 2, '#5cd160'), (8, 1, '#48a54b'), (6, 4, '#73c676'),                        # living moss edge
     (13, 6, '#fab326'), (14, 7, '#ef8737'), (13, 7, '#5cd160'), (14, 8, '#488238')]
    + [(11, 4, GOLD['hi']), (12, 4, GOLD['mid']), (11, 5, '#ef8737'), (12, 5, GOLD['lo'])]
    + finial(K))

P = B.items
TOOLS = set(P)
