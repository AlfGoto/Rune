"""Chest-menu backgrounds (176x222, same frame as vanilla generic_54) + in-slot UI icons.

Shown in game with the usual title-font trick: a bitmap glyph of this image in the menu title,
shifted left with negative space so it covers the chest. Slots stay real chest slots.
"""
from PIL import Image, ImageDraw
from legacy_kit import Book

import os
ASSETS = os.environ.get('MC_ASSETS', os.path.join(os.path.dirname(__file__), 'mc-assets'))
V = os.path.join(ASSETS, 'gui', 'container', 'generic_54.png')
BG, LIGHT, SHADOW, DARK, SLOT = (198, 198, 198, 255), (255, 255, 255, 255), (85, 85, 85, 255), (55, 55, 55, 255), (139, 139, 139, 255)


def hx(s, a=255):
    s = s.lstrip('#')
    return (int(s[0:2], 16), int(s[2:4], 16), int(s[4:6], 16), a)


def sxy(col, row):
    return 7 + 18 * col, 17 + 18 * row


def base():
    im = Image.open(V).convert('RGBA').crop((0, 0, 176, 222))
    d = ImageDraw.Draw(im)
    d.rectangle((7, 17, 168, 124), fill=BG)          # wipe the 6x9 chest grid, keep frame + player inventory
    return im, d


def inset(d, x0, y0, x1, y1, fill=SLOT):
    """Vanilla sunken box: dark top-left, white bottom-right."""
    d.rectangle((x0, y0, x1, y1), fill=fill)
    d.line((x0, y0, x1 - 1, y0), fill=DARK); d.line((x0, y0, x0, y1 - 1), fill=DARK)
    d.line((x0 + 1, y1, x1, y1), fill=LIGHT); d.line((x1, y0 + 1, x1, y1), fill=LIGHT)


def raised(d, x0, y0, x1, y1, fill=BG):
    d.rectangle((x0, y0, x1, y1), fill=fill)
    d.line((x0, y0, x1 - 1, y0), fill=LIGHT); d.line((x0, y0, x0, y1 - 1), fill=LIGHT)
    d.line((x0 + 1, y1, x1, y1), fill=SHADOW); d.line((x1, y0 + 1, x1, y1), fill=SHADOW)


def slot(d, col, row, fill=SLOT):
    x, y = sxy(col, row)
    inset(d, x, y, x + 17, y + 17, fill)


def gold_frame(d, col, row):
    """Gold corner brackets around a slot (featured / Active slot)."""
    x, y = sxy(col, row)
    g, G, o = hx('#fdf55f'), hx('#e9b115'), hx('#752802')
    for (cx, cy, sx, sy) in [(x - 1, y - 1, 1, 1), (x + 18, y - 1, -1, 1), (x - 1, y + 18, 1, -1), (x + 18, y + 18, -1, -1)]:
        d.point((cx, cy), fill=o)
        for k in range(1, 4):
            d.point((cx + sx * k, cy), fill=G if k < 3 else o)
            d.point((cx, cy + sy * k), fill=G if k < 3 else o)
        d.point((cx + sx, cy + sy), fill=g)


def rune_glyph(d, x, y, c):
    """Tiny 5x5 carved rune mark (decoration)."""
    for px, py in [(2, 0), (2, 1), (2, 2), (2, 3), (2, 4), (1, 1), (3, 1), (0, 2), (4, 2), (1, 3), (3, 3)]:
        d.point((x + px, y + py), fill=c)


def header(d, accent):
    """Top row: an engraved strip with rune marks, accent line under it."""
    inset(d, 7, 17, 168, 34, hx('#8b8b8b'))
    d.rectangle((8, 18, 167, 33), fill=hx('#9a9a9a'))
    for i, x in enumerate(range(12, 165, 12)):
        rune_glyph(d, x, 23, hx('#6f6f6f') if i % 3 else hx(accent))
    d.line((8, 33, 167, 33), fill=hx(accent))


def panel(d, c0, r0, c1, r1, tint):
    """A raised panel behind a block of slots, its slots drawn as soft wells."""
    x0, y0 = sxy(c0, r0); x1, y1 = sxy(c1, r1)
    raised(d, x0, y0, x1 + 17, y1 + 17, hx(tint))
    t = hx(tint); deep = tuple(int(v * 0.86) for v in t[:3]) + (255,)
    d.rectangle((x0 + 3, y0 + 3, x1 + 14, y1 + 14), outline=deep)        # engraved inner line
    for cx, cy in [(x0 + 5, y0 + 5), (x1 + 8, y0 + 5), (x0 + 5, y1 + 8), (x1 + 8, y1 + 8)]:
        rune_glyph(d, cx, cy, deep)


