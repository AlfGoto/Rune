# Research: resource pack on the latest Minecraft

Issue #16. Researched 2026-11-11 for **Minecraft 26.3** (the minimum in `docs/design-decisions.md`).

**Short answer**
- Item models: use the **`item_model` component**. Not the old integer custom model data.
- Pack format: **`97.1`** for 26.3 (pre-release 1 value, see warning).
- Icons: **font glyphs** from a `bitmap` provider, private-use characters.
- Translations: `assets/<ns>/lang/<code>.json` in the pack, plus the **`fallback`** field.
- Sending: Paper `Player#sendResourcePacks` with a **SHA-1 hex** hash.

This doc only reports facts. It decides nothing. Open points are listed at the end.

## How sure am I?
- **Checked on a source:** everything not marked otherwise.
- **Not checked:** marked **(unverified)**. Test these in the first pack prototype.
- Pack format `97.1` comes from the 26.3 **pre-release 1** notes. Re-check it on the final 26.3 release.

## 1. `item_model` component vs custom model data

**Item model definition files**
- Path: `assets/<namespace>/items/<id>.json`.
- An item uses it when its `item_model` component is `<namespace>:<id>`.
- Root has a required `model` object. Optional: `hand_animation_on_swap`, `oversized_in_gui`, `swap_animation_scale`.
- A missing model id shows the missing-model placeholder.

**Model types** (the `type` field of a model object)
- `model`: one model file, with optional `tints`.
- `composite`: several models in one space.
- `condition`: `on_true` / `on_false` from a boolean property (`broken`, `damaged`, `using_item`, ...).
- `select`: pick a case from a discrete property, with `fallback`.
- `range_dispatch`: pick by a number and `threshold`.
- `special`: built-in models (chest, shield, head, trident...).
- `empty`, `bundle/selected_item`.

**`custom_model_data` today is not one integer.** It is a compound with four lists:

| List | Read by | Missing value gives |
|---|---|---|
| `floats` | `range_dispatch` | `fallback` model |
| `flags` | `condition` | `on_false` model |
| `strings` | `select` | `fallback` model |
| `colors` | `tints` of a `model` | the `default` tint |

Each reader takes an `index` (default `0`).

**What this means for Rune**
- A Rune item type = a vanilla base item + its **own `item_model` id**. One file per item in `items/`.
- `custom_model_data` is for variants of one item (e.g. Rarity colour tint, Broken state, rank number).
- The item model id is the clean place for "one Rune Tool = one model".
- All of it is plain item components. Paper's public API can set them. No NMS needed (matches ADR 0001).
- Paper API calls to set `item_model` and `custom_model_data` on an `ItemMeta` or `ItemStack`: **(unverified)**, check the Javadoc when building Items.

## 2. Pack format number

**`pack.mcmeta` format (since 1.21.9)**
```json
{
  "pack": {
    "description": "Rune",
    "min_format": [97, 1],
    "max_format": 97
  }
}
```
- `min_format` / `max_format` take an integer or `[major, minor]`.
- Integer in `min_format` = `[N, 0]`.
- Integer in `max_format` = any minor of `N`.
- `pack_format` and `supported_formats` are only needed to support old versions. We do not (we target latest only).
- `overlays` exist for per-version differences. Not needed now.

**Numbers on the wiki** (resource pack format)

| Version | Format |
|---|---|
| 26.1 pre1 | 84.0 |
| 26.2 pre1 | 88.0 |
| 26.3 snapshot 10 | 97.0 |
| **26.3 pre1** | **97.1** |
| 26.4 snapshot 7 (upcoming) | 102.0 |

Mojang's 26.3 pre-release 1 notes also say: "The Resource Pack version is now 97.1".

**Warning**
- The number changes almost every snapshot, so it will change on each Minecraft bump.
- Every version-bump PR (Renovate) must update the format in the pack. This is a real cost of the "one build property" goal.
- Idea for the Foundations layer: **the pack format is generated from the one Minecraft version build property.** This is an idea, not a decision. See open points.

## 3. Font glyphs for icons

- Font file: `assets/<namespace>/font/<name>.json`.
- It has a `providers` list. Types: `bitmap`, `space`, `ttf`, `unihex`, `reference`.
- `bitmap` fields: `file` (PNG, glyph max 256×256), `height` (default 8), `ascent`, `chars` (grid of characters; `\u0000` pads empty cells).
- Characters above U+FFFF need surrogate pairs in the JSON.
- A text component uses it with `"font": "<ns>:<name>"` (default `minecraft:default`). The `font` field exists since 1.16.
- Each glyph is a character. Use a **private-use** Unicode range so it never clashes with real text.
- `space` providers can add negative or positive advances. Useful to place icons and align menu textures. **(unverified)** that negative advances still work this way on 26.3. Check in the prototype.

**What this means for Rune**
- Rarity icons, Job icons and menu backgrounds = glyphs in one Rune font.
- The glyph character table (character ↔ icon) should be generated or kept in one place in code, so the pack and the plugin never drift apart.
- Text with a glyph is a component with a `font`. Fine with Adventure components in Paper.
- Item names with an icon glyph: the glyph is in the name component. The `font` stays on that component only.

## 4. Translations in the pack

