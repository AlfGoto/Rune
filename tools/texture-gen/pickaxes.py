"""Rune pickaxes, v2 (fancier ladder). See kit.py for the parts."""
from kit import *

B = Book()
add = B.add
K = 'pick'

# ================= COMMON
FLINT = pal('#2e2d2d', '#a8a8a8', '#7f7f7f', '#565656', '#0e0e0e')
add('flint_pickaxe', 'Flint Pickaxe', 'C', 'Sturdy', PICK, FLINT,
    [(8, 3, '#3d3c3c'), (10, 3, '#a8a8a8'), (13, 7, '#3d3c3c'), (13, 9, '#a8a8a8'),   # knapped facets
     (8, 4, '#565656'), (12, 8, '#565656')]                                              # sharp lower edge
    + knot(K, STRING))

COAL = pal('#1c1c1e', '#7d8594', '#4a4f58', '#393e46', '#0d0d10')
STONE = dict(hi='#a6a6a6', mid='#888788', lo='#616161', out='#3f3f3f')
add('coal_pickaxe', 'Coal Pickaxe', 'C', 'Speed', PICK, COAL,
    [(8, 3, '#252525'), (9, 3, '#a9b3c4'), (13, 7, '#252525'), (13, 8, '#a9b3c4'), (10, 4, '#2e2e2e')]
    + knot(K, STONE))   # cobblestone lump binding

# ================= UNCOMMON
IRONBAND = [(8, 2, IRON['out']), (8, 3, IRON['hi']), (8, 4, IRON['lo']),
            (12, 8, IRON['lo']), (13, 8, IRON['hi']), (14, 8, IRON['out'])]
add('ironbound_pickaxe', 'Ironbound Pickaxe', 'U', 'Sturdy', PICK, FLINT,
    [(10, 3, '#a8a8a8'), (13, 9, '#a8a8a8'), (6, 4, IRON['mid']), (12, 6, IRON['mid'])]  # rivets
    + IRONBAND + knot(K, IRON) + reinforced())

COPPER_STAGES = [
    ('copper', 'Copper', pal('#5f2c1c', '#fc9982', '#e3826c', '#d66d48', '#421c11'),
     [(9, 3, '#c15a36'), (13, 7, '#c15a36')]),
    ('exposed_copper', 'Exposed Copper', pal('#5a3a2c', '#ce8d83', '#ba8277', '#a87762', '#3a2620'),
     [(9, 3, '#7a9869'), (13, 8, '#7a9869'), (7, 4, '#947661')]),
    ('weathered_copper', 'Weathered Copper', pal('#2f4a3a', '#7ab799', '#66a977', '#6c975c', '#1e2e26'),
     [(7, 3, '#a87762'), (13, 9, '#8d8770'), (10, 4, '#497164')]),
    ('oxidized_copper', 'Oxidized Copper', pal('#24493c', '#6ec59f', '#59b292', '#4fab90', '#15302a'),
     [(9, 3, '#3e816b'), (13, 7, '#3e816b')]),
]
for key, title, p, ov in COPPER_STAGES:
    add(key + '_pickaxe', title + ' Pickaxe', 'U', 'Speed', PICK, p, ov + knot(K, COPPERM) + reinforced())
    add('waxed_' + key + '_pickaxe', 'Waxed ' + title + ' Pickaxe', 'U', 'Speed', PICK, p,
        ov + knot(K, COPPERM) + reinforced() + [(6, 3, '#fffde0'), (13, 10, '#fffde0'), (8, 3, '#fabf29'), (13, 6, '#fabf29')])

LICH = pal('#2c3a18', '#8fb03a', '#70922d', '#50692c', '#172010')
add('lichen_pickaxe', 'Lichen Pickaxe', 'U', 'Biomes', PICK, LICH,
    [(6, 3, '#aeaf8c'), (7, 2, '#739285'), (13, 10, '#aeaf8c'), (14, 9, '#739285'),     # glow lichen creeping on tips
     (8, 3, '#f4c05e'), (9, 4, '#f19645'), (13, 7, '#f4c05e'), (12, 7, '#bf6717'),       # glow berries
     (10, 3, '#495e27')]
    + knot(K, dict(hi='#8fb03a', mid='#64922d', lo='#2b541e', out='#172010')) + reinforced())  # vine binding