def codex_main():
    im, d = base()
    header(d, '#8d6acc')
    slot(d, 4, 0); gold_frame(d, 4, 0)                                   # player head = Account level
    for c0, tint in [(0, '#b8946a'), (3, '#d8cfa8'), (6, '#9f86c4')]:    # Jobs (wood), Quests (parchment), Runes (amethyst)
        panel(d, c0, 1, c0 + 2, 3, tint)
        slot(d, c0 + 1, 2); gold_frame(d, c0 + 1, 2)                     # the icon slot in the middle
    for c in (1, 7):
        slot(d, c, 5)
    slot(d, 4, 5)                                                         # back / close (Menu.backSlot)
    return im


def rune_loadout():
    im, d = base()
    header(d, '#e9b115')
    panel(d, 0, 1, 2, 3, '#a8a8a8')                                      # the tool on display
    slot(d, 1, 2); gold_frame(d, 1, 2)
    # five Rune slots, carved into a deepslate band
    x0, y0 = sxy(4, 2); x1, _ = sxy(8, 2)
    inset(d, x0 - 2, y0 - 2, 168, y0 + 19, hx('#3d3d43'))
    for c in range(4, 9):
        slot(d, c, 2, hx('#515151'))
    gold_frame(d, 4, 2); gold_frame(d, 5, 2)                              # the 2 Active slots
    for c in range(4, 9):                                                # slot numbers as pips above
        x, y = sxy(c, 1)
        for k in range(c - 3):
            d.point((x + 9 - (c - 3) + 2 * k, y + 13), fill=hx('#6f6f6f'))
    # capacity trough across row 4: pip items sit in the slots, the groove joins them
    xa, ya = sxy(0, 4); xb, _ = sxy(8, 4)
    inset(d, xa, ya + 4, xb + 17, ya + 13, hx('#2a2a2a'))
    slot(d, 4, 5)
    return im


# ------------------------------------------------------------------ in-slot UI icons (16x16 items)
U = Book()
U.add('capacity_full', 'Capacity (used)', 'C', 'UI', [
    "................", "................", "................", "................",
    "ddddddddddddddddd"[:16], "aHHHHHHHHHHHHHHa", "aHLLLLLLLLLLLLMa", "aLLLLLLLLLLLLLMa",
    "aLMMMMMMMMMMMMMa", "aMMMMMMMMMMMMMDa", "dDDDDDDDDDDDDDDd", "................",
    "................", "................", "................", "................"],
    dict(d='#1a1a1a', a='#3a1f5a', H='#e8d4ff', L='#b38ef3', M='#8d6acc', D='#54398a'))
U.add('capacity_free', 'Capacity (free)', 'C', 'UI', [
    "................", "................", "................", "................",
    "dddddddddddddddd", "a..............a", "a..............a", "a..............a",
    "a..............a", "a..............a", "dddddddddddddddd", "................",
    "................", "................", "................", "................"],
    dict(d='#1a1a1a', a='#3a3a3a'))
U.add('capacity_over', 'Capacity (over)', 'C', 'UI', [
    "................", "................", "................", "................",
    "dddddddddddddddd", "aHHHHHHHHHHHHHHa", "aHLLLLLLLLLLLLMa", "aLLLLLLLLLLLLLMa",
    "aLMMMMMMMMMMMMMa", "aMMMMMMMMMMMMMDa", "dDDDDDDDDDDDDDDd", "................",
    "................", "................", "................", "................"],
    dict(d='#1a1a1a', a='#5c0700', H='#ff9a80', L='#ff5a3c', M='#e62008', D='#a41808'))
U.add('locked_slot', 'Locked slot', 'C', 'UI', [
    "................", "................", "......aaaa......", ".....a....a.....",
    ".....a....a.....", ".....a....a.....", "...aaaaaaaaaa...", "...aHHHHHHHMa...",
    "...aHMMddMMMa...", "...aHMMddMMMa...", "...aHMMMdMMMa...", "...aMMMMMMMDa...",
    "...aaaaaaaaaa...", "................", "................", "................"],
    dict(a='#2a2a2a', H='#a8a8a8', M='#7f7f7f', D='#565656', d='#1a1a1a'))
U.add('back_button', 'Back', 'C', 'UI', [
    "................", "................", "................", "......a.........",
    ".....aHa........", "....aHLa........", "...aHLLaaaaaaa..", "..aHLLLLLLLLLMa.",
    "..aDLLLLLLLLLMa.", "...aDLLaaaaaaa..", "....aDLa........", ".....aDa........",
    "......a.........", "................", "................", "................"],
    dict(a='#3a2a08', H='#fffde0', L='#fdf55f', M='#e9b115', D='#b26411'))

P = U.items
