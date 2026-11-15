"""Shared crafting Components (16x16, vanilla item style)."""
from kit import Book

B = Book()
add = B.add

# Reinforced Handle: vanilla stick + leather grip + iron ferrule and cap
add('reinforced_handle', 'Reinforced Handle', 'U', 'Components', [
    "................",
    "............sSs.",
    "............SXs.",
    "...........aXSs.",
    "..........acb...",
    ".........adb....",
    "........acb.....",
    ".......LMk......",
    "......LMk.......",
    ".....LMk........",
    "....adb.........",
    "...acb..........",
    "..sSs...........",
    "..SXs...........",
    "..ss............",
    "................",
], dict(a='#493615', b='#281e0b', c='#896727', d='#684e1e',
        L='#893b25', M='#542716', k='#3d1c10',
        s='#353535', S='#a8a8a8', X='#d8d8d8'))

# Reinforced Lining: folded leather with a wool inner layer, iron-studded edge
add('reinforced_lining', 'Reinforced Lining', 'U', 'Components', [
    "................",
    "................",
    "...aaaaaaaaaa...",
    "..adcdcdcdcdca..",
    "..acIcccccIcda..",
    "..adccccccccda..",
    "..abwwwwwwwwba..",
    "..abWwWwWwWwba..",
    "..abwwwwwwwwba..",
    "..adccccccccda..",
    "..acIcccccIcda..",
    "..adcdcdcdcdca..",
    "...afffffffff...",
    "................",
    "................",
    "................",
], dict(a='#542716', b='#893b25', c='#c65c35', d='#9e492a', f='#3d1c10',
        w='#e9ecec', W='#c8cccc', I='#d8d8d8'))

# Gem Setting: an empty gold bezel with four prongs
add('gem_setting', 'Gem Setting', 'R', 'Components', [
    "................",
    "................",
    ".....p....p.....",
    ".....P....P.....",
    "....aabbbbaa....",
    "...abccccccba...",
    "...bc.dddd.cb...",
    "...bcd....dcb...",
    "...bcd....dcb...",
    "...bc.dddd.cb...",
    "...abccccccba...",
    "....aaggggaa....",
    ".....P....P.....",
    ".....p....p.....",
    "................",
    "................",
], dict(a='#7f520c', b='#e9b115', c='#f9f969', d='#6e470b', g='#dc9613', p='#fdf55f', P='#b26411'))

# Echo Lens: tinted glass in an amethyst rim, echo shard focus
add('echo_lens', 'Echo Lens', 'R', 'Components', [
    "................",
    "................",
    ".....aaaaaa.....",
    "....abbccbba....",
    "...abtttttdba...",
    "..abtttEettdba..",
    "..abttEEEetdba..",
    "..acttEeEttcda..",
    "..acttteetttca..",
    "..abdttttttdba..",
    "...abdttttdba...",
    "....abbccbba....",
    ".....aaaaaa.....",
    "................",
    "................",
    "................",
], dict(a='#2e1f52', b='#8d6acc', c='#cfa0f3', d='#54398a',
        t='#2a2033', E='#29dfeb', e='#009295'))

# Deep Core: deepslate shell, amethyst crystals, diamond heart
add('deep_core', 'Deep Core', 'E', 'Components', [
    "................",
    ".......p........",
    "......pP.p......",
    "....aaPaaP......",
    "...abbcbbaa.....",
    "..abccdccbba....",
    "..abcdDdDcba.p..",
    "..acdDXDdcba.P..",
    "..abcdDdDcbaPa..",
    "..abbcdcdcbba...",
    "...abbcbcbba....",
    "...p.aaaaaa.....",
    "...P............",
    "................",
    "................",
    "................",
], dict(a='#1a1a20', b='#3d3d43', c='#515151', d='#646464',
        D='#4bede6', X='#d5fff6', p='#cfa0f3', P='#8d6acc'))

# Abyssal Plate: obsidian slab, crying veins, netherite-scrap rivets
add('abyssal_plate', 'Abyssal Plate', 'E', 'Components', [
    "................",
    "................",
    "..aaaaaaaaaaaa..",
    ".abnccccccccnba.",
    ".abccdccccvcccb.",
    ".acccdvcccvccca.",
    ".acccccvcccvcca.",
    ".acdccccvcccvca.",
    ".acdvcccccccvca.",
    ".acccvcccccdcca.",
    ".abccccvccccdca.",
    ".abnccccccccnba.",
    "..aaaaaaaaaaaa..",
    "................",
    "................",
    "................",
], dict(a='#06030b', b='#3b2754', c='#100c1c', d='#271e3d', v='#8308e4', n='#95867e'))

# Legendary Core: a nether star caged in gold and netherite, echo eyes
add('legendary_core', 'Legendary Core', 'L', 'Components', [
    "................",
    ".......gG.......",
    "......gnnG......",
    "....gGn s nGg...",
    "...gn.sSs..ng...",
    "..gn.sSwSs..nG..",
    "..Gn sSwWSs.nG..",
    ".gnEsSwWwSsEng..",
    "..Gn sSwSs..nG..",
    "..gn..sSs...ng..",
    "...gn..s...ng...",
    "....gGn..nGg....",
    "......gnnG......",
    ".......gG.......",
    "................",
    "................",
], {'g': '#b26411', 'G': '#fdf55f', 'n': '#31292a', 's': '#88a4a4', 'S': '#cbd6d6',
    'w': '#fdffa8', 'W': '#ffffff', 'E': '#29dfeb', ' ': '#141018'})

P = B.items