BONE = pal('#7b7e6b', '#fffffd', '#e8e5d2', '#cbc6a5', '#3e3f35')
add('bone_pickaxe', 'Bone Pickaxe', 'U', 'Detection', PICK, BONE,
    [(8, 3, '#3e3f35'), (9, 3, '#7b7e6b'), (9, 4, '#3e3f35'),                            # skull eye socket
     (4, 3, '#cbc6a5'), (4, 4, '#7b7e6b'), (14, 11, '#7b7e6b'), (13, 11, '#cbc6a5'),     # knuckle ends
     (13, 7, '#cbc6a5')]
    + knot(K, IRON) + reinforced())

MAG = pal('#574d39', '#fedec8', '#d8af93', '#af8e77', '#2e2820')
add('magnetite_pickaxe', 'Magnetite Pickaxe', 'U', 'Loot', PICK, MAG,
    [(5, 3, '#7a0a0a'), (6, 3, '#ff1414'), (7, 3, '#c41010'),                            # compass-red north end
     (13, 10, '#d8d8d8'), (13, 9, '#a8a8a8'),                                             # steel south end
     (9, 3, '#887455'), (10, 3, '#e9c8b1'), (13, 7, '#887455'), (12, 6, '#665237'),
     (6, 1, '#5e5e5e'), (14, 6, '#5e5e5e')]                                               # iron filings clinging
    + knot(K, IRON) + reinforced())

# ================= RARE (gold knot + gem, sharpened tips)
RED = pal('#5c0700', '#ff5a3c', '#e62008', '#a41808', '#2d0000')
add('redstone_pickaxe', 'Redstone Pickaxe', 'R', 'Speed', PICK, RED,
    [(5, 3, '#5f2c1c'), (6, 3, '#fc9982'), (13, 10, '#fc9982'), (13, 11, '#421c11'),     # copper tips kept
     (8, 3, '#ff0000'), (9, 4, '#720000'), (13, 7, '#ff0000'), (12, 8, '#720000'),       # dust traces
     (10, 3, '#ff9a80')]
    + sharpen(K, '#421c11') + knot(K, GOLD, GEM['diamond']) + reinforced())

GEO = pal('#3a3a40', '#fecbe6', '#b38ef3', '#8d6acc', '#1e1e24')
add('geode_pickaxe', 'Geode Pickaxe', 'R', 'Loot', PICK, GEO,
    [(6, 4, '#dfe0dc'), (12, 7, '#dfe0dc'), (12, 9, '#dfe0dc'), (10, 4, '#dfe0dc'),      # calcite layer
     (8, 1, '#cfa0f3'), (9, 1, '#54398a'), (15, 7, '#cfa0f3'), (15, 8, '#54398a'),       # crystal points poking out
     (5, 3, '#7a0a0a'), (6, 3, '#ff1414')]                                               # magnetite north end kept
    + sharpen(K, '#1e1e24') + knot(K, GOLD, GEM['diamond']) + reinforced())

EMER = pal('#005300', '#82f6ad', '#17dd62', '#00aa2c', '#002d00')
add('emerald_pickaxe', 'Emerald Pickaxe', 'R', 'Biomes', PICK, EMER,
    [(4, 3, '#494845'), (5, 3, '#817e76'), (6, 3, '#a3a393'), (4, 4, '#292724'),         # curled goat-horn tip
     (13, 10, '#a3a393'), (13, 11, '#595959'), (13, 12, '#292724'),
     (8, 3, '#dbffeb'), (9, 3, '#009529'), (13, 7, '#009529'), (13, 6, '#affdcd')]       # facets
    + knot(K, GOLD, GEM['diamond']) + reinforced())

