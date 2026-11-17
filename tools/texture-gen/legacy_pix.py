"""Tiny pixel-art helper: role grids + palettes -> 16x16 RGBA PNGs."""
from PIL import Image


def hx(s):
    s = s.lstrip('#')
    return (int(s[0:2], 16), int(s[2:4], 16), int(s[4:6], 16), 255)


def render(grid, pal, over=None):
    """grid: 16 strings of 16 chars. pal: role char -> hex. over: list of (x, y, hex)."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(grid):
        assert len(row) == 16, (y, row)
        for x, ch in enumerate(row):
            if ch == '.':
                continue
            im.putpixel((x, y), hx(pal[ch]))
    for x, y, c in over or []:
        im.putpixel((x, y), (0, 0, 0, 0) if c is None else hx(c))
    return im


def mask(grid, chars):
    return [(x, y) for y, r in enumerate(grid) for x, ch in enumerate(r) if ch in chars]


# vanilla stick colours (same on every vanilla tool)
STICK = dict(e='#493615', f='#684e1e', h='#896727', i='#281e0b')
