# Feedback

Visuals show what Gameplay does. Gameplay never depends on Visuals (ADR 0003).

## XP feedback (2026-11-13)
- `visuals.feedback` listens to `JobProgress.addJobXp` through a `JobXpListener`.
- Every gain shows `+12 Mining XP` in the action bar.
- A Job Level-up shows a title and plays the level-up sound.
- An Account Level-up sends a chat message.
- All text is in `lang/*/feedback.json` (English + French).
- Admin `/rune xp set` and `/rune level set` show nothing.

## Unlock pop-ups (2026-11-14)
- When a Job Level unlocks something, the vanilla advancement pop-up shows it in the top-right corner (one per new node of the Unlock Tree).
- No chat announcement for these nodes.
- See "Unlock Tree" in `docs/design-decisions.md`.