PRIS = pal('#2e6053', '#a4d1c2', '#7cb3a4', '#539882', '#16352d')
add('prismarine_pickaxe', 'Prismarine Pickaxe', 'R', 'Biomes', PICK, PRIS,
    [(8, 3, '#43806e'), (8, 4, '#2e6053'), (13, 7, '#43806e'), (12, 7, '#2e6053'),      # brick seams
     (10, 3, '#dfe9dc'), (13, 9, '#dfe9dc'),                                             # crystals
     (5, 3, '#ae4635'), (6, 3, '#d4ccc3'), (7, 3, '#a6846a'),                            # nautilus-shell tips
     (13, 10, '#d4ccc3'), (13, 11, '#ae4635')]
    + knot(K, GOLD, GEM['diamond']) + reinforced())

FIRE = pal('#411616', '#fbaa59', '#f48522', '#652828', '#200a0a', H_BLAZE)
add('fiery_pickaxe', 'Fiery Pickaxe', 'R', 'Biomes', PICK, FIRE,
    [(8, 3, '#e66410'), (8, 4, '#ca4e06'), (13, 8, '#e66410'), (12, 8, '#ca4e06'),      # lava cracks
     (10, 4, '#f48522'), (7, 1, '#ffc100'), (8, 1, '#ff6a00'), (15, 7, '#ff6a00'),       # flame licks
     (5, 3, '#ffc100')]
    + sharpen(K, '#200a0a') + knot(K, GOLD, GEM['diamond']))

ECHO = pal('#052a32', '#29dfeb', '#0a5060', '#034150', '#0d1217')
add('echo_pickaxe', 'Echo Pickaxe', 'R', 'Detection / Biomes', PICK, ECHO,
    [(5, 3, '#7b7e6b'), (6, 3, '#e8e5d2'), (13, 10, '#e8e5d2'), (13, 11, '#3e3f35'),    # bone tips under sculk
     (8, 3, '#009295'), (9, 4, '#29dfeb'), (13, 7, '#009295'), (12, 8, '#29dfeb'),       # sculk veins
     (4, 4, '#29dfeb'), (13, 12, '#29dfeb')]                                             # echo-shard points
    + knot(K, GOLD, GEM['echo']) + reinforced())

# ================= EPIC (special head, two materials, dark handle + pommel)
OBS = pal('#100c1c', '#6a5a86', '#3b2754', '#271e3d', '#06030b', H_NETH)
NB = [(8, 1, NETH['out']), (8, 2, NETH['hi']), (8, 3, NETH['mid']), (8, 4, NETH['lo']),
      (12, 8, NETH['lo']), (13, 8, NETH['hi']), (14, 8, NETH['mid']), (15, 8, NETH['out'])]
add('obsidian_pickaxe', 'Obsidian Pickaxe', 'E', 'Sturdy', PICK_HEAVY, OBS,
    NB + [(10, 3, '#8308e4'), (10, 4, '#6006ab'), (6, 5, '#6006ab'), (6, 6, '#8308e4'),  # crying-obsidian tears dripping
          (13, 6, '#8308e4'), (6, 2, '#a8a8a8'), (5, 3, '#6a5a86')]
    + knot(K, NETH, '#8308e4') + grip(DARK_WRAP) + pommel(NETH))

TUN = pal('#2f2f37', '#797979', '#646464', '#515151', '#16161b', H_DEEP)
add('tunneler', 'Tunneler', 'E', 'Speed', PICK_WIDE, TUN,
    [(3, 3, '#5c0700'), (4, 3, '#ff5a3c'), (4, 4, '#a41808'),                            # redstone-hot edges
     (13, 12, '#ff5a3c'), (13, 13, '#5c0700'), (12, 12, '#a41808'),
     (7, 3, '#e62008'), (13, 8, '#e62008'), (9, 3, '#8d6acc'), (13, 10, '#8d6acc'),      # deep-core inlays
     (8, 4, '#7a5bb5'), (12, 9, '#7a5bb5')]
    + knot(K, dict(hi='#8fd16b', mid='#6fae4f', lo='#3f7a2f', out='#1e3a16'), '#ff0000')  # sticky slime knot
    + grip(dict(dark='#16161b', mid='#3d3d43', light='#646464')) + pommel(IRON))

