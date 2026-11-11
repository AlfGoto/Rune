# Rune

An RPG layer for Minecraft: players level Jobs by playing, and customise themselves and their gear with Runes.

Rune is a Paper server plugin. Players join with a vanilla client. See `CONTEXT.md` and `docs/` for the design.

## Requirements

- Java 25
- No other install: Gradle comes from the wrapper.

## Build

```
./gradlew build
```

The plugin jar is in `build/libs/`.

## Run a local server

```
./gradlew runServer
```

- Starts Paper with the plugin in the `run/` folder.
- Look for `Rune enabled` in the log.
- Join `localhost` with a vanilla client.
- First run: accept the EULA in `run/eula.txt`, then run it again.

## Minecraft version

It lives in **one** place: `minecraftVersion` in `gradle.properties`.

## License

MIT, see `LICENSE`.
