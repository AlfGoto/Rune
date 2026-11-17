"""Debug preview: a labelled grid at x6 on a vanilla-ish slot background."""
import sys, os
from PIL import Image, ImageDraw
from pix import Pix, OUT


def preview(items, path, cols=8, scale=6):
    cell = 16 * scale + 8
    rows = (len(items) + cols - 1) // cols
    im = Image.new('RGBA', (cols * cell, rows * (cell + 14)), (30, 30, 32, 255))
    d = ImageDraw.Draw(im)
    for i, (name, p) in enumerate(items):
        x, y = (i % cols) * cell, (i // cols) * (cell + 14)
        d.rectangle([x + 2, y + 2, x + cell - 3, y + cell - 3], fill=(139, 139, 139, 255))
        im.paste(p.scaled(scale), (x + 4, y + 4), p.scaled(scale))
        d.text((x + 4, y + cell), name[:22], fill=(220, 220, 220, 255))
    im.save(path)


if __name__ == '__main__':
    d = os.path.join(OUT, 'items')
    names = sys.argv[1:] or sorted(f[:-4] for f in os.listdir(d))
    preview([(n, Pix.load(os.path.join(d, n + '.png'))) for n in names], os.path.join(OUT, 'preview.png'))
