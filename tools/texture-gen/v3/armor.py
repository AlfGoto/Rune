"""Rune Armor: item icons (vanilla iron templates, recoloured + job motifs) and worn layers."""
import os
from pix import Pix, item, vanilla, darken, lighten, hx
from palettes import R

SLOTS = ['helmet', 'chestplate', 'leggings', 'boots']
IRON = ['2d2d2d', '191919', '6b6b6b', '969696', 'c6c6c6', 'd8d8d8', 'ffffff']


def armor_map(mat):
    r = R(mat)
    return {'2d2d2d': darken(r[0], 0.45), '191919': darken(r[0], 0.3), '6b6b6b': r[0], '969696': r[1],
            'c6c6c6': r[2], 'd8d8d8': r[3], 'ffffff': lighten(r[3], 0.35)}


def base(slot, mat):
    return Pix(img=item(f'iron_{slot}')).remap(armor_map(mat))


# set definitions: (base material, trim material or None, accent material)
MINING = {
    'common': ('leather', None, 'candle'),
    'uncommon': ('chain', 'copper', 'candle'),
    'rare': ('iron', 'gold', 'diamond'),
    'epic': ('obsidian', 'crying', 'sea_lantern'),
    'legendary': ('netherite', 'gold', 'sculk'),
}
WOODCUTTING = {
    'common': ('leather', None, 'leaves_green'),
    'uncommon': ('chain', 'oak', 'leaves_green'),
    'rare': ('moss', 'gold', 'diamond'),
    'epic': ('dark_oak', 'obsidian', 'resin'),
    'legendary': ('gold', 'dark_oak_log', 'leaves_green'),
}
NAMES = {
    'mining': {
        'common': ["Miner's Helmet", "Miner's Vest", "Miner's Trousers", "Miner's Boots"],
        'uncommon': ["Caver's Helmet", "Reinforced Chestplate", "Caver's Leggings", "Caver's Boots"],
        'rare': ["Driller's Helm", "Driller's Cuirass", "Driller's Leggings", "Driller's Boots"],
        'epic': ["Abyssal Helm", "Abyssal Cuirass", "Abyssal Leggings", "Abyssal Boots"],
        'legendary': ["Crown of the Depths", "Heart of the Mountain", "Roots of the World", "Step of the Chasm"],
    },
    'woodcutting': {
        'common': ["Lumberjack's Cap", "Lumberjack's Vest", "Lumberjack's Trousers", "Lumberjack's Boots"],
        'uncommon': ["Feller's Hood", "Feller's Jerkin", "Feller's Leggings", "Feller's Boots"],
        'rare': ["Sylvan Helm", "Sylvan Cuirass", "Sylvan Leggings", "Sylvan Boots"],
        'epic': ["Heartwood Helm", "Heartwood Cuirass", "Heartwood Leggings", "Heartwood Boots"],
        'legendary': ["Crown of the Grove", "Elder's Bark", "Treetop Stride", "Stag's Hooves"],
    },
}
RANKS = ['common', 'uncommon', 'rare', 'epic', 'legendary']


def slug(s):
    return s.lower().replace("'", '').replace(' ', '_')


def trim(p, slot, mat):
    """Thin trim line of a second material (Uncommon+)."""
    t = R(mat)
    if slot == 'helmet':
        p.put([(4, 10), (5, 10), (6, 10)], t[2]); p.put([(10, 10), (11, 10)], t[1])
        p.put([(4, 4), (5, 4)], t[2])
    elif slot == 'chestplate':
        p.put([(4, 12), (5, 12), (6, 12), (7, 12), (8, 12), (9, 12), (10, 12), (11, 12)], t[1])
        p.put([(2, 3), (3, 3)], t[3]); p.put([(12, 3), (13, 3)], t[2])
    elif slot == 'leggings':
        p.put([(4, 3), (5, 3), (6, 3), (7, 3), (8, 3), (9, 3), (10, 3), (11, 3)], t[2]); p.set(4, 3, t[3])
    elif slot == 'boots':
        p.put([(3, 4), (4, 4), (5, 4), (9, 4), (10, 4), (11, 4)], t[2])


