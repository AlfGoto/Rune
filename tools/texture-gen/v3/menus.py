"""Menu backgrounds v3, free-form (docs/art-direction.md, "Menus").

Every menu is a 6-row chest. Its background is one 176x222 glyph of the rune:menus font in the menu title,
drawn freely: buttons are drawn here and covered by invisible items in the plugin. Slots that show a real
item (a Rune, a head, an icon, an arrow) are cut out (transparent), so the vanilla slot and the item show
whatever Minecraft draws first; decoration only goes around them. The 36 player slots are cut out too.

Live parts are drawn on top by the plugin (`MenuCanvas`): text and bars with the menu fonts (menufont.py),
and the images below (Quest notes in their Tier colour, Capacity orb fill), placed with space glyphs.
The layout constants here must match the Java menus (CodexMenus, RunesMenu, QuestsMenu, JobsMenu).
"""
import os
from PIL import Image, ImageDraw
from pix import hx
from palettes import R, _cache

MC = os.environ.get('MC_ASSETS', os.path.join(os.path.dirname(__file__), '..', 'node_modules', 'minecraft-assets',
                                              'minecraft-assets', 'data', '26.1'))
W, H, ROWS = 176, 222, 6
_cache['parchment'] = [hx(h) for h in ('9c8657', 'c2ad7c', 'dfcfa3', 'f2e8c9')]
_cache['codex'] = [hx(h) for h in ('1b1630', '2b2450', '3d3470', '524893')]
_cache['leather'] = [hx(h) for h in ('5a3418', '7d4a24', '9c6233', 'b97d48')]


def rgba(c):
    return tuple(c) if len(c) == 4 else tuple(c) + (255,)


def ramp(m):
    return [rgba(c) for c in R(m)]


