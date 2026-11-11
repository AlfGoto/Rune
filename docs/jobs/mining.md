# Mining — design (grilling sessions, 2026-11-10)

Starting values for the Mining Job. Every number lives in config and will be tuned by playtesting. Terms are defined in `../../CONTEXT.md`. All item names are translated (English default + French) through the language files.

## Scope & XP rules
- Mining = natural pickaxe blocks (stone-family blocks, ores, structure blocks). **The block decides the Job, not the tool** (wood broken with a pickaxe gives Woodcutting XP). Shovel blocks belong to Excavation.
- A block only counts when the player breaks it themselves: with a tool, a tool's effect (Veinminer, Blast Mine…), or by hand. TNT and creepers give 0 XP.
- 0 XP: blocks placed by a player, and blocks created by fluids (cobblestone / stone / basalt generators). Exception: obsidian (costs a lava source) always gives its XP.
- Multi-block effects (Veinminer, Tunneler, Blast Mine, Great Blast, Starbreaker, Worldheart): full XP and a Cache roll for every block. Veinminer is pickaxe-only and works only on ores (the axe equivalent is Treefeller, see `woodcutting.md`).
- Account Rate: **1.0** — Mining is the reference Job.

## Job XP per block (baseline: stone = 1)
Filler blocks follow their hardness:

| Filler block | XP |
|---|---|
| Netherrack | 0.25 |
| Stone, granite/diorite/andesite, tuff… | 1 |
| Deepslate | 1.5 |
| End stone | 2 |

Fixed values:

| Block | XP |
|---|---|
| Coal ore | 4 |
| Copper ore | 4 |
| Nether gold ore | 4 |
| Nether quartz ore | 5 |
| Amethyst cluster | 5 |
| Copper blocks (Trial Chambers, all variants) | 6 |
| Obsidian (natural or fluid-made) | 6 |
| Iron ore | 8 |
| Redstone / Lapis ore | 8 |
| Gold ore | 12 |
| Gilded blackstone | 15 |
| Raw iron / copper / gold block | 20 |
| Diamond ore | 30 |
| Emerald ore | 40 |
| Ancient debris | 60 |
| Gold block (Bastions, ruined portals) | 100 |

Deepslate ore variants: ×1.25. Structure blocks need no structure detection: a placed one gives 0 XP anyway.

## Level curve (shared by all Jobs)
XP from level L to L+1 = `50 × L^1.3`.

| Level | Total XP | ~Time at 2,500 XP/h |
|---|---|---|
| 5 | ~690 | 17 min |
| 10 | ~3,850 | 1.5 h |
| 15 | ~10,200 | 4 h |
| 20 | ~20,000 | 8 h |
| 25 | ~34,000 | 14 h |

Job Levels give recipes, Blueprints and milestone Rune Caches — never passive stats.

**Milestone Caches** (may be raised later): one every 5 levels — Lvl 5 Common, 10 Uncommon, 15 Rare, 20 Rare, 25 Epic, 30 Epic, then Epic every 5 levels with a Legendary Cache every 25 levels (50, 75, 100…).

## Rune Caches from mining
- Drop chance per block = `XP × 0.05%` (stone ≈ 1/2,000, diamond 1.5%, emerald 2%).
- Contents: 60% Mining Runes, 40% general passive Runes (Player/Armor Runes).
- Rarity follows the shared rule (same loot table; Rarity shifts odds and quantity).

## Pickaxe base stats
Default by Harvest Tier, overridable per pickaxe:

| | Speed | Durability |
|---|---|---|
| stone-tier | 4 | 131 |
| iron-tier | 6 | 250 |
| diamond-tier (Rare) | 8 | 1,561 |
| Epic / Legendary | 12 (gold speed) | 2,031 (netherite) |

Relics may exceed these.

## Mastery
+1 Capacity every **500 valuable Job Actions** (for pickaxes: ores mined), up to +75% of base Capacity. Each Job sets its own value when it is designed (axes: 1,000 logs). Rune Armor has no Mastery.

## Pickaxes
Players start with nothing and craft the vanilla wooden pickaxe (no Capacity). First Rune pickaxes unlock at Mining Level 2.

