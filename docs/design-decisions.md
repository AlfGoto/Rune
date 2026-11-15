# Design decisions (grilling session, 2026-11-08)

Working log of settled decisions. Terms are defined in `CONTEXT.md`.

## Platform & workflow
- Paper plugin + server resource pack, latest Minecraft (see ADR 0001). Minimum 26.3 (Poplar trees, Abandoned Camps are used by Woodcutting).
- Language: Java. Base package: `com.alfgoto.rune`.
- License: MIT.
- Resource pack served by the plugin itself (built-in HTTP server on `resource-pack.port`, default 8123). `resource-pack.external-url` sends players to another URL instead (nothing is served then). The pack is sent on join with its SHA-1 (2026-11-14, #18). The built-in URL uses the address the player joined with. Confirmed by Alfred (2026-11-14): keep the `resource-pack.port` key, hash of the plugin's own zip, optional pack. In external mode the hash is the one of the pack the plugin built, so the URL must serve `plugins/Rune/rune-pack.zip` (the zip is deterministic). The pack is optional (not forced): a player can decline. `/preview` uses the external URL: the workflow uploads the built pack to the `preview` GitHub Release and points the URL at its download link (the free playit.gg plan has no TCP tunnel for a second port).
- Minecraft version in one build property; Renovate opens version-bump PRs.
- Local test: `./gradlew runServer`, join `localhost` with a vanilla client.
- CI: build + MockBukkit tests + Claude PR review, copying the FloconAI repo's setup (same auth method).
- Conflicts (2026-11-14): after each PR merged into main, `fix-conflicts.yml` comments `@claude fix conflicts` on every open PR that now conflicts with main (once per PR commit). The comment starts the normal @claude agent, which merges main in.
- Player-facing text: English (default) + French, all text (including every item name) in language files with English as fallback, so server owners can add more languages.
- Translations ship in the resource pack (2026-11-11): items, menus and messages use translatable components, and each client shows them in its own language. Every translatable component carries the English text as fallback, so a missing key never shows a raw key.
- Text helpers (`com.alfgoto.rune.text`): translation keys are `rune.<category>.<id>`; language files live in `src/main/resources/lang/<language>/<module>.json`, one file per module (ADR 0004), and every language must have the same keys. Rarity colours live only in the `Rarity` enum: Common white, Uncommon green, Rare aqua, Epic light purple, Legendary gold (vanilla named colours, may change in the visual polish pass).
- Admin commands (2026-11-11): one root `/rune` command, permission `rune.admin` (default op). Each subcommand implements `Subcommand` and is registered by its module with `ctx.subcommand(...)`; debug tools go under `/rune debug <name>` with `ctx.debug(...)` (ADR 0004). Messages are translatable components with English fallback; keys live in the module's lang files. First subcommand: `/rune version`.
- Resource pack build (2026-11-11): `ResourcePackBuilder` (`visuals.resourcepack`) zips `pack.mcmeta`, the assets listed in `src/main/resources/pack/files.txt` (add every new asset there) and the plugin lang files (merged per language) at startup into `plugins/Rune/rune-pack.zip`. Deterministic zip, SHA-1 logged. Serving and sending it: see the resource pack decision above. The pack format is a constant for now (open point in `docs/research/resource-pack.md`).
- Block Tracking (2026-11-11): tags live in the chunk's PersistentDataContainer (one long array of chunk-local positions per tag), so they survive restarts. Tags: `placed`, `grown-natural`, `grown-bonemeal`, `fluid`. A block placed by a player is tagged; breaking it clears the tag; pistons move the tag with the block. Grown blocks (2026-11-13): trees, giant mushrooms and huge fungi are tagged from `StructureGrowEvent` (`grown-bonemeal` if from bone meal, player or dispenser, else `grown-natural`); vines, glow lichen and chorus spreading over time are `grown-natural`; glow lichen and big dripleaf fertilized with bone meal are `grown-bonemeal`. A growth replaces older tags on the block (a planted sapling stops being `placed`). A block formed by fluids (cobblestone, stone, basalt, obsidian, from `BlockFormEvent`) is tagged `fluid`; obsidian keeps its XP. The tags only mark blocks; the XP multipliers come with the Job XP rules. `BlockTrackingModule` provides the `BlockTracker` service. Debug: `/rune debug blocks` shows the tags of the block you look at. Not yet cleared: explosions, fire, leaf decay, fluids replacing a block (decide with the Job XP rules). A placed block always starts with only `placed`: placing clears older tags first, so a stale tag cannot stay.
- Repo: AlfGoto/Rune, public (GitHub Actions free and unlimited).
- PR preview: comment `/preview` on a PR → GitHub Action builds the plugin, starts Paper and exposes it through a free playit.gg tunnel with a fixed address. One preview slot: the latest `/preview` replaces the previous one; the bot comments which PR is live. Auto-stops after 10 min with no players, 1 h max.
- Code: a generic Job framework first; each Job is a definition (actions, tools, Runes, quest pool). Balancing lives in config. A bad config value logs a clear error and falls back to the default (Registry module).
- Content (Jobs, Runes, gear, Quests, loot) is defined with Java builders; only balancing numbers live in the config file (2026-11-11, see ADR 0002).
- Build Mining + Woodcutting first.
- Stay on Paper (alternative considered: Fabric + Polymer — rejected for harder version updates and fewer compatible servers).
- Player data stored in the player's PersistentDataContainer (no external database); storage behind one interface so MySQL can be added later for multi-server networks. Leaderboards use a small cache file.
- `PlayerStorage` (2026-11-11): string values by key (`get`/`set`/`remove` per player UUID); callers serialize their own data. The PDC implementation needs the player online (Paper cannot read an offline player's PDC). PDC calls must run on the main thread.

## Jobs
- 12 Jobs: Mining (pickaxe blocks), Excavation (shovel blocks + archaeology), Woodcutting (axe blocks + leaves), Farming (incl. herbs, cooking, pumpkins, melons, cocoa, bamboo), Ranching (husbandry, taming, bees incl. bee nests and beehives), Fishing (incl. ocean loot), Combat (melee, shield, hunting), Archery, Smithing (repairs, Rune Tool and Rune Armor crafting), Alchemy, Adventuring (biomes, structures, Nether/End, trading), Runesmithing.
- Dropped: Crafting, Building, Engineering (exploitable).
- Job XP weighted per action. Each Job has an Account Rate feeding Account XP. Mining = 1.0, the reference Job. Woodcutting = 0.8.
- Job Level has no design cap. Technical cap: levels are read up to 10 000 (`LevelCurve.MAX_LEVEL`) so the level loop stays short. One shared curve for all Jobs: XP to next level = `50 × L^1.3` (config: `jobs.level-curve.base` / `.exponent`).
- Job Levels give recipes, Blueprints and Rune Caches, never passive stats. Milestone Cache every 5 levels.
- Each Job Action feeds exactly one Job. For blocks, the block decides (a pickaxe on wood → Woodcutting XP). For kills, the weapon decides: bow/crossbow kill → Archery; sword/axe kill → Combat. Ranching never gets XP from kills (only breeding, taming, shearing, milking, bees).
- A Job's block scope starts from a vanilla tool tag (pickaxe, shovel, axe), with explicit carve-outs where another Job owns the block (e.g. axe-tag crops → Farming) and explicit additions (e.g. leaves → Woodcutting).
- Felled Tree detection (2026-11-14): `foundations.trees` holds the shared `TreeScan` (same wood type, connected through sides, edges and corners, natural = not `placed`; it follows the whole connected trunk in every direction) and fires the `TreeFelled` Rune event (in `foundations.triggers`) when a player breaks the last natural log of a trunk, whatever the break cause. Wood type comes from the block name: oak log, oak wood and both stripped forms are all "oak"; stems and hyphae follow the same way. The scan has no config key; its only limit is the cap its caller gives. `/rune debug events` also sends a chat message to the player on each Felled Tree. The canopy and "at or above" parts of the Treefeller rules are not in `TreeScan` yet: they come with Treefeller.
- Felling session (2026-11-15, Alfred, PR #144): a gap-free "connected" check fails once logs are gone (cutting 67, 65, 66, 64 would fire early, then again). So `FellingSessions` keeps an in-memory session per tree. The first natural-log break that is not in a session scans the full connected trunk (still intact) and stores the log positions. Each break removes its position and prunes positions that are no longer natural logs (fire, explosions, other players). When the set is empty, `TreeFelled` fires once. A session expires after 30 minutes and is lost on restart; the scan is capped at 200 logs (a bigger tree is tracked only up to 200). These two numbers are constants in code, not config keys, until Alfred asks for keys.
- Block Job Actions (2026-11-14): `gameplay.jobs` holds the `BlockActions` table (block → Job + base XP), `BlockXpRules` (placed 0; fluid-made 0 except obsidian; bone-meal grown × `jobs.bonemeal-multiplier`, default 0.25) and `BlockActionListener`, which turns a `RuneBlockBreakEvent` into Job XP through `JobProgress` (the action bar shows it). `content.jobs.CoreBlockActions` fills the table: the Job comes from the vanilla tool tag (pickaxe → Mining, axe → Woodcutting, shovel → Excavation) with the carve-outs (axe crops → Farming, bee nests / beehives → Ranching) and additions (leaves, wart blocks, shroomlight, glow lichen → Woodcutting); the XP per block lives in the config (`jobs.block-xp`, block name → XP, values from `docs/jobs/mining.md` and `woodcutting.md`; a wrong name or value is logged and skipped). A block not listed there gives no XP, and a block whose Job is not built yet (Excavation, Farming, Ranching) gives nothing. Block Tracking clears tags when a block breaks, so `BlockTrackingModule` depends on `TriggersModule` to make the Rune break event fire first. Mastery ×0.25 for bone-meal blocks comes with Mastery. Decided 2026-11-14 (Alfred), lowered ×0.2 the same day (issue #141): filler values stone, granite, diorite, andesite, tuff, cobblestone, basalt = 0.2 and cobbled deepslate = 0.3; the key `jobs.bonemeal-multiplier` stays; a block placed by a dispenser counts as `placed` (0 XP): `BlockTrackingListener` tags the block in front of a dispenser from `BlockDispenseEvent` when the item is a block item and the target is not solid (air, water, replaceable plants). Poplar exists in paper-api 26.3.build.251-stable and is in the config: poplar log / wood / stripped = 3, planks = 0.5, red / orange / yellow poplar leaves = 0.1.
- Per-Job content lives in `docs/jobs/<job>.md` (Mining done 2026-11-10, Woodcutting done 2026-11-11).

## Runes
- Runes are physical items.
- 5 Rarities: Common, Uncommon, Rare, Epic, Legendary (Legendary = game-changing, e.g. Veinminer).
- Rank up: 3 identical Runes of rank N at a smithing table → rank N+1, costs vanilla XP. (Prototype needed: smithing table slot restrictions.)
- Max Rank is set per Rune, by what makes sense for its effect. Some Runes have no Max Rank (e.g. Deepstride); the 3× cost per Rank caps them naturally. Defined in each Rune's definition.
- Variants (Affinity: <place>, Specialist: <ore>, Specialist: <wood>, Sanctuary: <biome>) are separate Runes: they rank up separately and stack with each other. One effect class with a parameter. Variants can appear in their place's chest loot.
- Stacking: the same effect from a Trait and/or several Runes adds up. Every effect is a number (on/off effects become chances). Chances above 100% roll again (e.g. Smelt 150% → always one smelt, 50% chance of a second: cobblestone → stone → smooth stone). Exception: Silk Touch (on/off) disables Fortune, Smelt and Ore Bloom.
- Stacking code (2026-11-14, `gameplay.effects.Stacking`): chance values are in percent (100 = guaranteed), like Rune values. `Stacking.total` sums the values of one effect id over a list of sources (Trait + Runes, each with its Rank). `Stacking.rolls` gives the number of triggers: each full 100% is one, the rest is the chance of one more. Silk Touch (effect id `silk_touch`) among the sources makes the total 0 for `fortune`, `smelt` and `ore_bloom`. These ids are reserved names; the Runes themselves come with their own tickets.
- Transmuting postponed (future Rune Altar).
- Obtained from Job Level milestones, rare drops during Job Actions, and Quest rewards.
- Runesmithing Job earns XP from ranking up.
- Player: 8 slots open from start, passive Runes only.
- Rune Armor slots take the same passive Runes as the player (Armor Runes) — armor acts as extra passive slots, limited by the armor's own Capacity.
- Swapping Runes is free and possible anytime.
- Each Rune lists compatible places (player, tool types, armor pieces); generic tool effects fit pickaxe, axe and shovel.
- Multi-block "harvest the whole thing" effects are tool-specific and named after their target: Veinminer = pickaxe, ores only; Treefeller = axe, trees only. They give full XP and a Cache roll for every block, and each has a block cap per Rank.
- Active Runes: Tool Runes on right-click-in-air / left-click-in-air (max 2 Active Slots per tool; 1 for bow, crossbow, trident, rod, shield). Right-click on a block stays vanilla.
- Movement abilities are contextual Tool Runes (Axe: dash to tree; Sword: lunge to monster; Pickaxe: dash to ore).
- UI: dedicated loadout menus, Active Slots highlighted, rarity colours, custom icon glyphs in names.
- Menus open from a Rune Codex item in the inventory (main menu: Jobs, Quests, Runes).
- Rune Codex: soulbound, fixed in the last inventory slot (not hotbar), can't be moved, dropped or lost on death. Clicking it in the inventory screen opens the main menu.

## Capacity (2026-11-10)
- Capacity Cost = Rarity base (Common 1, Uncommon 2, Rare 3, Epic 5, Legendary 8) + 1 per Rank above 1.
- Rune Tool / Rune Armor base Capacity by Rarity: Common 3, Uncommon 6, Rare 10, Epic 14, Legendary 18. Slots: Common 1 … Legendary 5.
- Traits can add or remove Capacity (bonus / drawback).
- Mastery: a Rune Tool gains +1 Capacity every N valuable Job Actions, up to +75% of base Capacity. Pickaxe: 500 ores. Axe: 1,000 logs. Other tools set when their Job is designed. Rune Armor has fixed Capacity.
- Account Level curve: XP to next level = `200 × L^1.3` (rewards playing several Jobs; config: `account.level-curve.base` / `.exponent`). Account XP = Job XP × the Job's Account Rate. The Account Rate lives in the Job definition (Java).
- Player Capacity = 5 + 1 per Account Level, capped at 40 (Account Level 35, ~120 h at 2,500 Account XP/h).

## Tools & armor
- Start like vanilla: players spawn with nothing and craft wooden tools.
- Vanilla wooden tools craftable, no Capacity, no bonuses.
- Vanilla stone, iron, gold, diamond and netherite tools and weapons: locked forever (found ones are unusable), can be salvaged.
- All vanilla armor (leather, chainmail, iron, gold, diamond, netherite) is Locked Gear: unusable, salvageable. New players have no armor until their first Rune Armor.
- Rune Armor: named pieces with Rarity, Trait, Rune slots, Capacity — the armor counterpart of Rune Tools. Protection follows vanilla by Rarity (Common ≈ leather … Legendary ≈ netherite), overridable per piece.
- Rune Tools: many named tools per Rarity, each with a Trait. Slots + Capacity grow with Rarity.
- Tool base stats follow vanilla by Harvest Tier (stone / iron / diamond); Epic and Legendary get gold speed (12) and netherite durability (2,031). Overridable per tool; Relics may exceed them. Axes keep vanilla axe damage (balance vs swords revisited with Combat).
- Harvest Tier: defaults from Rarity (Common stone, Uncommon iron, Rare+ diamond); a tool can be one tier above or below as a bonus/drawback.
- Traits can be a bonus, a drawback, or both (e.g. Coal Pickaxe: faster, less durable).
- Upgrade: most gear sits in Upgrade lines — the recipe of the next piece consumes the previous one (or several, e.g. 5 biome pickaxes → Worldpick, 5 essence axes → Yggdrasil). The new piece has a new, cooler name and look, and keeps the installed Runes that still fit; the rest go to the inventory or drop if it is full. Standalone pieces are allowed too. "All biomes" pieces are craftable Legendaries, not Relics.
- Evolve (same item, next Rarity) stays for a few special pieces. Each defines its own Evolution condition. Evolution can also cost vanilla XP.
- Relics: uncraftable Legendaries, allowed to be broken-strong. Extremely rare in the chests of any structure that fits their Job (surface or underground: Ancient City, Trial Chambers, Woodland Mansion, Abandoned Camps, buried treasure…), or ultra-rare rewards of Legendary-tier Quests of their Job; each has its own loot tables; only drop for players at Job Level 25+. Common structures get much lower odds per chest.
- Fused Relics: two Relics of the same Job + outrageously expensive materials (e.g. 2 Legendary Cores, 4 netherite blocks, 2 beacons, heavy core, enchanted golden apple) → one stronger Relic. No Blueprint needed. A Relic can only be Upgraded into a Fused Relic; Fused Relics cannot be Upgraded further. Applies to every Job.
- Blueprints unlock craftable Legendary recipes; each Blueprint has its own source (level milestone, structure chest, vault, Quest).
- Epic recipes are very expensive; Legendary recipes are extreme (beacons, netherite blocks, Legendary Core).
- Components: shared crafted intermediates (Reinforced Handle, Reinforced Lining, Gem Setting, Echo Lens, Deep Core, Abyssal Plate, Legendary Core). Reuse them for every Job's tools and armor.
- Starter Recipes: every player knows cheap Common Rune Tool and Rune Armor recipes from the start, made only from early materials. Mining and Woodcutting have none: their first Rune tools unlock at Level 2.
- Better gear: recipes unlocked by Job Level, with themed ingredients.
- Tools come from their Job (Mining → pickaxes, Excavation → shovels, Woodcutting → axes, Combat → swords…).
- Armor is themed per Job: any Job can unlock armor pieces that fit it, each set with its own scaling mechanic (Mining: depth; Woodcutting: Rhythm, charges from Felled Trees). Armor sets become "builds" for a Job. Smithing still earns XP from crafting and repairing all gear.
- Rare ingredients: custom ones from quests and rare drops, plus underused vanilla items (rabbit's foot, phantom membrane, nautilus shell…). Some Runes/tools boost ingredient drops.
- Durability: at 0 → Broken (keeps Runes), repaired at anvil (costs vanilla XP, like vanilla), grants Smithing XP.

## Enchanting & vanilla XP
- Vanilla enchanting is removed entirely (enchanting table, enchanted books, anvil enchant combining).
- Enchanted books in loot (chests, fishing, trades) are replaced by Runes / Rune Caches.
- Smithing no longer covers enchanting.
- Vanilla XP stays and is spent on: anvil repairs of Rune gear, Rune rank-ups, and (optionally, per piece) Evolution.
- Vanilla Overrides, first slice (2026-11-11, package `com.alfgoto.rune.overrides`): enchanting table and enchant events are cancelled; the anvil result is removed when an enchanted book is involved or the enchantments change (repairs and renames stay). Locked Gear is matched by material name (stone, iron, gold, diamond, netherite tools and weapons; leather, chainmail, iron, gold, diamond, netherite armor). It can't be crafted (crafting table, smithing table), can't mine, attack or be right-clicked with, and armor is taken off into the inventory (or dropped). No message is shown yet. Salvage and enchanted books in loot come in later tickets. Rune gear built on a vanilla material will need a marker so it is not locked (decided with the `RuneItem` base).

## Trading
- Runes, Rune Tools and Rune Armor are freely tradeable; no Job Level requirement to use them. Good gear is meant to be very rare, so trading is fine.
- Later: a config option to restrict trading.

## Anti-exploit
- Player-placed blocks give 0 Job XP in every Job (placed blocks are tracked). This also covers silk-touched ores placed back down.
- Blocks created by fluids (cobblestone / cobbled deepslate / stone / basalt generators) give 0 XP. Exception: obsidian (costs a lava source).
- Blocks only count when the player breaks them (tool, tool effect, hand); TNT, creepers and natural decay (leaves) give 0.
- Grown blocks (trees, giant mushrooms, huge fungi, chorus, spreading plants…) are tagged when they grow. Grown over time: full XP. Grown by bone meal (player or dispenser), or by an instant-regrowth effect: ×0.25 XP and ×0.25 Mastery. One rule for every Job.
- Blocks that spread on their own (vines, glow lichen) keep tiny base XP so farms are pointless.
- Filler blocks' XP follows hardness, scaled ×0.2 (issue #141): netherrack 0.05, stone 0.2, deepslate 0.3, end stone 0.4.
- Diminishing returns on farms: spawner/grinder mobs and repeated kills in a small area give less XP.
- Quest objectives that could be looped count results, not actions (e.g. "grow 10 trees" counts successful growths, not saplings planted).
- AFK detection: maybe later.

## Death
- Player Runes kept. Rune Tools, Rune Armor and loose Rune items drop like normal items (Runes installed in gear stay inside it).

## Quests
- Exactly 3 active, each from a random eligible Job; completing one draws a new one.
- A Quest = a Quest Type (per Job: Collect, Place, Depth, Tool, Variety, Reforestation…) × a Quest Tier. Code: one generic abstraction for every Job — Jobs only declare their Quest Types (objective + base size); Tiers, weighting and rewards are shared.
- Quest Tiers = the 5 Rarities (same colours). The Tier multiplies the objective (×1 / ×1.5 / ×2 / ×3 / ×5, config: `quests.tier-multipliers`) and improves the reward odds.
- Tier draw is weighted by the player's Job Level: the higher the level, the more the odds lean towards hard Tiers. Hard gates: Epic needs Job Level 15, Legendary needs 25.
- Eligibility: min Job Level 1 for gathering Jobs, 2 for crafting-type Jobs.
- No automatic reset. 1 Reroll Token per world day, max 3 held.
- Quest core (2026-11-13, package `gameplay.quests`): `QuestsModule` provides an empty `QuestTypeRegistry` and the `QuestBook`; `content.quests.CoreQuestsModule` fills it (for now one `test` Quest Type: Mining, base size 10). Active Quests are stored per player under `quest/slot/1..3` as `<type>:<tier>`; empty slots are filled when read and when the player joins. A random eligible Job is drawn, then one of its Quest Types, then a Tier. Tier weight = `max(0, base + per-level × Job Level)`, 0 below the Tier's min level (config: `quests.tier-min-levels` = 1/1/1/15/25, `quests.tier-weights.base` = 100/60/30/10/5, `quests.tier-weights.per-level` = -1.2/0/0.6/0.5/0.2; decided by Alfred 2026-11-14 as starting values, tuned after playtests). Rough odds, Common/Uncommon/Rare/Epic/Legendary: Job Level 1 = 52/32/16/–/–, 15 = 41/30/20/9/–, 25 = 34/29/22/11/5, 50 = 19/29/29/17/7. A Job's `questMinLevel` is its Quest Eligibility (default 1). Target = base size × Tier multiplier, rounded up. The `test` Quest Type on Mining is approved (easiest way to test). Commands: `/rune quest list`; test tool `/rune debug quest set <slot> <type> <tier>` (ignores gates, ADR 0004). Completion and rewards come in later tickets.
- Reroll Tokens (2026-11-14, `gameplay.quests.RerollTokens`): one earned per world day (main world full time / 24000), at most `quests.max-reroll-tokens` (default 3) held. Counted lazily from the stored day (`quest/reroll/tokens`, `quest/reroll/day`), so days spent offline count too. A new player starts with 0 (decided by Alfred 2026-11-14). Setting the time back earns nothing. `/rune quest reroll <slot>` spends one and draws a new Quest for the slot with the normal draw (it can give the same Quest Type or Tier again). No token or no eligible Quest: nothing is spent. Reroll Tokens as a Quest reward come with the Quest rewards ticket.
- Rewards: Job XP + Account XP bonus + a roll on a loot table (Runes, rare ingredients, Reroll Tokens, very rarely Blueprints). Relics of that Job only from Legendary-tier Quests, ultra-rarely. Quest Tier sets the odds.

## Rune Caches (gacha)
- Rune drops and quest loot come as sealed Rune Caches, never opened automatically.
- Right-click a Cache to open it: spinning reel menu, slows down, rarity-coloured flash, sounds (firework for Legendary).
- Every Cache rolls the same loot table; its Rarity (shown by its colour) only shifts the odds and the quantity. A Legendary Cache does not guarantee a Legendary Rune (it may give several Epics instead).
- Job Caches lean towards that Job's Runes (60% that Job's Runes, 40% general passive Runes).
- Drop chance from Job Actions scales with the action's XP (`XP × 0.05%`, same for every gathering Job so far).

## Unlock Tree (2026-11-14)
- Job unlocks are shown in the vanilla advancement screen (`L` key), not in a custom menu. Players already know it, and the tree layout, zoom, scrolling and unlock pop-ups come for free.
- One tab for every Job. Each Job is one horizontal line: the Job's icon on the left, then one node per Job Level that unlocks something (recipe, Blueprint, Milestone Cache), from left to right. The lines are parallel, one under the other, so a player sees all their Jobs side by side.
- A Job Level that unlocks nothing gets no node. Only registered Jobs get a line (Step 1: Mining and Woodcutting).
- Node: icon = what it unlocks (the tool's model, the Cache in its Rarity colour), title = Job + level (e.g. "Mining 12"), description = what it unlocks. Frame: task for recipes, goal for Milestone Caches, challenge for Blueprints.
- Display only (ADR 0003: Visuals never decide). Job Level is the source of truth. The tree is built from the Job definitions and their unlock data. Each player's nodes are re-synced from their Job Levels on join and on every level change, including `/rune level set` going down. Vanilla's saved advancement progress never decides anything.
- Reaching a level shows the vanilla pop-up in the corner for each new node. No chat announcement (the level-up title already exists, see `docs/feedback.md`).
- All text uses translation keys (EN + FR, English fallback), like every other text.
- Vanilla's own advancement tabs stay.
- The screen can't be clicked to choose things. If Jobs ever offer choices, a chest menu does the choosing and the Unlock Tree stays the map.
- The Rune Codex Jobs page keeps levels and XP bars; it does not copy the tree.
- **How the tab is registered:** at runtime with Paper's `loadAdvancement` (in memory, not persisted). It is rebuilt from the Job registries on every plugin enable.
  - Why: simplest option. Same display as a generated datapack. No files in the world folder, no datapack reload, no extra dependency.
  - Risk: the API is `@Deprecated` in `UnsafeValues`. Keep the JSON building separate from the loading. Then switching to a generated datapack (tested, works) only changes the loading step.
  - Not chosen: a library such as UltimateAdvancementAPI (extra dependency, not needed).
- **Reveal of locked nodes:** keep vanilla behaviour. Only the next few nodes after the last unlocked one are shown.
- Tested in game (prototype, since deleted): parallel lines per Job, toast without chat line, revoke works.

## Scope & delivery
- Infrastructure comes before any feature: repo scaffold, Gradle build + `runServer`, CI with tests, Claude PR review (same setup as FloconAI), `/preview` servers, Renovate.
- The target is the full product described here, not a cut-down version.
- Breaking it into small tasks (from nothing to full product) happens later, in a separate planning step. Intermediate milestones are a by-product of that order, not a reduced scope.
- Manual setup tasks become the first issues when issues are created:
  - Add the Claude PR review secret (same one as `flocon-ai/flocon-ai`) to the Rune repo.
  - Create the playit.gg account + one fixed game tunnel and add the secret to the Rune repo.

### Step 1 (decided 2026-11-11)
The first milestone the issues aim at:
- All infrastructure (scaffold, build, `runServer`, CI + tests, Claude review, `/preview`, Renovate) and the two manual setup tasks.
- The four core systems (Jobs, Items, Effects, Rewards — ADR 0002), plus player data storage, Account XP/Level and Capacity.
- Mining and Woodcutting with their full content as written in `docs/jobs/mining.md` and `docs/jobs/woodcutting.md`.
- The full Quest system (3 active, Quest Tiers, rewards, Reroll Tokens) with the Mining and Woodcutting Quest Types.
- Every visual those need (Rune Codex menus, resource pack with item models, Cache opening, XP feedback…). Visuals can be rough in step 1; a proper polish pass comes later.

## Items
- `RuneItem` base (2026-11-14, `gameplay.items`): a `RuneItem` has an id, a Rarity, a vanilla material and a model (the name of an item definition in the `rune` namespace of the resource pack). `ItemRegistry` (a service of `ItemsModule`) holds them, builds stacks (`create`) and reads the id back (`of`). The id is stored in the item's PDC under `rune:item_id`, so it survives restarts. The model is set with the `item_model` component; the name is the translation key `rune.item.<id>` coloured with the Rarity colour. Lore and slots come with Item Rendering. Content modules fill the registry (`content.items.CoreItemsModule` registers one test item, `test_item`, on the `test_rune` model).
- Item Rendering (2026-11-15, `visuals.itemrendering`): `ItemRegistry` no longer builds the name and lore. It calls its `ItemRedrawer`s on `create`, `update(stack, change)` and `redraw(stack)`; `ItemRenderingModule` registers the `ItemRenderer`. Name: translation key in the Rarity colour. Lore: the translated Rarity name, then a stats placeholder line ("No stats yet", replaced when items get stats). Data change → `update` → lore rebuilt from scratch. Lang file: `item_rendering.json`.
- Rune item type (2026-11-15, `gameplay.runes`): a `Rune` is a `RuneItem` (so it has the Rarity) + an `EffectBinding` (value per Rank, optional Max Rank) + the `Fit`s it can go in. `Fit` lists the places named in these docs: player, pickaxe, axe, shovel, sword, bow, crossbow, trident, fishing rod, shield, helmet, chestplate, leggings, boots (not to be confused with a Place, a biome or structure). `RuneRegistry` (a service of `RunesModule`) holds the Runes, registers their items, and builds stacks with `create(rune, rank)`. The Rank is stored on each stack in its PDC under `rune:rank` (1 if missing). The Max Rank is checked when a stack is created, never at run time. Capacity Cost = Rarity base + 1 per Rank above 1 (see Capacity); the numbers are in the config (`capacity.rune-cost.base`, one per Rarity, and `capacity.rune-cost.per-rank`). Lore of a Rune: Rarity, Rank (`Rank: 2 / 4`, or `Rank: 2` with no Max Rank), Capacity Cost, Fits. Content: `content.runes.CoreRunesModule` registers one test Rune, `test_rune` (Rare, Max Rank 4, fits player and pickaxe, an Effect that does nothing).
- Tool Loadout (2026-11-15, #33, `gameplay.runes.ToolLoadout`): the Runes installed on a Rune Tool are stored on the tool's PDC under `rune:loadout` (`id:rank;id:rank`), so they travel with it. Slots are positions from 1; uninstalling moves the next Runes up. An install is refused when the stack is not a Rune Tool, the Rune does not fit the tool kind (pickaxe, axe, shovel), the Rank is not allowed, no slot is free, or Capacity Cost would pass the tool's Capacity (base Capacity from `ToolStats`; Mastery and Trait Capacity are not built yet). `sources(tool)` gives the installed Runes as `Stacking.Source`s and `total(tool, effectId)` the stacked value: this is what feeds the Effect system (the Trait is not in the list until Traits exist). Debug commands on the held tool: `/rune debug install <rune> [rank]` (does not consume a Rune item) and `/rune debug uninstall <slot>`. Not built: using an item Rune from the inventory, the loadout menu, lore showing installed Runes, Player and Armor Loadouts.
- `/rune give rune <id> [rank]` (2026-11-15): gives one Rune at a Rank (default 1; must be allowed by its Max Rank). It is a variant of `/rune give`: `GiveSubcommand.addVariant`, so an item type with extra data can add its own form. A plain item with the id `rune` could not be given by `/rune give rune`.
- Rune Tool item type (2026-11-15, #30, `gameplay.items`): a `RuneItem` with a `ToolSpec` is a Rune Tool (`RuneItem.tool(...)`). It is built on the vanilla wooden tool of its `ToolKind` (pickaxe, axe, shovel; wooden tools are not Locked Gear). `ItemRegistry.create` sets vanilla components from `ToolStats`: the `tool` component (same two rules as vanilla: blocks the Harvest Tier cannot harvest give no drops, then the kind's blocks mine at the tool's speed) and `max_damage`. So the game itself mines at that tier. Note: in vanilla, iron tier already mines diamond ore; obsidian and ancient debris need diamond tier.
- Rune Tool stats: Harvest Tier = default from Rarity, shifted by `tierOffset` (-1, 0, +1; outside stone..diamond is refused). Speed and durability follow the tier (config `tools.speed-by-tier` 4/6/8, `tools.durability-by-tier` 131/250/1561); Epic and Legendary use `tools.epic-speed` 12 and `tools.epic-durability` 2031 whatever their tier. Slots and Capacity follow Rarity (`tools.slots-by-rarity` 1–5, `tools.capacity-by-rarity` 3/6/10/14/18). Each of speed, durability, slots and Capacity can be overridden per tool in its `ToolSpec`. Lore (Item Rendering) shows Harvest Tier, Rune slots, Capacity and Trait.
- Not decided yet: how a Trait is built. For now a `ToolSpec` only keeps an optional Trait id (shown in the lore as `rune.trait.<id>`); no Trait does anything. Mastery, installed Runes and Broken are not built. First test content: `test_pickaxe` (Common, stone tier).
- `/rune give <id> [amount]` (2026-11-14): gives the item to yourself (player only), with tab completion on ids. Amount 1 to 2304 (a full inventory); it is split into stacks and what does not fit drops at your feet.

## Crafting
- Recipe system (2026-11-15, `gameplay.crafting`): a `CraftingRecipe` is a shaped recipe (1–3 rows of 1–3 cells) with a Job, a Job Level, a result Rune item and an amount. Each ingredient is a vanilla material or a Rune item id (Components, previous gear). `RecipeBook` (a service of `CraftingModule`) holds them and registers each one as a server recipe; content modules fill it (`content.crafting.CoreCraftingModule` registers one test recipe: 4 cobblestone → `test_item`, Mining level 2). The result is built by `ItemRegistry.create`, so a crafted item is a real Rune item.
- Locked recipes (Job Level is the source of truth): while the player's Job Level is under the recipe's level, the craft result is removed and a craft click is cancelled. This also stops the recipe from being crafted from a recipe-book click.
- Unlock: `JobProgress` now tells `JobLevelListener`s on every Job Level change (XP gains, `/rune xp set`, `/rune level set`, up or down). Crafting listens and syncs the player's vanilla recipe book (discover unlocked recipes, forget the others). It also syncs on join. Rune ingredients are matched exactly (`RecipeChoice.ExactChoice`); this will be revisited when items get Ranks and lore.
- Components (2026-11-15, `content.items` / `content.crafting`): the 7 Components of `jobs/mining.md` are registered as items and recipes (placeholder textures, EN/FR names). Ingredients are exactly the documented ones. Proposed by Claude, to confirm: Rarity (Handle and Lining Uncommon, Gem Setting and Echo Lens Rare, Deep Core and Abyssal Plate Epic, Legendary Core Legendary, from the "Used by" column), unlock level (Mining 1, no lock), grid layouts (shaped, since shapeless is not built), wool = white wool. Known risk, to confirm: Components use Paper as base material, so a vanilla Map or Book recipe accepts them as plain paper (Rune recipes are safe, they match the Rune id). Needs a base material that no vanilla recipe uses; not picked yet.
- Not decided yet: Upgrade (Runes kept on the result), Blueprints, Evolve, Starter Recipes, shapeless recipes.

## Places
- Places (2026-11-11): a Place is a biome or structure, matched by Minecraft key (framework and `PlacesModule` in `gameplay.places`, which provides an empty `PlaceRegistry`; `content.places.CorePlacesModule` fills it). `PlaceDetector` caches the result per player and recomputes only when the player changes block. Structures match on their pieces, not the whole box. First Places: Lush Caves, Taiga (taiga + snowy taiga), Mineshaft (incl. badlands). Command: `/rune where`. Other Places named in the Job docs (Dripstone, Deep Dark, Mountains, Jungle) are added with their Runes/Quests.

- Menu framework (2026-11-15, package `visuals.menu`): `Menu` (chest menu, optional parent), `PagedMenu`, `Button` (icon + click action). Every click and drag is cancelled while a menu is open, in the menu and in the player's inventory, so nothing can be taken out. A menu with a parent shows a Back arrow in the middle of the bottom row; paged menus add Previous/Next arrows beside it, only when they lead somewhere. Test: `/rune debug menu`. Menus are built by later tickets (Rune Codex, Jobs, Quests, loadout, Cache reel).

## Later (not at launch)
- Transmuting at a Rune Altar (custom block).
- Full content lists for the other Jobs (tools, armor, Runes, quests, Evolution conditions, Salvage yields) — one `docs/jobs/<job>.md` each.
- Challenge-type Quests (conditions like "without placing torches"; timed).
- Config option to restrict trading of Runes / Rune gear.
- PvP: not considered yet.