END = pal('#432650', '#c7a8c7', '#ac7bac', '#906590', '#1f1226', H_NETH)
add('ender_pickaxe', 'Ender Pickaxe', 'E', 'Loot', PICK_HEAVY, END,
    [(4, 3, '#0b4d42'), (5, 3, '#2ccdb1'), (5, 2, '#8cf4e2'),                            # pearl tips
     (13, 10, '#2ccdb1'), (13, 11, '#0b4d42'), (14, 10, '#8cf4e2'),
     (8, 2, '#976997'), (8, 3, '#523460'), (8, 4, '#432650'),                            # shulker-shell plate seams
     (13, 7, '#523460'), (14, 7, '#976997'), (12, 7, '#432650'),
     (10, 3, '#b5e45a'), (13, 6, '#4e8386')]                                             # ender-eye glints
    + knot(K, NETH, GEM['ender']) + grip(dict(dark='#06030b', mid='#271e3d', light='#3b2754')) + pommel(NETH, '#2ccdb1'))

# ================= LEGENDARY (finial, gold filigree, gem pommel)
BRIS = pal('#100c1c', '#6a5a86', '#3b2754', '#271e3d', '#06030b', H_NETH)
GB = [(8, 1, GOLD['out']), (8, 2, GOLD['hi']), (8, 3, GOLD['mid']), (8, 4, GOLD['lo']),
      (12, 8, GOLD['lo']), (13, 8, GOLD['hi']), (14, 8, GOLD['mid']), (15, 8, GOLD['out'])]
add('briseroc', 'Briseroc', 'L', 'Sturdy', PICK_HEAVY, BRIS,
    GB + [(3, 4, '#0ebabd'), (4, 3, '#d6fffe'), (5, 2, '#4bede6'), (4, 4, '#4bede6'),   # diamond-capped tips
          (13, 11, '#4bede6'), (13, 10, '#d6fffe'), (14, 10, '#4bede6'), (13, 12, '#0ebabd'), (14, 11, '#0ebabd'),
          (10, 2, GOLD['hi']), (11, 2, GOLD['lo']), (15, 6, GOLD['lo']),                 # gold filigree
          (10, 3, '#8308e4'), (13, 6, '#8308e4'), (6, 3, '#4d494d')]
    + knot(K, GOLD, GEM['heart']) + finial(K) + grip(GOLD_WRAP) + pommel(GOLD, GEM['heart']))

WP = pal('#1d2a24', '#ffffff', '#59b292', '#2e7d5a', '#0c1410', H_NETH)
add('worldpick', 'Worldpick', 'L', 'Biomes', PICK, WP,
    [   # moss -> prismarine -> emerald (top arm), magma -> sculk (down arm)
        (4, 4, '#aeaf8c'), (5, 3, '#2c3a18'), (6, 3, '#8fb03a'), (7, 3, '#70922d'), (7, 4, '#50692c'), (6, 2, '#2c3a18'),
        (7, 2, '#f4c05e'),
        (8, 2, '#2e6053'), (8, 3, '#a4d1c2'), (8, 4, '#2e6053'), (9, 3, '#539882'), (9, 4, '#16352d'), (9, 2, '#2e6053'),
        (10, 2, '#005300'), (10, 3, '#17dd62'), (10, 4, '#00aa2c'), (11, 4, '#00aa2c'), (11, 5, '#82f6ad'),
        (12, 6, '#f48522'), (13, 6, '#fbaa59'), (12, 5, '#652828'), (13, 7, '#652828'), (13, 8, '#e66410'), (14, 7, '#411616'),
        (13, 9, '#0a5060'), (13, 10, '#29dfeb'), (12, 9, '#052a32'), (14, 9, '#052a32'), (12, 10, '#052a32'),
        (14, 10, '#052a32'), (13, 11, '#0d1217'), (13, 12, '#29dfeb'),
    ] + knot(K, GOLD, GEM['beacon']) + finial(K) + grip(GOLD_WRAP) + pommel(GOLD, '#17dd62'))