def motif(p, job, slot, rank, acc):
    a = R(acc)
    if job == 'mining':
        if slot == 'helmet':
            # headlamp on the forehead (candle -> lantern -> gem -> sea lantern -> sculk crown)
            p.set(7, 6, a[3]); p.set(8, 6, a[2]); p.set(7, 5, a[1])
            if rank == 'legendary':
                crown(p, 'gold'); p.set(7, 6, a[2]); p.set(8, 6, a[3])
        elif slot == 'chestplate':
            p.set(7, 9, a[2]); p.set(8, 9, a[3]); p.set(7, 10, a[1]); p.set(8, 10, a[2])
            if rank == 'legendary':
                p.set(7, 8, a[3]); p.set(8, 8, a[3]); p.set(6, 9, a[1]); p.set(9, 9, a[1])
        elif slot == 'leggings':
            p.set(4, 8, a[2]); p.set(10, 8, a[2])
            if rank in ('epic', 'legendary'):
                p.set(5, 9, a[1]); p.set(9, 9, a[1])
        elif slot == 'boots':
            p.set(3, 10, a[2]); p.set(11, 10, a[2])
    else:
        if slot == 'helmet':
            p.set(5, 5, a[2]); p.set(4, 5, a[1]); p.set(5, 4, a[3])  # leaf / sprig on the side
            if rank == 'legendary':
                crown(p, 'leaves_green'); p.put([(5, 2), (8, 2), (10, 2)], R('leaves_green')[3])
                p.put([(4, 5), (5, 5), (5, 4)], R('gold')[2])
        elif slot == 'chestplate':
            p.set(7, 8, a[1]); p.set(7, 9, a[2]); p.set(8, 9, a[1]); p.set(8, 10, a[2])  # vertical bark / leaf line
            if rank == 'legendary':
                p.put([(6, 8), (9, 10)], a[3]); p.put([(6, 7), (9, 7)], R('gold')[2])
        elif slot == 'leggings':
            p.set(4, 8, a[2]); p.set(10, 8, a[2])
            if rank in ('epic', 'legendary'):
                p.set(5, 10, a[1]); p.set(9, 10, a[1])
        elif slot == 'boots':
            p.set(3, 10, a[2]); p.set(11, 10, a[2])
            if rank == 'legendary':  # Stag's Hooves: hoof-shaped dark toes
                d = R('dark_oak_log')
                p.put([(2, 12), (3, 12), (4, 12)], d[0]); p.put([(11, 12), (12, 12), (13, 12)], d[0])


def crown(p, mat):
    g = R(mat)
    p.put([(5, 3), (6, 3), (7, 3), (8, 3), (9, 3), (10, 3)], g[1])
    p.put([(5, 2), (8, 2), (10, 2)], g[3]); p.put([(6, 2), (9, 2)], g[0])


def build_icon(job, rank, slot):
    base_mat, trim_mat, acc = (MINING if job == 'mining' else WOODCUTTING)[rank]
    p = base(slot, base_mat)
    if trim_mat:
        trim(p, slot, trim_mat)
    motif(p, job, slot, rank, acc)
    return p


def layer_map(mat, trim_mat=None):
    """Luminance ramp for the 64x32 worn layers (iron greys -> material)."""
    r = R(mat)
    return [darken(r[0], 0.35), r[0], r[1], r[2], r[3], lighten(r[3], 0.35)]


def build_layers(job, rank):
    base_mat, trim_mat, acc = (MINING if job == 'mining' else WOODCUTTING)[rank]
    ramp = layer_map(base_mat)
    l1 = Pix(img=vanilla('entity/equipment/humanoid/iron.png')).tint_ranked(ramp)
    l2 = Pix(img=vanilla('entity/equipment/humanoid_leggings/iron.png')).tint_ranked(ramp)
    return l1, l2


def render(outdir):
    res = {}
    for job in ('mining', 'woodcutting'):
        for rank in RANKS:
            for i, slot in enumerate(SLOTS):
                disp = NAMES[job][rank][i]
                name = slug(disp)
                p = build_icon(job, rank, slot)
                p.save(os.path.join(outdir, 'items', name + '.png'))
                res[name] = (p, disp, job, rank, slot)
            l1, l2 = build_layers(job, rank)
            l1.save(os.path.join(outdir, 'equipment', 'humanoid', f'{job}_{rank}.png'))
            l2.save(os.path.join(outdir, 'equipment', 'humanoid_leggings', f'{job}_{rank}.png'))
    return res


if __name__ == '__main__':
    from pix import OUT
    render(OUT)