- Path: `assets/<namespace>/lang/<code>.json`, e.g. `en_us.json`, `fr_fr.json`.
- Format: flat JSON object, `"key": "text"`.
- Placeholders: `%s`, or indexed `%1$s` (index starts at 1). A literal percent is `%%`.
- Lang files from several packs merge. A key missing in a pack is loaded from packs of lower priority.
- A client picks its language from its own settings.
- Component: `{"translate": "rune.job.mining", "fallback": "Mining", "with": [...]}`.
  - `fallback` is used if no translation is found. Added in 1.19.4.
  - Without `fallback`, a missing key shows **the raw key**.
  - `with` fills `%s` slots. An empty slot shows nothing.

**What this means for Rune** (fits the decision of 2026-11-11 in `docs/design-decisions.md`)
- Every translatable component carries the English text in `fallback`. A missing key never shows a raw key.
- The pack contains `en_us.json` and `fr_fr.json`, built from the plugin's language files, so there is **one source of text**.
- The generated pack must be rebuilt when a server owner adds a language or edits text.
- The behaviour of the client when the selected language lacks a key but `en_us` has it: **(unverified)**. The `fallback` field covers us either way.
- Server-side texts the client cannot translate (console, logs) use the plugin's own language files. **(unverified)** how Paper renders translatable components in console. Check in the prototype.

## 5. How Paper sends a pack with its hash

**Preferred API: Adventure, through `Player`** (`Player` is an `Audience`)
```java
ResourcePackInfo info = ResourcePackInfo.resourcePackInfo()
    .uri(URI.create("https://example.com/rune.zip"))
    .hash("<40-char SHA-1 hex>")
    .build();

ResourcePackRequest request = ResourcePackRequest.resourcePackRequest()
    .packs(info)
    .prompt(Component.text("..."))
    .required(true)
    .build();

player.sendResourcePacks(request);
```
- Hash = **SHA-1 of the ZIP file**, as a 40-character hex string. A wrong hash makes the client reject the pack. Recompute it whenever the ZIP changes.
- A pack is identified by a **UUID**. The pack info builder computes one from its name **(unverified)**; check how to set it. Keep it stable so the client replaces the old pack instead of stacking.
- `required(true)` forces the pack. Declining = disconnect **(unverified for the Paper path; the vanilla `require-resource-pack` does that)**.
- The request can replace or stack with other server packs. Stacking needs 1.20.3+. The builder method name for this: **(unverified)**.
- The client reports status through a callback. The Adventure docs warn the status can be **wrong, missing or out of order**. Do not trust it for game logic.
- Remove packs with `removeResourcePacks()` / `clearResourcePacks()` (Audience), or `Player#removeResourcePack(UUID)`.

**Older Bukkit methods** (still on `Player`, same effect)
- `addResourcePack(UUID, String url, byte[] hash, String prompt, boolean force)`: hash is a `byte[]`, nullable.
- `setResourcePack(UUID, String url, String hash, Component prompt, boolean required)`: hash is a hex `String`.
- `getResourcePackHash()` is **deprecated for removal** and always null.

**What this means for Rune** (fits the built-in HTTP server decision)
- The plugin builds the ZIP, computes the SHA-1 once, serves the ZIP, then sends `url + hash` on join.
- Config option for an external URL: the plugin still needs the hash of that ZIP. It can download it and hash it, or the owner sets the hash in config. **Open point.**
- `server.properties` `resource-pack` / `require-resource-pack` also exist, but they are one pack for the whole server and need a restart. The per-player API is better here.
- Serving over HTTP from the plugin: the URL given to the client must be reachable by the player. That is why `/preview` needs a second tunnel for the pack port (already decided).
- Size limit and timeouts of the client download: **(unverified)**. Keep the pack small.

## Open points for Alfred (nothing built)
1. **Pack format:** do we want the format generated from the Minecraft version build property? (Would avoid a manual edit on every Renovate PR.)
2. **External URL mode:** who provides the SHA-1: config, or the plugin downloads and hashes?
3. **Required or optional pack:** `required(true)` or a soft prompt? Not decided in `docs/`.
4. **Check on the final 26.3 release:** the format `97.1`.
5. **Prototype to settle the `(unverified)` items:** item model API calls, negative-advance glyph spacing, console rendering, pack UUID handling.

## Sources
- [Resource pack (Minecraft Wiki)](https://minecraft.wiki/w/Resource_pack)
- [Pack.mcmeta (Minecraft Wiki)](https://minecraft.wiki/w/Pack.mcmeta)
- [Items model definition (Minecraft Wiki)](https://minecraft.wiki/w/Items_model_definition)
- [item_model component (Minecraft Wiki)](https://minecraft.wiki/w/Data_component_format/item_model)
- [custom_model_data component (Minecraft Wiki)](https://minecraft.wiki/w/Data_component_format/custom_model_data)
- [Font definition (Minecraft Wiki)](https://minecraft.wiki/w/Font_definition)
- [Text component format (Minecraft Wiki)](https://minecraft.wiki/w/Text_component_format)
- [Minecraft 26.3 pre-release 1 (minecraft.net)](https://www.minecraft.net/en-us/article/minecraft-26-3-pre-release-1)
- [Adventure: resource packs (PaperMC docs)](https://docs.papermc.io/adventure/resource-pack)
- [Paper `Player` Javadoc](https://jd.papermc.io/paper/org/bukkit/entity/Player.html)
