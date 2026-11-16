"""Shared crafting components, hand-drawn on 16x16 grids with material ramps."""
import os
from pix import Pix, OUT, darken, hx
from palettes import R


def key(a, b=None, c=None):
    k = {'o': darken(R(a)[0], 0.45), 'd': R(a)[0], 'm': R(a)[1], 'l': R(a)[2], 'h': R(a)[3], 'k': hx('181818')}
    if b:
        k.update({'O': darken(R(b)[0], 0.45), 'D': R(b)[0], 'M': R(b)[1], 'L': R(b)[2], 'H': R(b)[3]})
    if c:
        k.update({'0': darken(R(c)[0], 0.45), '1': R(c)[0], '2': R(c)[1], '3': R(c)[2], '4': R(c)[3]})
    return k


COMPONENTS = {
    'reinforced_handle': ('Reinforced Handle', [
        '................',
        '............OO..',
        '...........OLHO.',
        '..........OLMMO.',
        '.........OLMMO..',
        '........kdmdO...',
        '.......kdmdk....',
        '......kdmdk.....',
        '.....kdmdk......',
        '....kdmdk.......',
        '...OLMMk........',
        '..OLMMO.........',
        '.OLMMO..........',
        '.OMMO...........',
        '..OO............',
        '................'], key('leather', 'iron')),
    'reinforced_lining': ('Reinforced Lining', [
        '................',
        '................',
        '...oooooooooo...',
        '..ohllllllllho..',
        '..olHHHHHHHHlo..',
        '..olHLLLLLLHlo..',
        '..olHL3333LHlo..',
        '..olHL3443LHlo..',
        '..olHL3443LHlo..',
        '..olHL3333LHlo..',
        '..olHLLLLLLHlo..',
        '..omHHHHHHHHmo..',
        '..odmmmmmmmmdo..',
        '...oooooooooo...',
        '................',
        '................'], key('leather', 'wool', 'iron')),
    'gem_setting': ('Gem Setting', [
        '................',
        '................',
        '................',
        '.....oooooo.....',
        '....ohllllho....',
        '...ohlo..olho...',
        '...olo....olo...',
        '...olo....olo...',
        '...olo....olo...',
        '...omlo..olmo...',
        '....omllllmo....',
        '.....oddddo.....',
        '......oooo......',
        '................',
        '................',
        '................'], key('gold')),
    'echo_lens': ('Echo Lens', [
        '................',
        '................',
        '.....oooooo.....',
        '....oDDDDDDo....',
        '...oDMLLLLMDo...',
        '..oDMLHhhLLMDo..',
        '..oDLHhllhLLDo..',
        '..oDLhl44lhLDo..',
        '..oDLhl44lhLDo..',
        '..oDLHhllhLLDo..',
        '..oDMLHhhLLMDo..',
        '...oDMLLLLMDo...',
        '....oDDDDDDo....',
        '.....oooooo.....',
        '................',
        '................'], key('sculk', 'amethyst', 'soul')),
    'deep_core': ('Deep Core', [
        '................',
        '................',
        '....oooooooo....',
        '...ollllllllo...',
        '..olmmmmmmmmlo..',
        '..olmMLLHLmmlo..',
        '..olmMLHHLLmlo..',
        '..olmMLL44LMlo..',
        '..olmMLH44LMlo..',
        '..olmMLLHLLMlo..',
        '..olmmMMMMMmlo..',
        '..odmmmmmmmmdo..',
        '...oddddddddo...',
        '....oooooooo....',
        '................',
        '................'], key('deepslate', 'amethyst', 'diamond')),
    'abyssal_plate': ('Abyssal Plate', [
        '................',
        '..oooooooooooo..',
        '.o4mmmmmmmmmm4o.',
        '.omlmmmmmMmmmlo.',
        '.omlmmmmMLMmmlo.',
        '.omlmmmMLHLMmlo.',
        '.omlmmMLMMLMmlo.',
        '.omlmMLMmmMLmlo.',
        '.omlMLMmmmmMLlo.',
        '.omlLMmmmmmmMlo.',
        '.omlMmmmmmmmmlo.',
        '.omdmmmmmmmmmdo.',
        '.o4dddddddddd4o.',
        '..oooooooooooo..',
        '................',
        '................'], key('obsidian', 'crying', 'netherite')),
    'legendary_core': ('Legendary Core', [
        '................',
        '.......oo.......',
        '......oLLo......',
        '......oLHo......',
        '..oo..oHHo..oo..',
        '.oLLooHhhHooLLo.',
        '.oLHH4hhhh4HHLo.',
        '..oH4hhHHhh4Ho..',
        '..oH4hhHHhh4Ho..',
        '.oLHH4hhhh4HHLo.',
        '.oLLooHhhHooLLo.',
        '..oo..oHHo..oo..',
        '......oLHo......',
        '......oLLo......',
        '.......oo.......',
        '................'], key('star', 'diamond', 'netherite')),
}


def render(outdir):
    res = {}
    for name, (disp, rows, k) in COMPONENTS.items():
        p = Pix().grid(rows, k)
        p.save(os.path.join(outdir, 'items', name + '.png'))
        res[name] = (p, disp)
    return res


if __name__ == '__main__':
    render(OUT)
