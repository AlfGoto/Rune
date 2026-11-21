"""Material ramps, each sampled from a vanilla 26.1 texture: [dark, mid, light, highlight]."""
from pix import block, item, ramp_from, hx

_cache = {}


def R(name):
    return _cache[name]


def _b(name, src, **kw):
    _cache[name] = ramp_from(block(src), 4, **kw)


def _i(name, src, **kw):
    _cache[name] = ramp_from(item(src), 4, **kw)


def _lit(name, *hexes):
    _cache[name] = [hx(h) for h in hexes]


# -- stone & metal ------------------------------------------------------------
_i('flint', 'flint', lo=0.05, hi=0.85)
_i('coal', 'coal', lo=0.05, hi=0.8)
_b('stone', 'stone', lo=0.1, hi=0.95)
_b('cobble', 'cobblestone', lo=0.1, hi=0.95)
_b('copper', 'copper_block', lo=0.1, hi=0.98)
_b('copper_exposed', 'exposed_copper', lo=0.1, hi=0.98)
_b('copper_weathered', 'weathered_copper', lo=0.1, hi=0.98)
_b('copper_oxidized', 'oxidized_copper', lo=0.1, hi=0.98)
_lit('iron', '6b6b6b', 'c1c1c1', 'd8d8d8', 'ffffff')  # vanilla iron tool ramp
_b('gold', 'gold_block', lo=0.12, hi=0.98)
_i('diamond', 'diamond', lo=0.1, hi=0.98)
_b('netherite', 'netherite_block', lo=0.05, hi=0.95)
_b('obsidian', 'obsidian', lo=0.0, hi=0.98, skip_dark=0.0)
_b('crying', 'crying_obsidian', lo=0.2, hi=0.98, skip_dark=0.05)
_b('amethyst', 'amethyst_block', lo=0.1, hi=0.98)
_b('prismarine', 'prismarine', lo=0.1, hi=0.98)
_b('sculk', 'sculk', lo=0.1, hi=0.98, skip_dark=0.03)
_i('echo', 'echo_shard', lo=0.1, hi=0.98)
_i('emerald', 'emerald', lo=0.1, hi=0.98)
_b('redstone', 'redstone_block', lo=0.1, hi=0.98)
_b('bone', 'bone_block_side', lo=0.1, hi=0.98)
_b('raw_iron', 'raw_iron_block', lo=0.1, hi=0.98)
_b('magma', 'magma', lo=0.15, hi=0.98)
_i('blaze', 'blaze_rod', lo=0.1, hi=0.98)
_i('breeze', 'breeze_rod', lo=0.1, hi=0.98)
_b('moss', 'moss_block', lo=0.1, hi=0.98)
_b('lichen', 'glow_lichen', lo=0.3, hi=0.98, skip_dark=0.0)
_b('deepslate', 'deepslate', lo=0.05, hi=0.95)
_i('ender', 'ender_pearl', lo=0.1, hi=0.98)
_b('purpur', 'purpur_block', lo=0.1, hi=0.98)
_b('end_stone', 'end_stone', lo=0.1, hi=0.98)
_b('resin', 'resin_block', lo=0.1, hi=0.98)
_b('sea_lantern', 'sea_lantern', lo=0.2, hi=0.98)
_b('honey', 'honeycomb_block', lo=0.1, hi=0.98)
_b('chain', 'iron_bars', lo=0.1, hi=0.9) if False else _lit('chain', '3a3a3a', '6e6e6e', '9a9a9a', 'c8c8c8')
# -- woods --------------------------------------------------------------------
for w in ['oak', 'birch', 'spruce', 'jungle', 'acacia', 'dark_oak', 'mangrove', 'cherry', 'pale_oak', 'crimson', 'warped', 'bamboo']:
    _b(w, f'{w}_planks', lo=0.1, hi=0.98)
_lit('poplar', '8a8f5c', 'c2c68e', 'dcdfa8', 'f0f1c9')  # no poplar texture in 26.1 (invented)
_b('oak_log', 'oak_log', lo=0.1, hi=0.95)
_b('dark_oak_log', 'dark_oak_log', lo=0.05, hi=0.95)
_b('pale_moss', 'pale_moss_block', lo=0.1, hi=0.98)
_b('leaves', 'oak_leaves', lo=0.15, hi=0.95, skip_dark=0.0)
_lit('leaves_green', '2e5a1c', '4a8a2a', '62b03a', '8ad455')  # oak leaves are grey-tinted in vanilla
# -- soft materials -----------------------------------------------------------
_lit('leather', '4a2f17', '7a4c26', '9b6534', 'bf8552')  # vanilla leather item colours
_lit('string', '8f8f8f', 'd9d9d9', 'ededed', 'ffffff')
_b('wool', 'white_wool', lo=0.2, hi=0.98)
_i('feather', 'feather', lo=0.3, hi=0.98)
_lit('stick_dark', '281e0b', '493615', '684e1e', '896727')  # vanilla stick outline, dark, mid, light
_lit('dark_handle', '0e0b06', '1f1710', '352a19', '4e3f27')
_lit('pink_petals', 'a24d6f', 'd877a1', 'f0a0c6', 'ffd1e6')
_lit('apple', '7a1515', 'c22a2a', 'e83c3c', 'ff7b7b')
_lit('star', 'c7b25a', 'f2e38a', 'fff6c2', 'ffffff')
_lit('fang', '55524a', '9c988c', 'd6d2c4', 'f5f2ea')
_lit('creaking_eye', 'c43d00', 'ff6a00', 'ffa040', 'ffd27a')
_lit('glow', '6a7a1c', 'b8c83a', 'e0ec6a', 'f6ffa8')
_lit('soul', '1f5c63', '3ea0a8', '7fdbe0', 'c8f6f8')
_lit('candle', 'b08a2a', 'e6c04a', 'f7df85', 'ffe9a8')
_lit('flame', 'c24a10', 'f08020', 'ffc040', 'fff0a0')


def shade(name, i):
    return R(name)[i]
