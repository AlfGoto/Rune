# 0003 — Code architecture: modules in five families

**Status:** Accepted (2026-11-11)

## Context
ADR 0002 defines the four core gameplay systems (Jobs, Items, Effects, Rewards). The plugin also needs foundations under them, visuals on top of them, and tooling around them. Each topic should be a small module with a clear job, so that pieces are easy to combine and a change in one place does not break another.

## Decision
The code is split into five families of modules.

### 1. Foundations
Invisible, used by everything else.
- **Registry** — Holds every definition (Jobs, Runes, gear, Quests, loot tables) by id. Loads the config file and reports config errors clearly.
- **Storage** — Player data (Job XP, Runes, Quests). One interface; the first implementation uses the player's PersistentDataContainer, MySQL can be added later.
- **Text** — Translation keys, colours and glyphs. Translations ship in the resource pack; every translatable component carries English as fallback.
- **Block Tracking** — Remembers which blocks were placed by a player or grown with bone meal. The base of all anti-exploit rules.
- **Triggers** — Listens to Paper events (block break, hit, click, tick…) and turns them into clean Rune events that the gameplay modules use.

### 2. Visuals
Read gameplay data, never change it.
- **Resource Pack** — Generates the pack (models, textures, glyphs, sounds, language files), serves it over HTTP and sends it to players.
- **Item Rendering** — Builds an item's name, lore, Rarity colour and Rune slots from its data. Change the data, the item redraws itself.
- **Interface** — A small menu framework (buttons, pages, back navigation). The Rune Codex, Jobs, Quests, loadout and Cache reel menus are built on it.
- **Feedback** — Action bar ("+12 Mining XP"), boss bars, level-up titles, sounds, particles, cooldown display.

### 3. Gameplay
The rules of the game.
- **Jobs** — Job Actions → Job XP, Job Levels, Account XP.
- **Items** — `RuneItem` base, durability, Broken state, Mastery.
- **Effects** — Triggers + conditions + actions, values per Rank, Stacking.
- **Loadout & Capacity** — Install and remove Runes, check Capacity, compute the total value of every effect.
- **Rewards** — Loot tables.
- **Quests** — Quest Type × Quest Tier, the 3 active Quests, Reroll Tokens. Uses Rewards.
- **Crafting** — Recipes, Blueprints, Upgrade, Evolve, Components, Rank-up at the smithing table.
- **Vanilla Overrides** — Enchanting removed, Locked Gear, Salvage, enchanted books in loot replaced by Rune Caches.

### 4. Content
Java definitions (plus a few Effect classes): one package per Job with its Runes, Rune Tools, Rune Armor and Quest Types.

### 5. Tooling
- Admin and debug commands (`/rune give`, `/rune xp`…).
- MockBukkit tests, with the most coverage on Effects and Loadout & Capacity.
- CI, `/preview` servers, Renovate.

## Dependency rule
Dependencies always point the same way: **Content → Gameplay → Foundations**. Visuals may read anything, but nothing depends on Visuals. Rebuilding a menu or a lore layout can never break a game rule.

## Build order
1. Tooling + Foundations
2. Resource Pack + Item Rendering (see a custom item in game early)
3. Jobs + Feedback (break an ore, see "+XP")
4. Effects + Loadout & Capacity + 3–4 Runes
5. Interface (Rune Codex, loadout menus)
6. Rewards → Rune Caches → Quests
7. Crafting + Vanilla Overrides
8. The rest of the Mining and Woodcutting content

Modules are shaped by this first real content (ADR 0002, "How we build it"), not designed in full up front.

## Consequences
- Each module can be tested on its own; Foundations and Gameplay can be tested without any visuals.
- New content touches only the Content family.
- A few more packages up front than a single "everything" package, in exchange for changes that stay local.
