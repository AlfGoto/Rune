"""Fonts for live text in menus (docs/art-direction.md, "Menus").

Menu text is drawn in the menu title with these bitmap fonts. A font row can only sit at one height, so
the plugin's resource pack builder makes one font per family and per height (`MenuFonts` in Java); this
script only draws the glyph sheets and writes `src/main/resources/menu_font.json`, which both the pack
builder and the text measuring code read.

Families
- normal: the vanilla 26.1 ASCII glyphs + French / Latin accents added on top (cell 8x12, cap top at row 2)
- shadow: the same with a baked 1px drop shadow (25 % grey, so the text colour tints it like vanilla shadow)
- small:  a 3x5 capitals font for numbers and long translations (cell 6x10); the plugin upper-cases text
- bars:   progress-bar pieces 1..16 px wide, 3 px tall, in 4 colours (cell 16x10); a bar = pieces side by side
"""
import json, os
from PIL import Image

MC = os.environ.get('MC_ASSETS', os.path.join(os.path.dirname(__file__), '..', 'node_modules', 'minecraft-assets',
                                              'minecraft-assets', 'data', '26.1'))

# ---------------------------------------------------------------- normal / shadow
ASCII = [chr(c) for c in range(33, 127)]
# accented letter -> (base, accent)
ACCENTS = {}
for base, marks in {'a': 'àâä', 'e': 'éèêë', 'i': 'îï', 'o': 'ôö', 'u': 'ùûü', 'y': 'ÿ', 'c': 'ç', 'n': 'ñ',
                    'A': 'ÀÂÄ', 'E': 'ÉÈÊË', 'I': 'ÎÏ', 'O': 'ÔÖ', 'U': 'ÙÛÜ', 'C': 'Ç', 'N': 'Ñ'}.items():
    for ch in marks:
        import unicodedata
        mark = unicodedata.decomposition(ch).split()[1]
        ACCENTS[ch] = (base, {'0301': 'acute', '0300': 'grave', '0302': 'circ', '0308': 'diaer', '0327': 'cedil',
                              '0303': 'tilde'}[mark])
NORMAL_CHARS = ASCII + list(ACCENTS)
CELL_W, CELL_H, CAP_TOP = 8, 12, 2   # rows 0-1: room for capital accents; ascii rows land on 2..9