def slot_xy(i):
    """Top-left of the 18x18 frame of menu slot i (the item sits at +1,+1)."""
    return 7 + 18 * (i % 9), 17 + 18 * (i // 9)


def player_slots():
    y0 = 31 + 18 * ROWS
    return [(7 + 18 * c, y0 + 18 * r) for r in range(3) for c in range(9)] + [(7 + 18 * c, y0 + 58) for c in range(9)]


def vanilla():
    g = Image.open(os.path.join(MC, 'gui', 'container', 'generic_54.png')).convert('RGBA')
    return g.crop((0, 0, W, H))


def item_png(name):
    return Image.open(os.path.join(MC, 'items', name + '.png')).convert('RGBA')


class Board:
    def __init__(self):
        self.im = vanilla()
        self.d = ImageDraw.Draw(self.im)
        self.holes = []

    def hole(self, i, ring=None):
        """Menu slot i shows a real item: cut out, with an optional 1px ring just outside it."""
        self.holes.append((*slot_xy(i), ring))
        return self

    def icon(self, name, x, y):
        self.im.alpha_composite(item_png(name), (x, y))

    def done(self):
        for x, y, ring in self.holes:
            if ring:
                d_, m, l, h = ramp(ring)
                self.d.rectangle((x - 1, y - 1, x + 18, y + 18), outline=m)
                self.d.line((x - 1, y - 1, x + 18, y - 1), fill=l)
                for cx, cy in [(x - 1, y - 1), (x + 18, y - 1), (x - 1, y + 18), (x + 18, y + 18)]:
                    self.d.point((cx, cy), fill=h)
        for x, y, _ in self.holes:
            self.d.rectangle((x, y, x + 17, y + 17), fill=(0, 0, 0, 0))
        for x, y in player_slots():
            self.d.rectangle((x, y, x + 17, y + 17), fill=(0, 0, 0, 0))
        return self.im


ARROWS = {
    'left': ['................', '................', '................', '......#.........', '.....##.........',
             '....###.........', '...##########...', '..###########...', '..###########...', '...##########...',
             '....###.........', '.....##.........', '......#.........', '................', '................',
             '................'],
}
ARROWS['right'] = [r[::-1] for r in ARROWS['left']]
ARROWS['back'] = ['................', '................', '.....#..........', '....##..........', '...#########....',
                  '..##########....', '...#########....', '....##......##..', '.....#......##..', '............##..',
                  '............##..', '......#######...', '......######....', '................', '................',
                  '................']


def arrow(direction, colour=(240, 240, 240, 255), shade=(150, 150, 150, 255)):
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    rows = ARROWS[direction]
    for y, r in enumerate(rows):
        for x, ch in enumerate(r):
            if ch == '#':
                im.putpixel((x, y), colour if y < 8 else shade)
    return im


def button(b, i, icon, mat):
    """A drawn 18x18 button at slot i (bevel in mat, icon inside); the plugin puts an invisible item on it."""
    d = b.d
    dk, m, l, h = ramp(mat)
    x, y = slot_xy(i)
    d.rectangle((x, y, x + 17, y + 17), fill=m, outline=dk)
    d.line((x + 1, y + 1, x + 16, y + 1), fill=h); d.line((x + 1, y + 1, x + 1, y + 16), fill=h)
    b.im.alpha_composite(icon, (x + 1, y + 1))


def track(d, x, y, w, back):
    """The empty groove of a progress bar of fill width w: the 3 px fill goes at (x+1, y+1)."""
    d.rectangle((x, y, x + w + 1, y + 4), fill=(30, 20, 10, 255))
    d.rectangle((x + 1, y + 1, x + w, y + 3), fill=back)


# ================================================================= Rune Codex: an open book
# Live (CodexMenus): title y 11, name y 58, level y 69, XP bar fill (19,82) w 59, XP small y 89,
# "Capacity" y 99, Capacity bar fill (19,111) w 59, Capacity small y 117;
# chapter label (x0+25, y0+8) width 44, value small (x0+25, y0+21)
CODEX_PORTRAIT = 11
CODEX_CHAPTERS = [(5, 'experience_bottle', 'oak'), (23, 'writable_book', 'parchment'), (41, 'amethyst_shard', 'amethyst')]


def codex():
    b = Board(); d = b.d
    cov, par, gold = ramp('codex'), ramp('parchment'), ramp('gold')
    d.rectangle((4, 3, 171, 127), fill=cov[1], outline=cov[0])
    d.line((5, 4, 170, 4), fill=cov[3])
    for cx, cy in [(5, 4), (166, 4), (5, 122), (166, 122)]:
        d.rectangle((cx, cy, cx + 4, cy + 4), fill=gold[1]); d.point((cx + 1, cy + 1), fill=gold[3])
    for x0, x1 in ((9, 86), (90, 166)):
        d.rectangle((x0, 7, x1, 123), fill=par[2])
        d.line((x0, 123, x1, 123), fill=par[0]); d.line((x0, 122, x1, 122), fill=par[1])
    d.rectangle((86, 7, 90, 123), fill=par[0]); d.line((88, 7, 88, 123), fill=cov[0])
    d.line((84, 8, 84, 121), fill=par[1]); d.line((92, 8, 92, 121), fill=par[1])
    d.line((22, 21, 74, 21), fill=par[0])                      # under the title
    x, y = slot_xy(CODEX_PORTRAIT)                              # portrait frame (gold), head = real item
    d.rectangle((x - 3, y - 3, x + 20, y + 20), fill=gold[1], outline=gold[0])
    d.line((x - 2, y - 2, x + 19, y - 2), fill=gold[3])
    track(d, 18, 81, 59, par[0])                                # Account XP
    track(d, 18, 110, 59, par[0])                               # Capacity
    for first, icon, mat in CODEX_CHAPTERS:                     # chapter cards: 4 cols x 2 rows each
        r_ = ramp(mat)
        x0, y0 = slot_xy(first); x1, y1 = x0 + 71, y0 + 35
        d.rectangle((x0 + 1, y0 + 2, x1 - 1, y1 - 2), fill=r_[1], outline=r_[0])
        d.line((x0 + 2, y0 + 3, x1 - 2, y0 + 3), fill=r_[3])
        d.polygon([(x1 - 1, y0 + 2), (x1 + 2, y0 + 2), (x1 - 1, y0 + 8)], fill=gold[1])
        d.rectangle((x0 + 3, y0 + 8, x0 + 22, y0 + 27), fill=r_[0])
        b.icon(icon, x0 + 5, y0 + 10)
    b.hole(CODEX_PORTRAIT)
    return b.done()


# ================================================================= Runes: the altar
RUNES_SOCKETS = [3, 5, 15, 24, 32, 30, 20, 11]     # the 8 Player Rune slots, clockwise from the top left
RUNES_ORB = [12, 13, 14, 21, 22, 23]                 # invisible Capacity button over the orb
RUNES_BACK, RUNES_PREV, RUNES_NEXT = 27, 26, 35   # Back is drawn; Previous / Next are live arrows
RUNES_SATCHEL = list(range(36, 54))
ORB_CX, ORB_CY, ORB_R = 88, 52, 15                   # live: orb fill image (top 37), number y 48 centred


def runes():
    b = Board(); d = b.d
    ds, am, st, gold, le = ramp('deepslate'), ramp('amethyst'), ramp('stone'), ramp('gold'), ramp('leather')
    d.rectangle((4, 3, 171, 89), fill=(28, 26, 36, 255))
    for rr, c in ((44, ds[0]), (42, ds[1]), (30, ds[0]), (28, (46, 42, 58, 255))):
        d.ellipse((ORB_CX - rr, ORB_CY - rr * 0.82, ORB_CX + rr, ORB_CY + rr * 0.82), fill=c)
    for px in (6, 158):
        d.rectangle((px, 14, px + 11, 86), fill=st[1]); d.line((px, 14, px, 86), fill=st[3]); d.line((px + 11, 14, px + 11, 86), fill=st[0])
        for yy in range(20, 86, 9):
            d.line((px + 1, yy, px + 10, yy), fill=st[0])
        d.rectangle((px + 3, 6, px + 8, 12), fill=(255, 200, 80, 255)); d.point((px + 5, 7), fill=(255, 255, 220, 255))
    for i in RUNES_SOCKETS:
        x, y = slot_xy(i)
        d.ellipse((x - 4, y - 4, x + 21, y + 21), fill=am[0])
        d.ellipse((x - 3, y - 3, x + 20, y + 20), fill=ds[0])
        b.hole(i, 'amethyst')
    d.ellipse((ORB_CX - ORB_R - 1, ORB_CY - ORB_R - 1, ORB_CX + ORB_R + 1, ORB_CY + ORB_R + 1), fill=gold[0])
    d.ellipse((ORB_CX - ORB_R, ORB_CY - ORB_R, ORB_CX + ORB_R, ORB_CY + ORB_R), fill=(24, 18, 36, 255))
    d.rectangle((4, 88, 171, 127), fill=le[1]); d.line((4, 88, 171, 88), fill=le[3]); d.line((4, 127, 171, 127), fill=le[0])
    for xx in range(6, 170, 4):
        d.point((xx, 90), fill=le[0]); d.point((xx, 125), fill=le[0])
    for i in RUNES_SATCHEL:
        b.hole(i, 'leather')
    button(b, RUNES_BACK, arrow('back'), 'stone')
    return b.done()                                       # Previous / Next: live arrows, only when they lead somewhere


def orb_fill(step):
    """Capacity orb filled to step/10 (step 0..10), drawn over the orb's box."""
    am = ramp('amethyst')
    size = 2 * ORB_R + 1
    im = Image.new('RGBA', (size, size), (0, 0, 0, 0)); d = ImageDraw.Draw(im)
    r = ORB_R - 1
    top = int(ORB_R + r - 2 * r * step / 10)
    if step > 0:
        for yy in range(max(top, ORB_R - r), ORB_R + r + 1):
            half = int(max(0, r * r - (yy - ORB_R) ** 2) ** 0.5)
            d.line((ORB_R - half, yy, ORB_R + half, yy), fill=am[3] if yy <= top + 1 else am[2] if yy < ORB_R + r - 3 else am[1])
    # a 1px transparent-safe marker so every step has the same width (the advance must not change)
    im.putpixel((0, ORB_R), (24, 18, 36, 255)); im.putpixel((size - 1, ORB_R), (24, 18, 36, 255))
    return im


# ================================================================= Quests: the notice board
# Notes are live images in the Tier colour. Live text per note: lines y 56 / 66 (centre x0+25, width 46),
# bar fill (x0+5, 79) w 41, numbers small y 85, Tier small y 95. Token count small at (63, 113).
QUEST_ICONS = [10, 13, 16]
QUEST_REROLL = [46, 49, 52]                         # live: shown only when a Reroll Token can be spent
QUEST_TOKEN, QUEST_BACK = 47, 53                    # token count: small text at (63, 113)
NOTE_X = [9 + 54 * k for k in range(3)]
NOTE_Y, NOTE_W, NOTE_H = 13, 51, 92
TIER_COLOURS = {'common': 'ffffff', 'uncommon': '55ff55', 'rare': '55ffff', 'epic': 'ff55ff', 'legendary': 'ffaa00'}


def quests():
    b = Board(); d = b.d
    oak, dk = ramp('oak'), ramp('dark_oak')
    d.rectangle((4, 3, 171, 127), fill=oak[1])
    for yy in range(7, 127, 6):
        d.line((4, yy, 171, yy), fill=oak[0])
    d.rectangle((4, 3, 171, 127), outline=dk[0])
    d.rectangle((4, 106, 171, 127), fill=dk[1]); d.line((4, 106, 171, 106), fill=dk[3])
    for i in QUEST_ICONS:
        b.hole(i)
    tx, ty = slot_xy(QUEST_TOKEN)                         # Reroll Tokens: information, not a button
    b.icon('paper', tx + 1, ty + 1)
    button(b, QUEST_BACK, arrow('back'), 'stone')
    return b.done()


def note(tier):
    """A Quest card: a frame in the Tier colour (bevelled, gem corners), a header band, a light inner panel,
    a ring around the icon slot (cut out) and the bar groove."""
    par = ramp('parchment')
    c = rgba(hx(TIER_COLOURS[tier]))
    light = tuple(min(255, int(v + (255 - v) * 0.55)) for v in c[:3]) + (255,)
    mid = tuple(int(v * 0.8) for v in c[:3]) + (255,)
    dark = tuple(int(v * 0.45) for v in c[:3]) + (255,)
    w, h = NOTE_W + 1, NOTE_H + 1
    im = Image.new('RGBA', (w, h), (0, 0, 0, 0)); d = ImageDraw.Draw(im)
    d.rectangle((2, 2, w - 1, h - 1), fill=(20, 12, 6, 120))                     # soft shadow
    d.rectangle((1, 0, w - 3, h - 2), fill=dark); d.rectangle((0, 1, w - 2, h - 3), fill=dark)  # rounded outline
    d.rectangle((1, 1, w - 3, h - 3), fill=mid)                                   # frame
    d.line((2, 1, w - 4, 1), fill=light); d.line((1, 2, 1, h - 4), fill=light)   # frame bevel
    d.line((2, h - 3, w - 3, h - 3), fill=dark); d.line((w - 3, 2, w - 3, h - 3), fill=dark)
    d.rectangle((3, 3, w - 5, 10), fill=c)                                        # header band
    d.line((3, 3, w - 5, 3), fill=light)
    d.line((3, 10, w - 5, 10), fill=mid)
    d.rectangle((3, 11, w - 5, h - 5), fill=par[3])                               # inner panel
    d.rectangle((3, 11, w - 5, h - 5), outline=par[1])
    d.line((4, h - 6, w - 6, h - 6), fill=par[2])
    for gx, gy in ((0, 0), (w - 6, 0), (0, h - 7), (w - 6, h - 7)):              # gem corners
        d.rectangle((gx + 1, gy + 1, gx + 4, gy + 4), fill=dark)
        d.rectangle((gx + 2, gy + 2, gx + 3, gy + 3), fill=light)
    for cx in (w // 2 - 9, w // 2 + 7):                                           # band studs
        d.point((cx, 6), fill=light); d.point((cx + 1, 7), fill=dark)
    ix, iy = 15, 21                                                               # ring just outside the icon slot
    d.rectangle((ix - 1, iy - 1, ix + 20, iy + 20), outline=par[1])
    d.rectangle((ix, iy, ix + 19, iy + 19), outline=c)
    d.rectangle((ix + 1, iy + 1, ix + 18, iy + 18), fill=(0, 0, 0, 0))
    gx, gy = 4, 78 - NOTE_Y                                                       # bar groove
    d.rectangle((gx, gy, gx + 42, gy + 4), fill=(30, 20, 10, 255))
    d.rectangle((gx + 1, gy + 1, gx + 41, gy + 3), fill=par[0])
    d.line((8, 92 - NOTE_Y, w - 10, 92 - NOTE_Y), fill=par[1])                    # divider above the Tier
    return im


# ================================================================= Jobs: job cards
# Live: title y 6 centred (88); banner text (27, 21), bar fill (119, 23) w 46; cards are live images (active or
# locked); per card (x0, y0 = card's top-left slot frame): level (x0+21, y0+5), bar fill (x0+21, y0+16) w 31,
# name (x0+3, y0+23) width 49; a locked card: "Soon" small (x0+21, y0+7).
JOBS_BANNER_ICON = 0
JOBS_CARDS = [(1 + (k // 3) * 2) * 9 + (k % 3) * 3 for k in range(6)]
JOBS_PREV, JOBS_BACK, JOBS_NEXT = 48, 49, 50       # Back is drawn; Previous / Next are live arrows


def jobs():
    b = Board(); d = b.d
    dk, oak = ramp('dark_oak'), ramp('oak')
    d.rectangle((4, 3, 171, 127), fill=dk[1], outline=dk[0])
    d.rectangle((7, 17, 168, 34), fill=(40, 70, 120, 255), outline=(20, 35, 70, 255))
    d.line((8, 18, 167, 18), fill=(90, 130, 190, 255))
    track(d, 118, 22, 46, (20, 35, 70, 255))
    for first in JOBS_CARDS:                                # empty card spots: a faint recess
        x0, y0 = slot_xy(first)
        d.rectangle((x0 + 1, y0 + 1, x0 + 52, y0 + 34), outline=dk[0])
        b.hole(first)
    button(b, JOBS_BANNER_ICON, item_png('nether_star'), 'gold')
    button(b, JOBS_BACK, arrow('back'), 'stone')
    return b.done()                                         # Previous / Next: live arrows, only when they lead somewhere


def job_card(locked):
    """A Job card (3 x 2 slots); its top-left slot is cut out for the icon. Locked = grey stone, no XP groove."""
    r_ = ramp('stone') if locked else ramp('oak')
    im = Image.new('RGBA', (54, 36), (0, 0, 0, 0)); d = ImageDraw.Draw(im)
    d.rectangle((1, 1, 52, 34), fill=r_[1], outline=r_[0])
    d.line((2, 2, 51, 2), fill=r_[3]); d.line((2, 2, 2, 33), fill=r_[3])
    if locked:
        lk = (80, 80, 80, 255)                                # small padlock, bottom right
        d.rectangle((44, 8, 49, 12), fill=lk); d.rectangle((45, 5, 48, 7), outline=lk)
        d.point((46, 10), fill=r_[3])
    else:
        d.rectangle((20, 15, 20 + 32, 15 + 4), fill=(30, 20, 10, 255))
        d.rectangle((21, 16, 20 + 31, 15 + 3), fill=r_[0])
    d.rectangle((0, 0, 18, 18), outline=r_[0])
    d.rectangle((1, 1, 17, 17), fill=(0, 0, 0, 0))          # the icon slot (cut out in the background too)
    return im


LOCKED_ICONS = {'excavation': 'iron_shovel', 'farming': 'iron_hoe', 'ranching': 'lead', 'fishing': 'fishing_rod',
                'combat': 'iron_sword', 'archery': 'bow', 'smithing': 'iron_ingot', 'alchemy': 'brewing_stand',
                'adventuring': 'map', 'runesmithing': 'amethyst_shard'}


def locked_icon(job):
    """Item texture of a Job that is not playable yet: its tool in grey."""
    it = item_png(LOCKED_ICONS[job])
    out = Image.new('RGBA', it.size, (0, 0, 0, 0))
    for y in range(it.height):
        for x in range(it.width):
            r, g, b_, a = it.getpixel((x, y))
            if a:
                v = int((0.3 * r + 0.59 * g + 0.11 * b_) * 0.6 + 40)
                out.putpixel((x, y), (v, v, v, a))
    return out


def reroll_icon():
    """Icon of the reroll buttons: a thick copper circular arrow."""
    import math
    co = ramp('copper')
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            r = math.hypot(x - 7.5, y - 7.5)
            ang = math.degrees(math.atan2(y - 7.5, x - 7.5)) % 360
            if 3.6 <= r <= 5.8 and not (300 <= ang <= 350):
                im.putpixel((x, y), co[3] if y < 6 else co[2] if y < 11 else co[1])
    for x, y in [(12, 1), (12, 2), (13, 2), (12, 3), (13, 3), (14, 3), (12, 4), (13, 4), (12, 5)]:
        im.putpixel((x, y), co[3])
    return im


# ================================================================= font
BACKGROUNDS = ['codex', 'runes', 'quests', 'jobs']
BACKGROUND_DRAW = {'codex': codex, 'runes': runes, 'quests': quests, 'jobs': jobs}
BG_FIRST, NOTE_FIRST, ORB_FIRST, NAV_FIRST, CARD_FIRST, REROLL_CHAR = 0xE100, 0xE110, 0xE120, 0xE130, 0xE140, 0xE150
NAV = [('runes_prev', RUNES_PREV, 'left'), ('runes_next', RUNES_NEXT, 'right'),
       ('jobs_prev', JOBS_PREV, 'left'), ('jobs_next', JOBS_NEXT, 'right')]


def nav_button(direction):
    """A live page arrow: a stone button (18x18) drawn over its slot."""
    b = Board.__new__(Board)
    b.im = Image.new('RGBA', (18, 18), (0, 0, 0, 0)); b.d = ImageDraw.Draw(b.im)
    dk, m, l, h = ramp('stone')
    b.d.rectangle((0, 0, 17, 17), fill=m, outline=dk)
    b.d.line((1, 1, 16, 1), fill=h); b.d.line((1, 1, 1, 16), fill=h)
    b.im.alpha_composite(arrow(direction), (1, 1))
    return b.im


def empty_item():
    """Item texture shown in an empty Rune socket or Job card: a dark recess (hides the grey vanilla slot)."""
    im = Image.new('RGBA', (16, 16), (24, 20, 32, 255)); d = ImageDraw.Draw(im)
    d.line((0, 0, 15, 0), fill=(12, 10, 16, 255)); d.line((0, 0, 0, 15), fill=(12, 10, 16, 255))
    d.line((1, 15, 15, 15), fill=(48, 42, 60, 255)); d.line((15, 1, 15, 15), fill=(48, 42, 60, 255))
    return im
TIERS = list(TIER_COLOURS)


def shift_advances():
    """Space glyphs: \\uf801.. = -1, -2, -4 .. -256 px; \\uf821.. = +1, +2 .. +256 px."""
    adv = {}
    for k in range(9):
        adv[chr(0xF801 + k)] = -(1 << k)
        adv[chr(0xF821 + k)] = 1 << k
    return adv


def images():
    """(name, image, top y, char) of every live image."""
    out = []
    for k, t in enumerate(TIERS):
        out.append(('note_' + t, note(t), NOTE_Y, chr(NOTE_FIRST + k)))
    for s in range(11):
        out.append(('orb_%d' % s, orb_fill(s), ORB_CY - ORB_R, chr(ORB_FIRST + s)))
    for k, (name, slot, direction) in enumerate(NAV):
        out.append(('nav_' + name, nav_button(direction), slot_xy(slot)[1], chr(NAV_FIRST + k)))
    k = 0
    for row_name, first in (('top', JOBS_CARDS[0]), ('bottom', JOBS_CARDS[3])):
        for locked in (False, True):
            out.append(('job_card_%s%s' % (row_name, '_locked' if locked else ''), job_card(locked),
                        slot_xy(first)[1], chr(CARD_FIRST + k)))
            k += 1
    out.append(('reroll', reroll_button(), slot_xy(QUEST_REROLL[0])[1], chr(REROLL_CHAR)))
    return out


def reroll_button():
    """The live reroll button (18x18), drawn under a Quest card when a Reroll Token can be spent."""
    b = Board.__new__(Board)
    b.im = Image.new('RGBA', (18, 18), (0, 0, 0, 0)); b.d = ImageDraw.Draw(b.im)
    dk, m, l, h = ramp('copper')
    b.d.rectangle((0, 0, 17, 17), fill=m, outline=dk)
    b.d.line((1, 1, 16, 1), fill=h); b.d.line((1, 1, 1, 16), fill=h)
    b.im.alpha_composite(reroll_icon(), (1, 1))
    return b.im


def font():
    providers = [{"type": "space", "advances": shift_advances()}]
    for k, name in enumerate(BACKGROUNDS):
        providers.append({"type": "bitmap", "file": "rune:gui/menu/%s.png" % name, "height": H, "ascent": 13,
                          "chars": [chr(BG_FIRST + k)]})
    for name, im, top, ch in images():
        providers.append({"type": "bitmap", "file": "rune:gui/menu/%s.png" % name, "height": im.height,
                          "ascent": 13 - top, "chars": [ch]})
    return {"providers": providers}
