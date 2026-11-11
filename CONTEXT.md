# Rune — Domain Glossary

An RPG layer for Minecraft: players level Jobs by playing, and customise themselves and their gear with Runes.

## Workflow

**Starting work on an issue** — To have Claude start working on a GitHub issue, comment `@claude do this` on the issue. This triggers the Claude Code GitHub Action, which needs the `CLAUDE_CODE_OAUTH_TOKEN` repository secret to be set.

## Progression

**Job** — A line of activity that earns Job XP from specific in-game actions. There are 12: Mining, Excavation, Woodcutting, Farming, Ranching, Fishing, Combat, Archery, Smithing, Alchemy, Adventuring, Runesmithing.

**Mining** — The Job for natural pickaxe blocks (stone-family blocks and ores), whatever tool breaks them.

**Excavation** — The Job for natural shovel blocks (dirt, sand, gravel, clay, snow, soul sand, mud…) and archaeology (brushing).

**Woodcutting** — The Job for natural axe blocks (logs, wood, Nether stems, giant mushroom blocks, mangrove roots, structure planks…) plus leaves, whatever tool breaks them. Axe blocks that are crops (pumpkins, melons, cocoa, bamboo) belong to Farming; bee nests and beehives belong to Ranching.
_Avoid_: Foraging, Lumberjack

**Felled Tree** — A tree counts as felled when the player breaks the last natural log of its trunk: the logs of the same wood type connected to the one they broke. Cutting only the top of a tree does not fell it. Effects that say "per tree" (Rhythm, replanting, tree Quests) count Felled Trees.