def _ascii_glyph(sheet, ch):
    c = ord(ch)
    return sheet.crop(((c % 16) * 8, (c // 16) * 8, (c % 16) * 8 + 8, (c // 16) * 8 + 8))


def _width(g):
    w = 0
    for x in range(g.width):
        if any(g.getpixel((x, y))[3] for y in range(g.height)):
            w = x + 1
    return w


def _top(g):
    for y in range(g.height):
        if any(g.getpixel((x, y))[3] for x in range(g.width)):
            return y
    return 0


def _accented(sheet, ch):
    base, mark = ACCENTS[ch]
    src = _ascii_glyph(sheet, 'ı' if False else base)
    g = Image.new('RGBA', (CELL_W, CELL_H), (0, 0, 0, 0))
    body = src.copy()
    if base == 'i' and mark != 'cedil':  # drop the dot of i under the accent
        top = _top(body)
        for x in range(8):
            body.putpixel((x, top), (0, 0, 0, 0))
    g.paste(body, (0, CAP_TOP), body)
    w = _width(src)
    cx = (w - 1) // 2
    top = _top(body) + CAP_TOP
    r = top - 2
    white = (255, 255, 255, 255)
    pts = {'acute': [(cx + 1, r), (cx, r + 1)], 'grave': [(cx - 1, r), (cx, r + 1)],
           'circ': [(cx, r), (cx - 1, r + 1), (cx + 1, r + 1)], 'diaer': [(cx - 1, r + 1), (cx + 1, r + 1)],
           'tilde': [(cx - 1, r + 1), (cx, r), (cx + 1, r + 1), (cx + 2, r)],
           'cedil': [(cx, CAP_TOP + 7), (cx - 1, CAP_TOP + 8)]}[mark]
    for p in pts:
        if 0 <= p[0] < CELL_W and 0 <= p[1] < CELL_H:
            g.putpixel(p, white)
    return g


def normal_glyphs():
    sheet = Image.open(os.path.join(MC, 'font', 'ascii.png')).convert('RGBA')
    out = {}
    for ch in ASCII:
        g = Image.new('RGBA', (CELL_W, CELL_H), (0, 0, 0, 0))
        a = _ascii_glyph(sheet, ch)
        g.paste(a, (0, CAP_TOP), a)
        out[ch] = g
    for ch in ACCENTS:
        out[ch] = _accented(sheet, ch)
    return out


def with_shadow(g):
    s = Image.new('RGBA', g.size, (0, 0, 0, 0))
    for y in range(g.height):
        for x in range(g.width):
            if g.getpixel((x, y))[3] and x + 1 < g.width and y + 1 < g.height:
                s.putpixel((x + 1, y + 1), (64, 64, 64, 255))
    s.alpha_composite(g)
    return s


# ---------------------------------------------------------------- small (3x5 capitals)
SMALL = {
    'A': ['.#.', '#.#', '###', '#.#', '#.#'], 'B': ['##.', '#.#', '##.', '#.#', '##.'], 'C': ['.##', '#..', '#..', '#..', '.##'],
    'D': ['##.', '#.#', '#.#', '#.#', '##.'], 'E': ['###', '#..', '##.', '#..', '###'], 'F': ['###', '#..', '##.', '#..', '#..'],
    'G': ['.##', '#..', '#.#', '#.#', '.##'], 'H': ['#.#', '#.#', '###', '#.#', '#.#'], 'I': ['###', '.#.', '.#.', '.#.', '###'],
    'J': ['..#', '..#', '..#', '#.#', '.#.'], 'K': ['#.#', '#.#', '##.', '#.#', '#.#'], 'L': ['#..', '#..', '#..', '#..', '###'],
    'M': ['#...#', '##.##', '#.#.#', '#...#', '#...#'], 'N': ['#..#', '##.#', '#.##', '#..#', '#..#'],
    'O': ['.#.', '#.#', '#.#', '#.#', '.#.'], 'P': ['##.', '#.#', '##.', '#..', '#..'], 'Q': ['.#.', '#.#', '#.#', '#.#', '.##'],
    'R': ['##.', '#.#', '##.', '#.#', '#.#'], 'S': ['.##', '#..', '.#.', '..#', '##.'], 'T': ['###', '.#.', '.#.', '.#.', '.#.'],
    'U': ['#.#', '#.#', '#.#', '#.#', '###'], 'V': ['#.#', '#.#', '#.#', '#.#', '.#.'], 'W': ['#...#', '#...#', '#.#.#', '##.##', '#...#'],
    'X': ['#.#', '#.#', '.#.', '#.#', '#.#'], 'Y': ['#.#', '#.#', '.#.', '.#.', '.#.'], 'Z': ['###', '..#', '.#.', '#..', '###'],
    '0': ['###', '#.#', '#.#', '#.#', '###'], '1': ['.#.', '##.', '.#.', '.#.', '###'], '2': ['##.', '..#', '.#.', '#..', '###'],
    '3': ['##.', '..#', '.#.', '..#', '##.'], '4': ['#.#', '#.#', '###', '..#', '..#'], '5': ['###', '#..', '##.', '..#', '##.'],
    '6': ['.##', '#..', '###', '#.#', '###'], '7': ['###', '..#', '.#.', '.#.', '.#.'], '8': ['###', '#.#', '###', '#.#', '###'],
    '9': ['###', '#.#', '###', '..#', '##.'], '.': ['.', '.', '.', '.', '#'], ',': ['.', '.', '.', '#', '#'],
    ':': ['.', '#', '.', '#', '.'], '/': ['..#', '..#', '.#.', '#..', '#..'], '-': ['...', '...', '###', '...', '...'],
    '+': ['...', '.#.', '###', '.#.', '...'], '%': ['#.#', '..#', '.#.', '#..', '#.#'], '!': ['#', '#', '#', '.', '#'],
    '?': ['##.', '..#', '.#.', '...', '.#.'], '(': ['.#', '#.', '#.', '#.', '.#'], ')': ['#.', '.#', '.#', '.#', '#.'],
    "'": ['#', '#', '.', '.', '.'], '<': ['..#', '.#.', '#..', '.#.', '..#'], '>': ['#..', '.#.', '..#', '.#.', '#..'],
    '#': ['#.#', '###', '#.#', '###', '#.#'], '=': ['...', '###', '...', '###', '...'],
}
SMALL_W, SMALL_H = 6, 10


def small_glyphs():
    out = {}
    for ch, rows in SMALL.items():
        g = Image.new('RGBA', (SMALL_W, SMALL_H), (0, 0, 0, 0))
        for y, row in enumerate(rows):
            for x, c in enumerate(row):
                if c == '#':
                    g.putpixel((x, y), (255, 255, 255, 255))
        out[ch] = g
    return out


# ---------------------------------------------------------------- bars
BAR_STYLES = {  # fill colour, top highlight
    'green': ((80, 200, 80), (150, 240, 150)),
    'aqua': ((70, 190, 230), (160, 230, 255)),
    'gold': ((235, 175, 30), (255, 225, 120)),
    'purple': ((160, 90, 220), (210, 160, 255)),
}
BAR_W, BAR_H, BAR_PIECES = 16, 10, 16
BAR_FIRST = 0xE600  # style k, width w (1..16) -> BAR_FIRST + 16*k + (w-1)


def bar_glyphs():
    out = {}
    for k, (name, (fill, hi)) in enumerate(BAR_STYLES.items()):
        for w in range(1, BAR_PIECES + 1):
            g = Image.new('RGBA', (BAR_W, BAR_H), (0, 0, 0, 0))
            for x in range(w):
                g.putpixel((x, 0), hi + (255,))
                g.putpixel((x, 1), fill + (255,))
                g.putpixel((x, 2), tuple(int(c * 0.75) for c in fill) + (255,))
            out[chr(BAR_FIRST + 16 * k + w - 1)] = g
    return out


# ---------------------------------------------------------------- sheets + metadata
def sheet(glyphs, cw, ch_, cols=16):
    chars = list(glyphs)
    rows = (len(chars) + cols - 1) // cols
    im = Image.new('RGBA', (cw * cols, ch_ * rows), (0, 0, 0, 0))
    grid = []
    for r in range(rows):
        line = ''
        for c in range(cols):
            i = r * cols + c
            if i < len(chars):
                im.paste(glyphs[chars[i]], (c * cw, r * ch_))
                line += chars[i]
            else:
                line += '\u0000'
        grid.append(line)
    return im, grid


def advance(g):
    """Minecraft's bitmap advance at scale 1: the used width + 1."""
    return _width(g) + 1


def families():
    n = normal_glyphs()
    s = {ch: with_shadow(g) for ch, g in n.items()}
    return {
        'normal': (n, CELL_W, CELL_H, CAP_TOP, 4),
        'shadow': (s, CELL_W, CELL_H, CAP_TOP, 4),
        'small': (small_glyphs(), SMALL_W, SMALL_H, 0, 2),
        'bars': (bar_glyphs(), BAR_W, BAR_H, 0, 0),
    }


def export(pack_textures, meta_path):
    meta = {}
    for name, (glyphs, cw, ch_, cap, space) in families().items():
        im, grid = sheet(glyphs, cw, ch_)
        im.save(os.path.join(pack_textures, 'font', 'menu_%s.png' % name))
        meta[name] = {"file": "rune:font/menu_%s.png" % name, "height": ch_, "capTop": cap, "space": space,
                      "chars": grid, "advances": {c: advance(g) for c, g in glyphs.items()}}
    with open(meta_path, 'w', encoding='utf-8') as f:
        json.dump(meta, f, ensure_ascii=True, indent=1)
        f.write('\n')
    return ['assets/rune/textures/font/menu_%s.png' % n for n in meta]