### Upgrade lines
```
STURDY      Flint (C) ─► Ironbound (U) ─► Obsidian (E) ─► Briseroc (L)
SPEED       Coal (C) ─► Copper (U) ─► Redstone (R) ─► Tunneler (E)
DETECTION   Bone (U) ─► Echo (R)
LOOT        Magnetite (U) ─► Geode (R) ─► Ender (E)
BIOMES      Lichen (U) + Prismarine (R) + Emerald (R) + Fiery (R) + Echo (R) ─► Worldpick (L)
RELICS      Warden Pickaxe (L), Starbreaker (L)
FUSED       Warden Pickaxe + Starbreaker ─► Worldheart (L)
```
Echo serves both Detection and Biomes; sacrificing it for the Worldpick means crafting another one.

### List
| Lvl | Pickaxe | Rarity | Tier | Capacity | Trait |
|---|---|---|---|---|---|
| 2 | Flint | Common | stone | 3 | Fortune · mines slower |
| 2 | Coal | Common | stone | 3 −1 | Mines faster · lower durability |
| 4 | Copper | Uncommon | stone | 6 | Faster as it oxidizes; wax to lock the stage |
| 5 | Ironbound | Uncommon | iron | 6 +2 | Plain, very durable |
| 6 | Lichen | Uncommon | stone (−1) | 6 +1 | Affinity: Lush Caves; glows (light around you) |
| 7 | Bone | Uncommon | iron | 6 | Chance that breaking stone reveals nearby ores |
| 8 | Magnetite | Uncommon | iron | 6 | Magnet (drops pulled to inventory) |
| 10 | Fiery | Rare | diamond | 10 | Affinity: Nether; Smelt chance |
| 11 | Redstone | Rare | diamond | 10 −2 | Momentum: faster per block in a row, resets after 2 s |
| 12 | Geode | Rare | diamond | 10 | Chance for bonus gems |
| 13 | Emerald | Rare | diamond | 10 | Affinity: Mountains (mining + move speed) |
| 14 | Prismarine | Rare | diamond | 10 | Affinity: Ocean; full speed underwater / floating |
| 15 | Echo | Rare | iron (−1) | 10 +2 | Affinity: Deep Dark; sonar ping outlines ores through walls |
| 18 | Obsidian | Epic | diamond | 14 +3 | Nearly unbreakable · slow |
| 20 | Tunneler | Epic | diamond | 14 −3 | Mines 3×3 · slower, more durability use |
| 22 | Ender | Epic | diamond | 14 | Drops go to Ender Chest |
| — | Worldpick (FR: Pioche du Monde) | Legendary | diamond | 18 | Affinity everywhere |
| 22 (Blueprint) | Briseroc | Legendary | diamond | 18 | Chance to double ores; self-repairs in total darkness |
| Relic | Warden Pickaxe | Legendary | diamond | 18 | Every ore broken pings a sonar revealing all ores within 16 blocks |
| Relic | Starbreaker (FR: Fendeur d'étoiles) | Legendary | diamond | 18 | Mines 5×5 with no speed loss |
| Fused Relic | Worldheart (FR: Cœur du Monde) | Legendary | diamond | 18 | Mines 5×5 with no speed loss; every ore broken pings a 24-block sonar; click in air: pulls every revealed ore within 16 blocks straight into the inventory |

- Briseroc Blueprint: guaranteed at Mining Level 22.
- Relics: Ancient City chests (Warden Pickaxe), Trial Vault chests (Starbreaker), and ultra-rare Legendary-tier Mining Quest rewards; only for players at Mining Level 25+.
- Worldheart: no Blueprint; recipe = Warden Pickaxe + Starbreaker + 2 Legendary Cores + 4 netherite blocks + 2 beacons + 1 heavy core + 16 echo shards. Ores pulled by its click give full XP and a Cache roll each.

## Components (shared with every Job)
| Component | Recipe | Used by |
|---|---|---|
| Reinforced Handle | 2 sticks + 1 iron ingot + 1 leather | Every Uncommon+ tool and weapon |
| Reinforced Lining | 2 leather + 1 wool + 1 iron ingot | Every Uncommon+ armor piece |
| Gem Setting | 1 gold ingot + 4 gold nuggets | Rare tools and armor |
| Echo Lens | 4 echo shards + 1 amethyst shard + 1 tinted glass | Detection effects (pickaxe, scoped bow…) |
| Deep Core | 4 deepslate + 4 amethyst shards + 1 diamond | Epic Mining gear and armor, Alchemy |
| Abyssal Plate | 4 obsidian + 4 crying obsidian + 1 netherite scrap | Epic tools and armor of every Job |
| Legendary Core | 1 nether star + 1 dragon head + 1 netherite ingot + 4 diamond blocks + 2 echo shards | Every craftable Legendary, every Job |

## Pickaxe recipes
| Pickaxe | Recipe |
|---|---|
| Flint | 3 flint + 2 sticks |
| Coal | 2 coal + 1 cobblestone + 2 sticks |
| Copper | Coal Pickaxe + 3 copper ingots + Reinforced Handle |
| Ironbound | Flint Pickaxe + 3 iron ingots + Reinforced Handle |
| Lichen | 2 moss blocks + 1 glow lichen + 2 glow berries + Reinforced Handle |
| Bone | 3 bone blocks + 1 skeleton skull + Reinforced Handle |
| Magnetite | 3 raw iron blocks + 1 compass + Reinforced Handle |
| Redstone | Copper Pickaxe + 2 redstone blocks + 1 diamond + Gem Setting |
| Geode | Magnetite Pickaxe + 3 amethyst blocks + 1 diamond + Gem Setting |
| Echo | Bone Pickaxe + Echo Lens + 2 diamonds |
| Fiery | 3 magma blocks + 2 blaze rods + 1 diamond + Gem Setting |
| Prismarine | 2 prismarine + 1 nautilus shell + 2 diamonds + Gem Setting |
| Emerald | 3 emeralds + 1 goat horn + 1 diamond + Gem Setting |
| Obsidian | Ironbound Pickaxe + 2 Abyssal Plates + 2 diamond blocks + 1 ghast tear |
| Tunneler | Redstone Pickaxe + 2 Deep Cores + 1 sticky piston + 2 diamond blocks |
| Ender | Geode Pickaxe + 1 Ender Chest + 1 shulker shell + 2 eyes of ender + 2 diamond blocks |
| Worldpick | Lichen + Prismarine + Emerald + Fiery + Echo Pickaxes + Legendary Core + 2 beacons + 1 emerald block |
| Briseroc | Obsidian Pickaxe + Legendary Core + 2 beacons + 2 netherite blocks + 1 heart of the sea (Blueprint required) |
| Worldheart | Warden Pickaxe + Starbreaker + 2 Legendary Cores + 4 netherite blocks + 2 beacons + 1 heavy core + 16 echo shards |

## Mining Runes
Generic effects fit pickaxe, axe and shovel; cave/ore effects are pickaxe-only.

| Rune | Rarity | Max Rank | Per Rank | Fits |
|---|---|---|---|---|
| Haste | Common | 5 | +8% mining speed | pickaxe, axe, shovel |
| Sturdy | Common | 4 | −25% durability use | pickaxe, axe, shovel |
| Specialist: <ore> | Common | 3 | +20% speed on one ore | pickaxe |
| Fortune | Uncommon | 3 | +15% chance of +1 drop | pickaxe, axe, shovel |
| Magnet | Uncommon | 3 | Pull radius +2 blocks | pickaxe, axe, shovel |
| Ore Sense | Uncommon | 3 | +5% chance to outline nearby ores | pickaxe |
| Affinity: <place> | Uncommon | 3 | +10% speed in one place (Lush Caves, Dripstone, Deep Dark, Mineshaft, Mountains…; forest places for axes) | pickaxe, axe |
| Deepstride | Uncommon | none | Move faster below Y 0, +16 Y per Rank | pickaxe |
| Smelt | Rare | 3 | +25% smelt chance | pickaxe, axe, shovel |
| Cache Hunter | Rare | 3 | +15% Rune Cache chance | pickaxe, axe, shovel |
| Ore Dash *(Active)* | Rare | 3 | Dash to nearest visible ore, range 8/12/16, cooldown 20 s | pickaxe |
| Blast Mine *(Active)* | Epic | 3 | Next block explodes 3×3, cooldown 30 → 10 s | pickaxe |
| Ore Bloom | Epic | 5 | Mining an ore: +8% chance a neighbouring stone becomes that ore | pickaxe |
| Silk Touch | Epic | 1 | Blocks drop themselves; disables Fortune, Smelt, Ore Bloom | pickaxe, axe, shovel |
| Great Blast *(Active)* | Legendary | 3 | Next block explodes 3×3×3, cooldown 60 → 20 s | pickaxe |
| Veinminer | Legendary | 3 | Mines a vein of ores: 8 / 16 / 32 blocks | pickaxe |

- Each variant (Affinity: Lush Caves, Specialist: Iron…) is a separate Rune. Affinity Runes can appear in their place's chest loot.
- Ore Bloom: bloomed ores give full XP and can bloom again. Does not work with Silk Touch.
- Silk-touched ore gives normal XP when first broken; once placed it gives 0.

## Mining armor ("deeper = stronger")
Each piece's Trait scales with depth; each piece has its own scale, default linear from Y 64 (0%) to Y −64 (100%). Protection follows vanilla by Rarity (Common ≈ leather, Uncommon ≈ chainmail, Rare ≈ iron, Epic ≈ diamond, Legendary ≈ netherite), overridable per piece. Capacity fixed (no Mastery).

Unlock levels: Common 3, Uncommon 7, Rare 12, Epic 18, Legendary by Blueprint.

| Slot | Trait | Common | Uncommon | Rare | Epic | Legendary |
|---|---|---|---|---|---|---|
| Helmet | Light radius | Miner's Helmet | Caver's Helmet | Driller's Helm | Abyssal Helm | Crown of the Depths |
| Chestplate | Damage reduction | Miner's Vest | Reinforced Chestplate | Driller's Cuirass | Abyssal Cuirass | Heart of the Mountain |
| Leggings | Mining speed while sneaking | Miner's Trousers | Caver's Leggings | Driller's Leggings | Abyssal Leggings | Roots of the World |
| Boots | Fall damage reduction | Miner's Boots | Caver's Boots | Driller's Boots | Abyssal Boots | Step of the Chasm |

### Armor recipes
| Piece | Recipe |
|---|---|
| Miner's Helmet | 4 leather + 1 candle |
| Caver's Helmet | Miner's Helmet + 3 copper ingots + 1 lantern + Reinforced Lining |
| Driller's Helm | Caver's Helmet + 2 diamonds + 1 soul lantern + Gem Setting |
| Abyssal Helm | Driller's Helm + Abyssal Plate + Echo Lens + 2 diamond blocks + 1 sea lantern |
| Crown of the Depths | Abyssal Helm + Legendary Core + 1 beacon + 2 netherite blocks + 1 wither skeleton skull |
| Miner's Vest | 6 leather + 2 copper ingots |
| Reinforced Chestplate | Miner's Vest + 4 iron ingots + Reinforced Lining |
| Driller's Cuirass | Reinforced Chestplate + 3 diamonds + 1 shield + Gem Setting |
| Abyssal Cuirass | Driller's Cuirass + 2 Abyssal Plates + Deep Core + 2 diamond blocks |
| Heart of the Mountain | Abyssal Cuirass + Legendary Core + 2 beacons + 2 netherite blocks + 1 emerald block |
| Miner's Trousers | 5 leather + 2 string |
| Caver's Leggings | Miner's Trousers + 3 iron ingots + 1 piston + Reinforced Lining |
| Driller's Leggings | Caver's Leggings + 2 diamonds + 2 redstone blocks + Gem Setting |
| Abyssal Leggings | Driller's Leggings + Abyssal Plate + Deep Core + 2 diamond blocks |
| Roots of the World | Abyssal Leggings + Legendary Core + 1 beacon + 2 netherite blocks + 2 mangrove roots |
| Miner's Boots | 4 leather + 1 wool |
| Caver's Boots | Miner's Boots + 2 iron ingots + 1 slime block + Reinforced Lining |
| Driller's Boots | Caver's Boots + 2 diamonds + 2 feathers + 1 phantom membrane + Gem Setting |
| Abyssal Boots | Driller's Boots + Abyssal Plate + 2 phantom membranes + 2 diamond blocks |
| Step of the Chasm | Abyssal Boots + Legendary Core + 1 beacon + 2 netherite blocks + 1 honey block |

### Legendary armor Blueprints
| Piece | Blueprint source |
|---|---|
| Crown of the Depths | Ancient City chests (Mining 20+) |
| Heart of the Mountain | Guaranteed at Mining Level 30 |
| Roots of the World | Rare Mining Quest reward (high Quest Tier) |
| Step of the Chasm | Trial Chambers Ominous Vault |

## Mining Quests
Quest Types at launch: Collect ("mine 20 iron"), Place ("mine 50 blocks in a Mineshaft"), Depth ("mine 10 ores below Y −32"), Tool ("mine 100 blocks with a Coal Pickaxe"). Each is drawn at a Quest Tier (see `design-decisions.md`). Later: Challenge ("without placing torches", timed). Legendary-tier Mining Quests can, ultra-rarely, reward pickaxe Relics; high Tiers can reward the Roots of the World Blueprint.
