"""Menu backgrounds v3: one 176 x (114 + 18*rows) PNG per menu, shown through a glyph of the
rune:menus font in the menu title (ascent 13, shifted -8 px, see docs/art-direction.md "Menus").

Built on the vanilla chest frame (generic_54, stitched for the row count). Every slot that can hold an
item (used menu slots + the 36 player slots) is cut out (transparent), so the vanilla slot and the item
always show, whatever the draw order. Unused menu slots are painted over by the panels.
"""
import os
from PIL import Image, ImageDraw
from pix import hx
from palettes import R, _cache

MC = os.environ.get('MC_ASSETS', os.path.join(os.path.dirname(__file__), '..', 'node_modules', 'minecraft-assets',
                                              'minecraft-assets', 'data', '26.1'))
W = 176
_cache['parchment'] = [hx(h) for h in ('9c8657', 'c2ad7c', 'dfcfa3', 'f2e8c9')]


def height(rows):
    return 114 + 18 * rows


def slot_xy(i):
    """Top-left of the 18x18 frame of menu slot i (vanilla: item at +1,+1)."""
    return 7 + 18 * (i % 9), 17 + 18 * (i // 9)


def player_slots(rows):
    y0 = 31 + 18 * rows
    return [(7 + 18 * c, y0 + 18 * r) for r in range(3) for c in range(9)] + \
           [(7 + 18 * c, y0 + 58) for c in range(9)]


def vanilla(rows):
    g = Image.open(os.path.join(MC, 'gui', 'container', 'generic_54.png')).convert('RGBA')
    top = 17 + 18 * rows
    im = Image.new('RGBA', (W, height(rows)), (0, 0, 0, 0))
    im.paste(g.crop((0, 0, W, top)), (0, 0))
    im.paste(g.crop((0, 126, W, 222)), (0, top))
    return im


def rgba(c):
    return tuple(c) if len(c) == 4 else tuple(c) + (255,)


class Board:
    def __init__(self, rows):
        self.rows = rows
        self.im = vanilla(rows)
        self.d = ImageDraw.Draw(self.im)
        self.holes = []

    # panels ---------------------------------------------------------------
    def panel(self, c0, r0, c1, r1, mat, pattern=None):
        """A bevelled board covering menu slots (c0,r0)..(c1,r1) inclusive."""
        d, m, l, h = (rgba(c) for c in R(mat))
        x0, y0 = 7 + 18 * c0, 17 + 18 * r0
        x1, y1 = 7 + 18 * (c1 + 1) - 1, 17 + 18 * (r1 + 1) - 1
        self.d.rectangle((x0, y0, x1, y1), fill=m)
        self.d.line((x0, y0, x1, y0), fill=h)
        self.d.line((x0, y0, x0, y1), fill=h)
        self.d.line((x0, y1, x1, y1), fill=d)
        self.d.line((x1, y0, x1, y1), fill=d)
        if pattern:
            pattern(self, x0 + 1, y0 + 1, x1 - 1, y1 - 1, (d, m, l, h))
        return self

    def hole(self, i, ring=None):
        """Cut menu slot i out (transparent); optional 1px ring around it (gold / copper / iron...)."""
        self.holes.append((*slot_xy(i), ring))
        return self

    def mark(self, x, y, c):
        """A small engraved rune mark (5x5 diamond with a cross stroke)."""
        for px, py in [(x + 2, y), (x + 1, y + 1), (x + 3, y + 1), (x, y + 2), (x + 4, y + 2), (x + 2, y + 2),
                       (x + 1, y + 3), (x + 3, y + 3), (x + 2, y + 4)]:
            self.d.point((px, py), fill=c)

    def done(self):
        for x, y, _ in self.holes + [(x, y, None) for x, y in player_slots(self.rows)]:
            self.d.rectangle((x, y, x + 17, y + 17), fill=(0, 0, 0, 0))
        # rings sit on the slot's own 1px edge (where the vanilla bevel is), never on the item area,
        # so two framed slots side by side never overlap
        for x, y, ring in self.holes:
            if ring:
                d, m, l, h = (rgba(c) for c in R(ring))
                self.d.rectangle((x, y, x + 17, y + 17), outline=m)
                self.d.line((x, y, x + 17, y), fill=l)
                self.d.line((x, y, x, y + 17), fill=l)
                for cx, cy in [(x, y), (x + 17, y), (x, y + 17), (x + 17, y + 17)]:
                    self.d.point((cx, cy), fill=h)
        return self.im


# patterns -------------------------------------------------------------------
def planks(b, x0, y0, x1, y1, ramp):
    d, m, l, h = ramp
    y = y0 + 3
    row = 0
    while y < y1:
        b.d.line((x0, y, x1, y), fill=d)
        off = 9 if row % 2 else 0
        for x in range(x0 + off, x1, 18):
            b.d.point((x, y - 2), fill=d)
            b.d.point((x, y - 1), fill=d)
        y += 4
        row += 1


def parchment(b, x0, y0, x1, y1, ramp):
    d, m, l, h = ramp
    for i, (x, y) in enumerate([(x0 + 5 + (k * 23) % (x1 - x0 - 8), y0 + 3 + (k * 13) % (y1 - y0 - 4)) for k in range(18)]):
        b.d.point((x, y), fill=l if i % 2 else d)


def crystals(b, x0, y0, x1, y1, ramp):
    d, m, l, h = ramp
    for k in range(14):
        x = x0 + 4 + (k * 29) % (x1 - x0 - 8)
        y = y0 + 3 + (k * 17) % (y1 - y0 - 6)
        b.d.line((x, y, x + 2, y + 2), fill=l)
        b.d.point((x + 1, y + 3), fill=d)


def stone(b, x0, y0, x1, y1, ramp):
    d, m, l, h = ramp
    for k in range(20):
        x = x0 + 2 + (k * 31) % (x1 - x0 - 4)
        y = y0 + 2 + (k * 11) % (y1 - y0 - 3)
        b.d.point((x, y), fill=d if k % 3 else l)


# the menus ------------------------------------------------------------------
def codex_main():
    """3 rows. Jobs (wood) / Quests (parchment) / Runes (amethyst), each a 3x3 board; the icon sits in the
    gold-framed centre slot (10 / 13 / 16), the 8 slots around it are invisible buttons doing the same."""
    b = Board(3)
    for k, (mat, pat) in enumerate([('oak', planks), ('parchment', parchment), ('amethyst', crystals)]):
        b.panel(3 * k, 0, 3 * k + 2, 2, mat, pat)
        dk = rgba(R(mat)[0])
        x0, y0 = 7 + 54 * k, 17
        for mx, my in [(x0 + 3, y0 + 3), (x0 + 46, y0 + 3), (x0 + 3, y0 + 46), (x0 + 46, y0 + 46)]:
            b.mark(mx, my, dk)
        b.hole(10 + 3 * k, 'gold')
    return b.done()


def quests():
    """3 rows. Token (slot 4, copper), the 3 Quests (11 / 13 / 15, gold), Back (22, deepslate)."""
    b = Board(3)
    b.panel(0, 0, 8, 2, 'parchment', parchment)
    dk = rgba(R('parchment')[0])
    for i in (11, 13, 15):
        x, y = slot_xy(i)
        b.d.line((x - 3, y + 21, x + 20, y + 21), fill=dk)
    b.mark(12, 22, dk); b.mark(159, 22, dk); b.mark(12, 58, dk); b.mark(159, 58, dk)
    b.hole(4, 'copper')
    for i in (11, 13, 15):
        b.hole(i, 'gold')
    b.hole(22, 'deepslate')
    return b.done()


def jobs():
    """4 rows (paged). 27 Job / Account slots on a wood board, Account (slot 0) framed gold;
    bottom band: previous (30), back (31), next (32)."""
    b = Board(4)
    b.panel(0, 0, 8, 2, 'oak', planks)
    b.panel(0, 3, 8, 3, 'dark_oak', planks)
    b.hole(0, 'gold')
    for i in range(1, 27):
        b.hole(i)
    for i in (30, 32):
        b.hole(i, 'oak_log')
    b.hole(31, 'deepslate')
    return b.done()


def runes():
    """6 rows. Player Rune slots 0-7 in a deepslate band, Capacity (8) framed gold; engraved divider;
    the Runes in the inventory (18-44) on amethyst; Back (49)."""
    b = Board(6)
    b.panel(0, 0, 8, 0, 'deepslate', stone)
    for i in range(8):
        b.hole(i, 'amethyst')
    b.hole(8, 'gold')
    b.panel(0, 1, 8, 1, 'stone', None)
    sd, sm, sl, sh = (rgba(c) for c in R('stone'))
    y = 17 + 18 + 8
    b.d.line((10, y, 165, y), fill=sd)
    b.d.line((10, y + 1, 165, y + 1), fill=sh)
    for x in (30, 84, 138):
        b.mark(x, y - 2, rgba(R('amethyst')[1]))
    b.panel(0, 2, 8, 4, 'amethyst', crystals)
    for i in range(18, 45):
        b.hole(i)
    b.panel(0, 5, 8, 5, 'deepslate', stone)
    b.hole(49, 'deepslate')
    return b.done()


MENUS = {'codex_main': (codex_main, 3), 'jobs': (jobs, 4), 'quests': (quests, 3), 'runes': (runes, 6)}
FIRST_CHAR = 0xE100  #  codex_main,  jobs,  quests,  runes
SHIFT_LEFT, SHIFT_BACK = '', ''  # -8 before the background, -169 after it (177 advance - 8)


def font():
    providers = [{"type": "space", "advances": {SHIFT_LEFT: -8, SHIFT_BACK: -169}}]
    for k, (name, (_, rows)) in enumerate(MENUS.items()):
        providers.append({"type": "bitmap", "file": "rune:gui/menu/%s.png" % name, "height": height(rows),
                          "ascent": 13, "chars": [chr(FIRST_CHAR + k)]})
    return {"providers": providers}
