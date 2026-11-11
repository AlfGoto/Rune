# 0004 — Self-registering modules (no shared files to edit)

**Status:** Accepted (2026-11-11)

## Context
ADR 0003 splits the code into modules. But every module still had to be wired by hand into `RunePlugin.onEnable()`, a shared `/rune` subcommand list, a shared `DebugSubcommand`, and the two big language files. Six PRs opened on the same day all edited `RunePlugin.java`, three of them each added a *different* `command/DebugSubcommand.java`, and five edited `lang/en_us.json` and `lang/fr_fr.json`. Every merge conflicted with the others.

## Decision
**Adding a feature never edits a file shared with other features.**

### 1. Modules register themselves
Each module is a class that implements `RuneModule` and is tagged `@AutoService(RuneModule.class)`:

```java
@AutoService(RuneModule.class)
public final class BlockTrackingModule implements RuneModule {
    @Override
    public void enable(RuneContext ctx) {
        BlockTracker tracker = ctx.provide(BlockTracker.class, new PdcBlockTracker(Text.NAMESPACE));
        ctx.listen(new BlockTrackingListener(tracker));
        ctx.debug(new BlocksDebug(tracker));      // /rune debug blocks
    }
}
```

- The AutoService annotation processor writes `META-INF/services` at compile time, so there is no hand-written list to conflict on.
- `RunePlugin` finds every module with `ModuleLoader` and enables them. **Do not edit `RunePlugin`** to add a feature.
- `RunePlugin` itself only sets up what every module needs: config (`ctx.config()`) and `PlayerStorage` (`ctx.get(PlayerStorage.class)`).

### 2. Modules share objects as services
- A module shares an object with `ctx.provide(Type.class, object)`.
- Another module reads it with `ctx.get(Type.class)` and lists the provider in `dependsOn()`.
- `ModuleLoader` enables modules in dependency order (ties sorted by class name, so the order is always the same). A missing dependency or a cycle stops the plugin with a clear message.
- No getters on `RunePlugin` (no `plugin.blockTracker()`): use services.
- **Services follow ADR 0003's direction.** A Gameplay module provides the framework (often an empty registry). Content modules `dependsOn` it and fill it. A Gameplay module never imports `content.*`. Example: `JobsModule` provides an empty `JobRegistry`, and `content.jobs.CoreJobsModule` registers Mining and Woodcutting into it.
- **One service type per thing.** Don't use a generic class like `Registry` as the service key. Wrap it in a named type (`JobRegistry`, `RuneRegistry`…) so two registries never clash.
- **Read registries when they're used, not at enable time.** Content modules add entries after the framework module is enabled, so a command or listener that copies `all()` in `enable` misses them.

### 3. Commands
- `ctx.subcommand(...)` adds `/rune <name>`.
- `ctx.debug(...)` adds `/rune debug <name>`. Each module brings its own debug tool with its own name (`blocks`, `events`, `reveal`…). Nobody owns a shared `DebugSubcommand`.
- **Commands live with their feature.** The `command` package is only the `/rune` router (`Subcommand`, `RuneCommand`, `DebugCommand`, `/rune version`). Any module may import it, and it never imports a feature. A feature's subcommands and debug tools go in the feature's own package (`gameplay.jobs.XpSubcommand`, `foundations.blocktracking.BlocksDebug`). Putting them in `command` creates a package cycle.

### 4. Language files: one per module
- Translation keys live in `src/main/resources/lang/<language>/<module>.json` (e.g. `lang/en_us/mining.json` and `lang/fr_fr/mining.json`).
- The resource pack build merges them into one file per language. A key defined in two files fails the build.
- `lang/<language>/core.json` holds keys that belong to no module (rarity names, `/rune` usage).

### 5. Docs
- A new design decision goes in a new ADR, or in the doc of its topic (`docs/jobs/mining.md`…), rather than at the end of a shared section of `docs/design-decisions.md`.

## Consequences
- Two features built in parallel only conflict if they really change the same thing.
- A module can be tested alone: create a `RuneContext`, call `enable`.
- Load order is explicit (`dependsOn()`), not the order of lines in `onEnable()`.
- Still shared, and still able to conflict: `pack/files.txt` (one line per asset) and `config.yml`. Fix them the same way when they start to hurt.
