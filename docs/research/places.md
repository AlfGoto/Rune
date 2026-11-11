# Research — detecting Structures and biomes with the Paper API

Issue #49. Researched 2026-11-11 against Paper API **26.3** javadocs (`jd.papermc.io`) and the Paper server source (`CraftWorld`, `CraftRegionAccessor`). No NMS needed.

This is research, not a decision. Nothing here is built. The "Proposal" parts need Alfred's OK before they go in `design-decisions.md`.

## Answer first

- **Which biome?** `Location#getBiome()` / `World#getBiome(x, y, z)`. Very cheap.
- **In a Structure?** `World#hasStructureAt(Position, Structure)`. One lookup, no chunk loading. Cheap enough to run on a block break.
- **Which Structure?** `World#getStructures(chunkX, chunkZ)` returns `GeneratedStructure` objects (type + bounding box + pieces).
- **Which Structure owns a loot chest?** `LootGenerateEvent#getLootTable().getKey()`. The key names the Structure (`chests/ancient_city`…).
- **Trial Vaults are different:** they fire `BlockDispenseLootEvent`, not `LootGenerateEvent`.

## Needs per design doc

| Need | Source | Section below |
|---|---|---|
| Mining Quest "mine 50 blocks in a Mineshaft" (Place type) | `docs/jobs/mining.md` | Structures |
| Woodcutting Quest "cut 40 logs in a Dark Forest" (Place type) | `docs/jobs/woodcutting.md` | Biomes |
| Affinity: <place> Runes (biome and Structure variants) | both Jobs | Biomes, Structures |
| Sanctuary: <biome> Runes | `woodcutting.md` | Biomes |
| Relics and Blueprints from chests (Ancient City, Trial Chambers, Woodland Mansion, Abandoned Camp) | both Jobs | Loot chests |

## Biomes

**Verified in javadoc**
- `RegionAccessor#getBiome(Location)` and `#getBiome(int x, int y, int z)` return the **noise biome**: one biome per 4×4×4 block cell.
- `RegionAccessor#getComputedBiome(int x, int y, int z)` returns what the client shows in F3 (blends neighbouring cells). Heavier.
- `World#getBiome(int x, int z)` is **deprecated** (biomes are 3D). Always pass Y.

**Verified in Paper source**
- `getBiome` is `getNoiseBiome(x >> 2, y >> 2, z >> 2)`: a lookup in already-loaded data. No block reads.

**Cost:** negligible. Fine to call on every block break, or once per second for Sanctuary.

**Use**
- Affinity and Sanctuary: use `getBiome` (noise biome). Edge accuracy of 4 blocks is fine for a +10% speed.
- Use `getComputedBiome` only if a border must match F3 exactly.
- Compare by biome **key**, not by Java constant, so the config can list biomes as strings (`minecraft:dark_forest`). Biome is a registry type now. The exact key accessor was **not verified**: check `Registry.BIOME.getKey(biome)` when coding.

**Not needed:** `World#locateNearestBiome` searches far and is expensive. Do not use it for effects.

## Structures

**Verified in javadoc**
- `World#hasStructureAt(Position, Structure)`: "whether the naturally generated structure exists at the position".
- `World#getStructures(int chunkX, int chunkZ)` and `#getStructures(int, int, Structure)`: generated structures that intersect that chunk. Chunk coordinates, not block coordinates.
- `GeneratedStructure`: `getStructure()`, `getBoundingBox()`, `getPieces()`. Also a `PersistentDataHolder`.
- `StructurePiece#getBoundingBox()`.
- `World#locateNearestStructure(Location, Structure, radius, findUnexplored)`: returns `StructureSearchResult`. The overload with the old `org.bukkit.StructureType` is deprecated.

**Verified in Paper source**
- `hasStructureAt` calls `structureManager().getStructureWithPieceAt(...)`. **One lookup, piece-level**, not just the big bounding box.
- `getStructures` only reads the chunk's stored structure starts. **It requests no chunk status and loads/generates nothing.**
- `locateNearestStructure` runs the vanilla search. Heavy. Not for per-block use.

### Structure constants (Paper 26.3)

| Our term | Paper constant(s) |
|---|---|
| Mineshaft | `Structure.MINESHAFT` **and** `Structure.MINESHAFT_MESA` (two constants!) |
| Ancient City | `Structure.ANCIENT_CITY` |
| Woodland Mansion | `Structure.MANSION` |
| Trial Chambers | `Structure.TRIAL_CHAMBERS` |
| Abandoned Camp | **18 biome variants**, `Structure.ABANDONED_CAMP_FOREST`, `_PALE_GARDEN`, `_DAPPLED_FOREST`, `_SWAMP`… (full list in the Structure javadoc) |

- **Rune design note:** an "Affinity: Mineshaft" or "Affinity: Abandoned Camp" Rune must match a **set** of constants, not one.
- `Structure#getKey()` is deprecated for removal. Use `Registry.STRUCTURE.getKey(structure)` (or `RegistryKey.STRUCTURE`). Not tested.

### Whole box or only the pieces?