**Job Action** — An in-game action that grants Job XP to exactly one Job (e.g. breaking a diamond ore → Mining). Different actions grant different amounts (diamond ≫ stone). For blocks, the block decides the Job, not the tool (wood broken with a pickaxe → Woodcutting). For kills, the weapon decides (bow kill → Archery, sword kill → Combat). A block only counts when the player breaks it themselves (with a tool, a tool's effect, or by hand) — not by TNT or creepers. Blocks placed by a player, and blocks created by fluids (cobblestone/basalt generators), never give Job XP, in any Job; obsidian is the exception.

**Job XP** — Points earned in a single Job. Not to be confused with vanilla XP.

**Job Level** — A Job's level, derived from its Job XP. No design cap (technical cap: 10 000); unlocks get rarer at high levels. Job Levels give recipes, Blueprints and Rune Caches, never passive stats.

**Account XP** — A separate pool fed by every Job XP gain, multiplied by that Job's **Account Rate** (harder Jobs feed it faster).

**Account Level** — Level derived from Account XP. Sets Rune Capacity up to a cap.

**Vanilla XP** — Minecraft's own green experience bar. Not used for Job progression. Spent on repairing Rune gear, ranking up Runes, and some Evolutions. Vanilla enchanting does not exist in Rune.

## Runes

**Rune** — A physical item that grants an effect when installed. Has a Rarity, a Rank, a Capacity Cost, and a list of compatible places (player, tool types, armor pieces). Runes that share an effect but differ in a parameter (e.g. Affinity: Lush Caves vs Affinity: Mineshaft) are different Runes.
_Avoid_: enchantment

**Player Rune** — A Rune installed on the player. Always passive (e.g. Speed). Kept on death.

**Armor Rune** — A Rune installed in a piece of Rune Armor. Always passive; the same passive Runes that fit on the player can go here, so armor works as extra passive slots. Travels with the armor.

**Tool Rune** — A Rune installed in a Rune Tool. Passive or Active. Travels with the tool.

**Active Rune** — A Tool Rune triggered by a click in the air with its tool, then goes on cooldown.

**Active Slot** — One of up to two Tool Rune slots bound to a click: right-click in air or left-click in air. Tools whose right-click-in-air is already used (bow, crossbow, trident, fishing rod, shield) have only the left-click Active Slot.

**Rarity** — Tier shared by Runes, Rune Tools, Rune Armor, Rune Caches and Quests (as Quest Tier): Common, Uncommon, Rare, Epic, Legendary. Legendary is reserved for game-changing effects (e.g. Veinminer). On a Rune Cache, Rarity does not guarantee the Rarity of its contents (see Rune Cache).

**Rank** — A Rune's upgrade level. Three identical Runes of rank N combine at a smithing table into one of rank N+1. Each Rune has its own **Max Rank**, set by what makes sense for its effect; a Rune can also have no Max Rank (e.g. Deepstride), since each Rank costs three times more than the last.

**Stacking** — When the same effect comes from several sources (a Trait and Runes, or several Runes), their values add up. Every effect has a value, so everything stacks. Chance effects can go above 100%: each full 100% is a guaranteed trigger and the rest is the chance of one more (e.g. Smelt at 150% always smelts once and has a 50% chance to smelt the result again: cobblestone → stone → smooth stone). Exception: Silk Touch is on/off and turns off drop-changing effects (Fortune, Smelt, Ore Bloom) on its tool.

**Capacity** — Budget limiting which Runes can be installed together. Player Capacity comes from Account Level (capped). A Rune Tool's or Rune Armor's Capacity comes from its Rarity, can be raised or lowered by its Trait, and for Rune Tools grows with Mastery.

**Capacity Cost** — What a Rune consumes from Capacity; depends on its Rarity and Rank.

**Mastery** — Extra Capacity a Rune Tool earns through use: +1 each time it performs a set number of valuable Job Actions (e.g. ores mined for a pickaxe, logs for an axe), up to +75% of its base Capacity. Rune Armor has no Mastery.

**Loadout** — The set of Runes currently installed on a player, a tool or an armor piece. Can be changed freely at any time. Players have 8 Player Rune slots.

**Rune Cache** — A sealed item containing a random reward (usually Runes). Dropped by Job Actions, found in loot where vanilla had enchanted books, and given by Quests; the player opens it whenever they choose. Every Cache rolls the same loot table; the Cache's Rarity only shifts the odds and the quantity of what it gives (a Legendary Cache may give several Epic Runes instead of a Legendary one). A Cache dropped by a Job leans towards that Job's Runes.

**Rune Codex** — The item every player carries that opens the Rune menus (Jobs, Quests, Runes).

**Block Reveal** — A visual that outlines chosen blocks through walls for one player, for a set time. Only that player sees it, and it is removed when the time ends, when the player leaves and when the plugin stops. Used by Ore Sense, Bone, Echo, Warden Pickaxe and Worldheart. Test it with `/rune debug reveal <radius>` (outlines ores for 10 seconds).

## Gear

**Rune Tool** — A named tool or weapon obtained through Jobs (e.g. Lichen Pickaxe). Has a fixed Rarity, a Trait, a Harvest Tier, Rune slots and Capacity that grow with Rarity.

**Rune Armor** — A named armor piece, the armor counterpart of a Rune Tool: fixed Rarity, Trait, Rune slots (for Armor Runes), Capacity.

**Trait** — A Rune Tool's or Rune Armor's built-in effect, independent of installed Runes (e.g. Fiery Pickaxe has a chance to smelt). A Trait can share its effect with Runes; they stack (see Stacking). A Trait can be a bonus, a drawback, or both (e.g. faster but less durable), including more or less Capacity.

**Harvest Tier** — Which blocks a Rune Tool can harvest (vanilla's stone / iron / diamond tiers). Defaults from the tool's Rarity (Common = stone-tier, Uncommon = iron-tier, Rare and above = diamond-tier); a tool can be one tier above or below its default as a bonus or drawback.

**Upgrade** — Crafting a new, better-named Rune Tool or Rune Armor piece whose recipe consumes one or more existing pieces (e.g. Miner's Helmet → Caver's Helmet; every biome pickaxe → one Worldpick). The result keeps the installed Runes that fit its slots and Capacity; the rest go to the player's inventory, or drop on the ground if it is full. The result usually looks like a more advanced version of its ingredients. Pieces can also stand alone, outside any Upgrade line. Combining every biome piece (Worldpick, Yggdrasil) gives a craftable Legendary, not a Relic.

**Evolve** — What a few special Rune Tools or Rune Armor pieces can do: move up to the next Rarity while keeping their identity (same name, same item). Each such piece has its own **Evolution** condition (materials, a usage milestone, or both). Unlike Upgrade, no new item is crafted.

**Component** — A crafted intermediate item used in Rune gear recipes (e.g. Reinforced Handle, Gem Setting, Legendary Core). Components are shared by every Job, not tied to one.

**Relic** — A Legendary Rune Tool or Rune Armor piece that cannot be crafted from regular materials, with effects stronger than craftable Legendaries. Found only, extremely rarely: in the chests of a structure that fits its Job (any structure, surface or underground, e.g. Ancient City for Mining, Woodland Mansion for Woodcutting), or as a reward of a Legendary-tier Quest of the matching Job (pickaxe Relics only from Mining Quests, axe Relics only from Woodcutting Quests). Each Relic has its own loot tables. A Relic can only drop for a player at Job Level 25 or more in its Job. A Relic can only be Upgraded into a Fused Relic.

**Fused Relic** — A Relic whose recipe consumes two Relics of the same Job plus outrageously expensive materials. The strongest gear of its Job; still Legendary (no extra Rarity). It cannot be found, only fused, and cannot be Upgraded further.

**Starter Recipe** — A cheap Common Rune Tool or Rune Armor recipe that every player knows from the start, made only from early-game materials.

**Blueprint** — An item that unlocks the recipe for a craftable Legendary Rune Tool or Rune Armor piece (not a Relic). Each Blueprint has its own source: a guaranteed Job Level milestone, structure chests, a vault, or a Quest reward.

**Broken** — State of a Rune Tool at 0 durability: unusable but keeps its Runes. Repaired at an anvil.

**Locked Gear** — Any vanilla stone, iron, gold, diamond or netherite tool or weapon, and all vanilla armor (leather included). Can't be crafted or used, only salvaged. Vanilla wooden tools remain craftable and usable but have no Capacity.
_Avoid_: Locked Tool (old name, too narrow now that armor is included)

**Salvage** — Turning Locked Gear into materials.

## Quests

**Quest** — A Job-specific objective with a reward. A player always has exactly 3 active, each from a random eligible Job. Completing one immediately draws a new one. Every Quest is a Quest Type (Collect, Place, Tool…) drawn at a Quest Tier.
_Avoid_: daily quest (Quests never reset on a timer)

**Quest Type** — The kind of objective a Quest asks for (e.g. Collect, Place, Depth, Variety, Reforestation). Each Job has its own list of Quest Types.

**Quest Tier** — A Quest's difficulty and loot level, using the five Rarities (Common → Legendary). The Tier multiplies the objective's size and improves the reward odds. It is drawn at random; the higher the player's Job Level, the more the odds lean towards hard Tiers. Epic Quests need Job Level 15, Legendary Quests Job Level 25.

**Quest Eligibility** — A Job only supplies Quests once the player reaches its minimum Job Level (1 for gathering Jobs, 2 for crafting-type Jobs like Alchemy).

**Reroll Token** — Lets a player replace one active Quest. One is earned per world day; at most 3 can be held.
