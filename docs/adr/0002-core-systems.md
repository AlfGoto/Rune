# 0002 — Four core systems: Jobs, Items, Effects, Rewards

**Status:** Accepted (2026-11-09). Amended 2026-11-11: content is defined in Java.

## Context
Rune will hold a lot of content: 12 Jobs, many Rune Tools and Rune Armor pieces, hundreds of Runes and Traits, a large Quest pool, and loot tables. Adding content later should be cheap: a few lines of definition, plus at most one small Java class for new behaviour. Without shared foundations, every new Rune or tool would need ad-hoc code spread across the codebase.

## Decision
Build four core systems. Content is defined with small Java builders and registered by id; behaviour lives in small Java classes registered by id. Balancing numbers live in the plugin's config file.

1. **Jobs** — A Job is a definition: Job Actions and their XP, Account Rate, Quest pool, and unlocks per Job Level. A new Job means one definition, plus a Java class only when it needs custom action detection.

2. **Items** — One `RuneItem` base for every custom item: Rune Tool, Rune Armor, Rune, Rune Cache, Blueprint, Rune Codex, and rare ingredients. The base handles the id, model, Rarity, PersistentDataContainer data, and name/lore rendering. Each item type adds only what is specific to it (slots and Capacity for gear, Rank for Runes, and so on).

3. **Effects** — One system for "when X happens, do Y": triggers (block break, hit, click in air, tick…), conditions, and actions. Runes and Traits are both built on it:
   - Rune = Effect + Rune item data (Rarity, Rank, Max Rank, compatible places).
   - Trait = Effect built into a Rune Tool or Rune Armor piece.
   - Active Rune = Effect triggered from an Active Slot, with a cooldown.
   Effect parameters can vary per Rank. There is no separate "skills" system: Runes are the skills.

4. **Rewards** — One loot/reward system shared by Quest rewards, Rune Caches, Job Level milestones, and the loot that replaces enchanted books. It rolls tables of Runes, ingredients, Reroll Tokens, Blueprints, Job XP and Account XP.

Example: adding a Rune.
```java
rune("veinminer")
    .rarity(LEGENDARY)
    .fits(PICKAXE)
    .effect(VEINMINE, maxBlocks(8, 16, 32));   // one value per Rank
```

### Content in Java (2026-11-11)
- Java builders give compile-time errors, IDE autocomplete and safe renames, with no extra loading or validation code to write.
- The config file keeps only the numbers a server owner may want to tune: XP per action, level curves, Cache drop rates, Quest Tier multipliers.
- Cost: adding a Rune means touching code.

## How we build it
The systems are not designed in full up front. They are built alongside the first real content (Mining + Woodcutting, 3–4 Runes, 2 Rune Tools) and shaped by those real cases. Then they are kept stable as more content is added.

## Consequences
- Most new content is a short Java definition. New behaviour is one registered Effect (or Job action) class.
- Runes and Traits share code, tests and tooling.
- The Effect system is the most important piece to get right, and it gets the most test coverage.
- There is a small upfront cost before the first features ship.