- `GeneratedStructure#getBoundingBox()` is a big box. It includes plain stone around a Mineshaft or a Mansion.
- `hasStructureAt` and `getPieces()` are tighter.
- Ancient City and Trial Chambers are big underground boxes. A player digging beside one may be inside the box but not near a piece.
- **Proposal:** use `hasStructureAt` for "inside". It is both cheaper and tighter. Decide with Alfred if "near" should count.

### Cost per block break

- `hasStructureAt`: one piece lookup. Cheap.
- To save more: cache per player, keyed by chunk (or by 16×16×16 section). Structures never change after generation, so the cache is never stale. Only re-check when the player breaks a block in another chunk.
- Only check when something needs it: an active "Place" Quest of that Structure, or an installed Affinity Rune for a Structure. Most breaks need no check.
- **Not measured.** No benchmark was run. If it matters later, add a micro-benchmark in the test suite.

## Loot chests: which Structure owns them?

### Normal chests and barrels → `LootGenerateEvent`

**Verified in javadoc**
- Fires when a `LootTable` fills an `InventoryHolder`, on **first open**. Also fires when a plugin calls `LootTable.fillInventory(...)`: then `isPlugin()` is `true`. Ignore those if our own code fills chests.
- `getLootTable()` is never null. `getLootTable().getKey()` gives the key.
- `getInventoryHolder()` is the chest (null for block-break loot). `getEntity()` is the player who opened it.
- `getLoot()` is mutable and `setLoot(...)` replaces it. This is also the hook for the Vanilla Overrides rule "enchanted books in loot become Rune Caches".
- Cancellable.
- Does **not** fire for entity loot.

**Loot table keys**
`org.bukkit.loot.LootTables` has these constants:

| Structure | Constants |
|---|---|
| Ancient City | `ANCIENT_CITY`, `ANCIENT_CITY_ICE_BOX` |
| Woodland Mansion | `WOODLAND_MANSION` |
| Mineshaft | `ABANDONED_MINESHAFT` |
| Abandoned Camp | `ABANDONED_CAMP_COMMON_CHEST`, `ABANDONED_CAMP_SECRET_CHEST`, `ABANDONED_CAMP_BARREL` |
| Trial Chambers (non-vault) | `TRIAL_CHAMBERS_SUPPLY`, `_CORRIDOR`, `_INTERSECTION`, `_INTERSECTION_BARREL`, `_ENTRANCE`, dispenser and pot tables |

- **Table key → Structure type is a map we keep in config.** Match on the key string, not on the Java constant, so a Paper rename does not break it.
- **Good news for Abandoned Camps:** the secret chest has its own table. A "hidden chest" Relic (Timberfall, Crown of the Grove Blueprint) can key on `ABANDONED_CAMP_SECRET_CHEST` with no position check.
- **Limit:** the table key says *which kind* of Structure, **not which variant**. All 18 camp variants share the three camp tables. (A web search says the variant changes decoration, not the pool. That comes from fan sites and was **not verified**.) If a Relic must only come from the Pale Garden camp (Creaking Axe), use a second check: `getStructures(...)` at the chest position, then look for `ABANDONED_CAMP_PALE_GARDEN`.
- If a vanilla chest was **reached before our plugin existed**, nothing changes: the loot table stays on the container until first open. Pre-open detection: `Lootable#getLootTable()` on the chest block state. (Javadoc implies the table stays until first open; not tested in game.)

### Trial Vaults → `BlockDispenseLootEvent`

**Verified in javadoc**
- Fires when a block dispenses loot from its loot table. The javadoc example is a player unlocking a Trial Chambers vault.
- `getLootTable()`, `getDispensedLoot()` / `setDispensedLoot(...)`, `getPlayer()` (null for a Trial Spawner), `getBlock()`.
- Reward tables: `TRIAL_CHAMBERS_REWARD`, `_COMMON`, `_RARE`, `_UNIQUE` and the `_OMINOUS` versions.
- This covers the Starbreaker Relic ("Trial Vault chests") and the Step of the Chasm Blueprint ("Trial Chambers Ominous Vault"). Both want the **ominous** reward tables.
- Not verified: whether `LootGenerateEvent` also fires for vaults. The javadoc suggests not. Test in game.

### Buried treasure

- `Structure.BURIED_TREASURE` exists. Its chest loot table is **not** in the `LootTables` constants I fetched, so it was **not verified** that it appears there. Needed for the Motherroot Relic. Check `LootTables` and the `LootGenerateEvent` key in the first test server.

## Gaps and risks

- **Minecraft 26.3 Abandoned Camps are new.** Loot table constants exist in Paper 26.3, but I have not seen the table contents.
- Per-block cost was read from source, not benchmarked.
- Biome key accessor and `Registry.STRUCTURE.getKey` calls not compiled. The repo has no Paper dependency yet.
- Folia is not considered (ADR 0001 says Paper).

## Questions for Alfred

1. Does "in a Mineshaft" mean **inside a piece** (`hasStructureAt`) or **inside the whole bounding box**? Proposal: piece.
2. Should an Abandoned Camp Quest or Affinity match **any** of the 18 variants? Proposal: yes, with one config group.
3. OK to key Relic sources on loot table keys in config (`rune.relics.<id>.loot-tables`)? This adds config keys, so it needs your OK before it is built.
