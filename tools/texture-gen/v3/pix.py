"""Tiny pixel-art toolkit for Rune textures (16x16, no anti-aliasing)."""
from __future__ import annotations
import os
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
# vanilla 26.1 textures: `npm install minecraft-assets` in tools/texture-gen, or point MC_ASSETS_26 at a data/26.1 folder
VANILLA = os.environ.get('MC_ASSETS_26') or os.path.join(HERE, '..', 'node_modules', 'minecraft-assets', 'minecraft-assets', 'data', '26.1')
OUT = os.environ.get('RUNE_TEX_OUT') or os.path.join(HERE, '..', 'out')


def hx(s):
    s = s.lstrip('#')
    return (int(s[0:2], 16), int(s[2:4], 16), int(s[4:6], 16), 255)


def lum(c):
    r, g, b = c[:3]
    return 0.2126 * r + 0.7152 * g + 0.0722 * b


def vanilla(path):
    return Image.open(os.path.join(VANILLA, path)).convert('RGBA')


def item(name):
    return vanilla(f'items/{name}.png')


def block(name):
    return vanilla(f'blocks/{name}.png')


class Pix:
    def __init__(self, w=16, h=16, img: Image.Image | None = None):
        if img is not None:
            self.im = img.convert('RGBA').copy()
        else:
            self.im = Image.new('RGBA', (w, h), (0, 0, 0, 0))
        self.w, self.h = self.im.size

    @classmethod
    def load(cls, path):
        return cls(img=Image.open(path))

    def copy(self):
        return Pix(img=self.im)

    def get(self, x, y):
        if 0 <= x < self.w and 0 <= y < self.h:
            return self.im.getpixel((x, y))
        return (0, 0, 0, 0)

    def set(self, x, y, c):
        if 0 <= x < self.w and 0 <= y < self.h:
            if isinstance(c, str):
                c = hx(c)
            self.im.putpixel((x, y), tuple(c))
        return self

    def put(self, pts, c):
        for x, y in pts:
            self.set(x, y, c)
        return self

    def clear(self, pts):
        for x, y in pts:
            self.set(x, y, (0, 0, 0, 0))
        return self

    def pixels(self):
        for y in range(self.h):
            for x in range(self.w):
                p = self.im.getpixel((x, y))
                if p[3]:
                    yield x, y, p

    def remap(self, mapping: dict):
        """mapping: {src_hex: dst_hex_or_tuple}. Exact colour swap."""
        m = {}
        for k, v in mapping.items():
            m[hx(k)[:3] if isinstance(k, str) else tuple(k[:3])] = hx(v) if isinstance(v, str) else tuple(v)
        for x, y, p in list(self.pixels()):
            if p[:3] in m:
                self.im.putpixel((x, y), m[p[:3]])
        return self

    def tint(self, ramp, mask=None):
        """Luminance remap of grey-ish pixels onto a discrete ramp (dark -> light).
        Every pixel snaps to one ramp stop: no new colours."""
        n = len(ramp)
        ramp = [hx(c) if isinstance(c, str) else c for c in ramp]
        for x, y, p in list(self.pixels()):
            if mask and (x, y) not in mask:
                continue
            l = lum(p) / 255.0
            i = min(n - 1, int(round(l * (n - 1))))
            self.im.putpixel((x, y), (ramp[i][0], ramp[i][1], ramp[i][2], p[3]))
        return self

    def tint_ranked(self, ramp):
        """Like tint, but by rank of the template's own distinct colours (works for light-only templates)."""
        ramp = [hx(c) if isinstance(c, str) else c for c in ramp]
        uniq = sorted({p[:3] for _, _, p in self.pixels()}, key=lum)
        n = len(uniq)
        idx = {c: min(len(ramp) - 1, int(round(i / max(1, n - 1) * (len(ramp) - 1)))) for i, c in enumerate(uniq)}
        for x, y, p in list(self.pixels()):
            c = ramp[idx[p[:3]]]
            self.im.putpixel((x, y), (c[0], c[1], c[2], p[3]))
        return self

    def blit(self, other: 'Pix', dx=0, dy=0):
        for x, y, p in other.pixels():
            self.set(x + dx, y + dy, p)
        return self

    def grid(self, rows, key, dx=0, dy=0):
        """Draw from ASCII rows; key maps char -> colour. '.' = skip."""
        for y, row in enumerate(rows):
            for x, ch in enumerate(row.replace(' ', '')):
                if ch == '.':
                    continue
                c = key[ch]
                if c is None:
                    self.set(x + dx, y + dy, (0, 0, 0, 0))
                else:
                    self.set(x + dx, y + dy, c)
        return self

    def scaled(self, k):
        return self.im.resize((self.w * k, self.h * k), Image.NEAREST)

    def save(self, path):
        os.makedirs(os.path.dirname(path), exist_ok=True)
        self.im.save(path)
        return self


def ramp_from(img: Image.Image, n=4, lo=0.14, hi=0.94, skip_dark=0.11):
    """Pick n shades (dark->light) from a vanilla texture's own colours."""
    img = img.convert('RGBA')
    cols = {}
    for y in range(img.height):
        for x in range(img.width):
            p = img.getpixel((x, y))
            if p[3] < 200:
                continue
            if lum(p) / 255.0 < skip_dark:
                continue
            cols[p[:3]] = cols.get(p[:3], 0) + 1
    uniq = sorted(cols, key=lum)
    if not uniq:
        return [(128, 128, 128, 255)] * n
    out = []
    for i in range(n):
        t = lo + (hi - lo) * i / max(1, n - 1)
        out.append(uniq[min(len(uniq) - 1, int(t * (len(uniq) - 1)))] + (255,))
    return out


def darken(c, f=0.55):
    c = hx(c) if isinstance(c, str) else c
    return (int(c[0] * f), int(c[1] * f), int(c[2] * f), 255)


def lighten(c, f=0.3):
    c = hx(c) if isinstance(c, str) else c
    return (int(c[0] + (255 - c[0]) * f), int(c[1] + (255 - c[1]) * f), int(c[2] + (255 - c[2]) * f), 255)


def sheet(pixes, cols=16, scale=1):
    """Pack Pix list into a sprite sheet; returns (Image, positions)."""
    n = len(pixes)
    rows = (n + cols - 1) // cols
    im = Image.new('RGBA', (cols * 16, rows * 16), (0, 0, 0, 0))
    pos = []
    for i, p in enumerate(pixes):
        x, y = (i % cols) * 16, (i // cols) * 16
        im.paste(p.im, (x, y))
        pos.append((x, y))
    if scale != 1:
        im = im.resize((im.width * scale, im.height * scale), Image.NEAREST)
    return im, pos
