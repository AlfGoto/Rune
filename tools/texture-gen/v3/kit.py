"""Shared tool-building kit: vanilla template + Tinkers'-style parts (head, knot, grip, pommel)."""
from pix import Pix, item, hx, darken
from palettes import R

IRON_HEAD = ['444444', 'ffffff', 'd8d8d8', 'c1c1c1', '969696']
STICK = {'281e0b': 0, '493615': 1, '684e1e': 2, '896727': 3}  # outline, dark, mid, light
OUT = '181818'


def head_map(mat, outline=None):
    r = R(mat)
    out = outline or darken(r[0], 0.45)
    return {'444444': out, 'ffffff': r[3], 'd8d8d8': r[2], 'c1c1c1': r[1], '969696': r[0]}


def cells_handle(kind):
    if kind == 'pickaxe':
        c = [(12, 3), (13, 3), (12, 4), (13, 4)]
        for y in range(5, 14):
            c += [(15 - y, y), (16 - y, y), (17 - y, y)]
    else:
        c = [(11, 4), (12, 4), (12, 5), (9, 6)]
        for y in range(7, 14):
            c += [(15 - y, y), (16 - y, y), (17 - y, y)]
    c += [(2, 14), (3, 14)]
    return c


def cells_knot(kind):
    if kind == 'pickaxe':
        return [(12, 3), (13, 3), (12, 4), (13, 4), (10, 5), (9, 6), (10, 6), (11, 6)]
    return [(11, 4), (12, 4), (12, 5), (9, 6), (8, 7), (9, 7), (10, 7)]


def cells_grip(kind):
    rows = (9, 10, 11) if kind == 'pickaxe' else (10, 11, 12)
    return [(15 - y, y) for y in rows] + [(16 - y, y) for y in rows] + [(17 - y, y) for y in rows]


def cells_pommel(kind):
    return [(2, 13), (3, 13), (4, 13), (2, 14), (3, 14)]


def recolor_cells(p: Pix, cells, mat, flat=False):
    """Recolor handle-type cells by their vanilla stick shade class.
    flat: softer contrast (themed handles made of bright materials)."""
    r = R(mat)
    idx = [0, 1, 1, 2] if flat else [0, 1, 2, 3]
    for x, y in cells:
        c = p.get(x, y)
        if c[3] == 0:
            continue
        k = '%02x%02x%02x' % c[:3]
        i = STICK.get(k, 2)
        p.set(x, y, r[idx[i]])
    return p


def base(kind, mat, outline=None):
    p = Pix(img=item('iron_pickaxe' if kind == 'pickaxe' else 'iron_axe'))
    p.remap(head_map(mat, outline))
    return p


GEM_PX = {'pickaxe': (10, 6), 'axe': (9, 7)}


def apply_rarity(p: Pix, kind, rarity, head, *, knot=None, handle=None, gem='diamond', band=None,
                 edge=None, pommel_gem='diamond'):
    """Fanciness ladder (see art-direction.md)."""
    r = R(head)
    K = cells_knot(kind)
    G = cells_grip(kind)
    P = cells_pommel(kind)
    if rarity == 'common':
        recolor_cells(p, K, knot or 'string')
    elif rarity == 'uncommon':
        recolor_cells(p, K, 'iron')
        recolor_cells(p, G, 'leather')
        recolor_cells(p, P, 'iron')
    elif rarity == 'rare':
        if handle:
            recolor_cells(p, cells_handle(kind), handle, flat=True)
        else:
            recolor_cells(p, G, 'leather')
        recolor_cells(p, K, 'gold')
        p.set(*GEM_PX[kind], R(gem)[2])
        recolor_cells(p, P, 'iron')
        sharpen(p, kind, head, edge)
    elif rarity == 'epic':
        recolor_cells(p, cells_handle(kind), handle or 'dark_handle')
        recolor_cells(p, K, 'gold')
        p.set(*GEM_PX[kind], R(gem)[2])
        recolor_cells(p, P, 'netherite')
        sharpen(p, kind, head, edge)
        heavy_head(p, kind, head)
        if band:
            add_band(p, kind, band)
    elif rarity == 'legendary':
        recolor_cells(p, cells_handle(kind), handle or 'dark_handle')
        recolor_cells(p, K, 'gold')
        p.set(*GEM_PX[kind], R(gem)[2])
        recolor_cells(p, P, 'gold')
        p.set(3, 13, R(pommel_gem)[2])
        sharpen(p, kind, head, edge)
        heavy_head(p, kind, head)
        if band:
            add_band(p, kind, band)
        finial(p, kind)
        filigree(p, kind)
        if kind == 'axe':
            double_bit(p, head)
    return p


def sharpen(p, kind, head, edge=None):
    r = R(edge or head)
    out = darken(R(head)[0], 0.45)
    if kind == 'pickaxe':
        p.set(5, 3, r[2]); p.set(4, 3, out)
        p.set(13, 11, r[2]); p.set(13, 12, OUT)
    else:
        p.set(7, 3, r[3]); p.set(6, 4, r[2]); p.set(6, 5, r[2])
        p.set(6, 3, out); p.set(5, 4, out); p.set(5, 5, out); p.set(6, 6, out)


def heavy_head(p, kind, head):
    r = R(head)
    out = darken(r[0], 0.45)
    if kind == 'pickaxe':
        p.put([(7, 4), (8, 4), (9, 4)], r[0])
        p.set(6, 4, r[1])
        p.put([(7, 5), (8, 5), (9, 5)], OUT)
        p.set(6, 5, out)
    else:
        p.set(7, 6, r[1]); p.set(6, 6, out); p.set(7, 7, out)
        p.set(8, 2, r[2]); p.set(7, 2, out)


def add_band(p, kind, band):
    r = R(band)
    if kind == 'pickaxe':
        p.put([(10, 3), (10, 4)], r[2]); p.put([(11, 3), (11, 4)], r[1])
    else:
        p.set(12, 6, r[2]); p.set(12, 7, r[1]); p.set(11, 7, r[1])


def finial(p, kind):
    g = R('gold')
    if kind == 'pickaxe':
        p.set(7, 1, g[1]); p.set(8, 1, g[2]); p.set(8, 0, g[3])
    else:
        p.set(9, 0, g[1]); p.set(10, 0, g[2]); p.set(11, 1, g[1])


def filigree(p, kind):
    g = R('gold')
    if kind == 'pickaxe':
        p.set(8, 3, g[2]); p.set(13, 8, g[2])
    else:
        p.set(9, 4, g[2]); p.set(11, 6, g[2])


def double_bit(p, head):
    r = R(head)
    out = darken(r[0], 0.45)
    p.set(13, 4, out); p.set(13, 5, r[2]); p.set(14, 5, out); p.set(14, 6, out)
    p.set(13, 6, r[1]); p.set(13, 7, r[1]); p.set(14, 7, out); p.set(13, 8, out)


def honeycomb(p, kind):
    """Waxed tell: two honey pixels on the head."""
    h = R('honey')
    if kind == 'pickaxe':
        p.set(9, 3, h[3]); p.set(13, 6, h[2])
    else:
        p.set(9, 3, h[3]); p.set(11, 5, h[2])
