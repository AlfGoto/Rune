# 0001 — Build Rune as a Paper server plugin, not a client mod

**Status:** Accepted (2026-11-08)

## Context
We want an RPG layer (Jobs, Runes, Rune Tools, Quests) that players can join without installing anything, that other servers can adopt easily, and that survives Minecraft updates with little effort.

## Decision
Rune is a Paper server plugin, paired with a server resource pack for custom item models, icons (font glyphs) and textured menus. It targets the latest Minecraft version and only uses Paper's public API (no NMS), with the version set in a single build property.

## Consequences
- Players join with a vanilla client; the resource pack is sent on join.
- Other servers install one `.jar`.
- Version bumps are mostly a one-line change.
- No genuinely new blocks, mobs or keybinds: custom items are vanilla items with custom models, and Active Runes use clicks in the air instead of new keys.