# ================= RELICS (unique silhouettes)
WARDEN = [
    "......k...k.....",
    "......k...k.....",
    "......OOOOO.....",
    "....OHLMxMLOef..",
    "....aODxDxMMhi..",
    "..........eLMD..",
    ".........efiMLDk",
    "........ehi.xMD.",
    ".......efi..DMDk",
    "......ehi...xLD.",
    ".....efi....DHD.",
    "....ehi......D..",
    "...efi..........",
    "..ehi...........",
    "..ii............",
    "................",
]
WP_PAL = pal('#0d1217', '#29dfeb', '#1f4b55', '#052a32', '#06090c', H_DEEP)
WP_PAL.update(k='#29dfeb', x='#009295', a='#d1d6b6')
add('warden_pickaxe', 'Warden Pickaxe', 'Relic', 'Relics', WARDEN, WP_PAL,
    [(6, 1, '#0d1217'), (10, 1, '#0d1217'),                                              # antennae roots
     (8, 3, '#29dfeb'), (13, 7, '#29dfeb')]                                              # soul-lit ribs
    + knot(K, dict(hi='#d1d6b6', mid='#819988', lo='#4b4c4f', out='#2d2d2d'), GEM['echo'])
    + grip(dict(dark='#06090c', mid='#034150', light='#009295'))
    + pommel(dict(hi='#d1d6b6', mid='#819988', lo='#4b4c4f', out='#2d2d2d'), '#29dfeb'))

STARB = [
    "........s.......",
    ".......sws......",
    "......OOsOO.....",
    ".....OHLMMLOef..",
    "....c.ODDDMMhi..",
    "..........eLMD..",
    ".........efiMLDs",
    "........ehi.DMDw",
    ".......efi..DMDs",
    "......ehi...DLD.",
    ".....efi....DHD.",
    "....ehi......D..",
    "...efi.......c..",
    "..ehi...........",
    "..ii............",
    "................",
]
ST = pal('#182c39', '#a7c7d8', '#507687', '#406278', '#0c0a15', dict(e='#373b35', f='#525a51', h='#6c716b', i='#292828'))
ST.update(s='#fdffa8', w='#ffffff', c='#d67b5b')
add('starbreaker', 'Starbreaker', 'Relic', 'Relics', STARB, ST,
    [(13, 4, '#ff6a00'), (5, 3, '#6d3421'), (6, 3, '#d67b5b'), (13, 10, '#d67b5b'),     # trial copper
     (8, 3, '#ffc195'), (13, 8, '#ff6a00'), (9, 4, '#bf4d00')]                            # active-vault ember
    + knot(K, GOLD, GEM['star']) + grip(dict(dark='#292828', mid='#6d3421', light='#d67b5b'))
    + pommel(dict(hi='#6c716b', mid='#525a51', lo='#373b35', out='#292828'), '#fdffa8'))  # heavy-core pommel

WHEART = [
    ".....k..s...k...",
    ".....k.sws..k...",
    "....OOOOsOOO....",
    "...OHLLMxMLOef..",
    "..aODDxDDxMMhi..",
    "..........eLMDk.",
    ".........efiMLDk",
    "........ehi.xMDs",
    ".......efi..DMDw",
    "......ehi...xLDs",
    ".....efi....DLD.",
    "....ehi.....xMD.",
    "...efi......DHD.",
    "..ehi........D..",
    "..ii.........a..",
    "................",
]
WH = pal('#0c0a15', '#ffffff', '#1f4b55', '#0a2a3a', '#05040a', H_NETH)
WH.update(k='#29dfeb', x='#009295', s='#fdffa8', w='#ffffff', a='#d67b5b')
add('worldheart', 'Worldheart', 'Fused', 'Relics', WHEART, WH,
    [(5, 3, '#29dfeb'), (13, 9, '#29dfeb')]
    + knot(K, GOLD, '#ff1414') + finial(K) + grip(GOLD_WRAP) + pommel(GOLD, '#ff1414'))

P = B.items
