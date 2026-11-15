# Rewards: loot tables

Decided 2026-11-15 (issue #41). Builds on ADR 0002 and `design-decisions.md` ("Every Cache rolls the same loot table").

## Architecture
`gameplay.rewards` is a small engine. **It knows no reward kind**, so there is no cycle: Jobs (milestone Caches) and Quests (rewards) use Rewards, and Rewards imports neither.

- `RewardsModule` provides an empty `LootTableRegistry` and a `LootRoller`, and adds `/rune loot`.
- `Reward`: `available(RollContext)` and `roll(RollContext, RandomGenerator)` → `Drop`.
- `Drop`: `describe()` (a `Component`) and `give(Player)`.
- `LootTable`: Java builder, a list of entries `(Reward, base weight, Rarity of the entry)`.
- `RollContext`: player UUID, the source Rarity (Cache Rarity or Quest Tier) and an optional Job **id** (an id, so Rewards does not import Jobs).
- **Rolling is pure.** It returns a `List<Drop>` and gives nothing. The caller decides when to call `give` (the Cache reel and Quest rewards, later). The `RandomGenerator` is passed in, so tests use a seed.
- Each reward kind lives in the module that owns the thing:

| Reward | Package | Notes |
|---|---|---|
| `RuneReward` | `gameplay.runes` | One Rune (Rank 1) of the entry's Rarity |
| `ItemReward` | `gameplay.items` | An item id, min to max count |
| `JobXpReward` | `gameplay.jobs` | Needs a Job in the context, else not available |
| `AccountXpReward` | `gameplay.jobs` | Uses `JobProgress.addAccountXpBonus` (no Job XP behind it) |
| `RerollTokenReward` | `gameplay.quests` | Uses `RerollTokens.grant`, capped at the max held |

- Blueprint: not now. Ticket #56 adds a `BlueprintReward` the same way.
- Tables are filled by a Content module: `content.rewards.CoreLootModule`.

## How a Rarity shifts the odds
Weight of an entry = `base weight × boost ^ (entry Rarity index)`. Common = 0 … Legendary = 4.

- `boost` comes from the source Rarity: config `rewards.rarity-boost` = **1 / 1.5 / 2 / 3 / 4** (Common → Legendary).
- An entry whose reward is not available has weight 0.

With Rune base weights 1000 / 400 / 150 / 40 / 5, the Rune Rarity odds (%) are:

| Source | C | U | R | E | L |
|---|---|---|---|---|---|
| Common | 63 | 25 | 9 | 3 | 0.3 |
| Uncommon | 48 | 29 | 16 | 6 | 1 |
| Rare | 36 | 29 | 21 | 11 | 3 |
| Epic | 20 | 24 | 27 | 21 | 8 |
| Legendary | 11 | 18 | 27 | 29 | 14 |

`LootRollerTest` checks this table (seeded, ±1%).

## Quantity
Number of rolls per open: config `rewards.rolls-by-rarity` = **1 / 1 / 2 / 2 / 3**. Three rolls at 29% Epic is how a Legendary Cache gives several Epic Runes.

## Job bias (60 / 40)
- A Rune definition can have an optional Job (`Rune.job()`). No Job means a general Rune.
- `RuneReward` with a Job in the context picks that Job's Runes with chance config `rewards.job-bias` = **0.6**, otherwise general Runes. Without a Job it only picks general Runes. Runes of another Job never drop.
- If the chosen pool has no Rune of that Rarity, it uses the other pool. If both are empty, it steps down one Rarity and tries again. If it finds nothing at all, the entry is not available.

## First table: `cache`

| Entry | Base weight | Entry Rarity |
|---|---|---|
| Rune, Common | 1000 | Common |
| Rune, Uncommon | 400 | Uncommon |
| Rune, Rare | 150 | Rare |
| Rune, Epic | 40 | Epic |
| Rune, Legendary | 5 | Legendary |
| Job XP 100 | 150 | Common |
| Account XP 100 | 100 | Common |
| Reroll Token ×1 | 60 | Uncommon |

- Base weights and amounts live in Java (content). Only the 3 tuning numbers go in `config.yml`.
- Rare ingredients come later, with the tickets that define them. For now only `test_rune` exists, so every Rune entry gives it.

## Command
`/rune loot roll <table> [rarity] [job]`: prints each Drop and **gives** them to the sender. Default Rarity: Common. Default Job: none. An unknown Job id gives no Job-bound result (Rewards cannot check Job ids).
