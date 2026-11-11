# Design decisions (grilling session, 2026-11-08)

Working log of settled decisions. Terms are defined in `CONTEXT.md`.

## Platform & workflow
- Paper plugin + server resource pack, latest Minecraft (see ADR 0001). Minimum 26.3 (Poplar trees, Abandoned Camps are used by Woodcutting).
- Language: Java. Base package: `com.alfgoto.rune`.
- License: MIT.
- Resource pack served by the plugin itself (built-in HTTP server, one extra port). A config option can point to an external URL instead. `/preview` needs a second playit.gg tunnel for the pack port.
- Minecraft version in one build property; Renovate opens version-bump PRs.
- Local test: `./gradlew runServer`, join `localhost` with a vanilla client.
- CI: build + MockBukkit tests + Claude PR review, copying the FloconAI repo's setup (same auth method).
- Player-facing text: English (default) + French, all text (including every item name) in language files with English as fallback, so server owners can add more languages.
- Translations ship in the resource pack (2026-11-11): items, menus and messages use translatable components, and each client shows them in its own language. Every translatable component carries the English text as fallback, so a missing key never shows a raw key.
- Text helpers (`com.alfgoto.rune.text`): translation keys are `rune.<category>.<id>`; language files live in `src/main/resources/lang/<language>/<module>.json`, one file per module (ADR 0004), and every language must have the same keys. Rarity colours live only in the `Rarity` enum: Common white, Uncommon green, Rare aqua, Epic light purple, Legendary gold (vanilla named colours, may change in the visual polish pass).
- Admin commands (2026-11-11): one root `/rune` command, permission `rune.admin` (default op). Each subcommand implements `Subcommand` and is registered by its module with `ctx.subcommand(...)`; debug tools go under `/rune debug <name>` with `ctx.debug(...)` (ADR 0004). Messages are translatable components with English fallback; keys live in the module's lang files. First subcommand: `/rune version`.
- Resource pack build (2026-11-11): `ResourcePackBuilder` (`visuals.resourcepack`) zips `pack.mcmeta`, the assets listed in `src/main/resources/pack/files.txt` (add every new asset there) and the plugin lang files (merged per language) at startup into `plugins/Rune/rune-pack.zip`. Deterministic zip, SHA-1 logged. Serving and sending it are separate tasks. The pack format is a constant for now (open point in `docs/research/resource-pack.md`).
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
- Per-Job content lives in `docs/jobs/<job>.md` (Mining done 2026-11-10, Woodcutting done 2026-11-11).

## Runes
- Runes are physical items.
- 5 Rarities: Common, Uncommon, Rare, Epic, Legendary (Legendary = game-changing, e.g. Veinminer).
- Rank up: 3 identical Runes of rank N at a smithing table → rank N+1, costs vanilla XP. (Prototype needed: smithing table slot restrictions.)
- Max Rank is set per Rune, by what makes sense for its effect. Some Runes have no Max Rank (e.g. Deepstride); the 3× cost per Rank caps them naturally. Defined in each Rune's definition.
- Variants (Affinity: <place>, Specialist: <ore>, Specialist: <wood>, Sanctuary: <biome>) are separate Runes: they rank up separately and stack with each other. One effect class with a parameter. Variants can appear in their place's chest loot.
- Stacking: the same effect from a Trait and/or several Runes adds up. Every effect is a number (on/off effects become chances). Chances above 100% roll again (e.g. Smelt 150% → always one smelt, 50% chance of a second: cobblestone → stone → smooth stone). Exception: Silk Touch (on/off) disables Fortune, Smelt and Ore Bloom.
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

## Trading
- Runes, Rune Tools and Rune Armor are freely tradeable; no Job Level requirement to use them. Good gear is meant to be very rare, so trading is fine.
- Later: a config option to restrict trading.

## Anti-exploit
- Player-placed blocks give 0 Job XP in every Job (placed blocks are tracked). This also covers silk-touched ores placed back down.
- Blocks created by fluids (cobblestone / stone / basalt generators) give 0 XP. Exception: obsidian (costs a lava source).
- Blocks only count when the player breaks them (tool, tool effect, hand); TNT, creepers and natural decay (leaves) give 0.
- Grown blocks (trees, giant mushrooms, huge fungi, chorus, spreading plants…) are tagged when they grow. Grown over time: full XP. Grown by bone meal (player or dispenser), or by an instant-regrowth effect: ×0.25 XP and ×0.25 Mastery. One rule for every Job.
- Blocks that spread on their own (vines, glow lichen) keep tiny base XP so farms are pointless.
- Filler blocks' XP follows hardness (netherrack 0.25, stone 1, deepslate 1.5, end stone 2).
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
- Rewards: Job XP + Account XP bonus + a roll on a loot table (Runes, rare ingredients, Reroll Tokens, very rarely Blueprints). Relics of that Job only from Legendary-tier Quests, ultra-rarely. Quest Tier sets the odds.

## Rune Caches (gacha)
- Rune drops and quest loot come as sealed Rune Caches, never opened automatically.
- Right-click a Cache to open it: spinning reel menu, slows down, rarity-coloured flash, sounds (firework for Legendary).
- Every Cache rolls the same loot table; its Rarity (shown by its colour) only shifts the odds and the quantity. A Legendary Cache does not guarantee a Legendary Rune (it may give several Epics instead).
- Job Caches lean towards that Job's Runes (60% that Job's Runes, 40% general passive Runes).
- Drop chance from Job Actions scales with the action's XP (`XP × 0.05%`, same for every gathering Job so far).

## Scope & delivery
- Infrastructure comes before any feature: repo scaffold, Gradle build + `runServer`, CI with tests, Claude PR review (same setup as FloconAI), `/preview` servers, Renovate.
- The target is the full product described here, not a cut-down version.
- Breaking it into small tasks (from nothing to full product) happens later, in a separate planning step. Intermediate milestones are a by-product of that order, not a reduced scope.
- Manual setup tasks become the first issues when issues are created:
  - Add the Claude PR review secret (same one as `flocon-ai/flocon-ai`) to the Rune repo.
  - Create the playit.gg account + two fixed tunnels (game port, resource pack port) and add the secret to the Rune repo.

### Step 1 (decided 2026-11-11)
The first milestone the issues aim at:
- All infrastructure (scaffold, build, `runServer`, CI + tests, Claude review, `/preview`, Renovate) and the two manual setup tasks.
- The four core systems (Jobs, Items, Effects, Rewards — ADR 0002), plus player data storage, Account XP/Level and Capacity.
- Mining and Woodcutting with their full content as written in `docs/jobs/mining.md` and `docs/jobs/woodcutting.md`.
- The full Quest system (3 active, Quest Tiers, rewards, Reroll Tokens) with the Mining and Woodcutting Quest Types.
- Every visual those need (Rune Codex menus, resource pack with item models, Cache opening, XP feedback…). Visuals can be rough in step 1; a proper polish pass comes later.

## Later (not at launch)
- Transmuting at a Rune Altar (custom block).
- Full content lists for the other Jobs (tools, armor, Runes, quests, Evolution conditions, Salvage yields) — one `docs/jobs/<job>.md` each.
- Challenge-type Quests (conditions like "without placing torches"; timed).
- Config option to restrict trading of Runes / Rune gear.
- PvP: not considered yet.
