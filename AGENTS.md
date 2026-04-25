# PROJECT KNOWLEDGE BASE

**Generated:** 2026-04-26 01:08:50 +08:00
**Commit:** 671a9c7
**Branch:** 1.20

## Language policy

- 默认使用简体中文回答。
- 除非我明确要求英文，否则不要切换英文叙述。
- 代码、命令、报错、API 名称保持原文，不要强行翻译。
- 提问澄清时也使用中文。

## OVERVIEW

Forge 1.20.1 port of Net Music Mod. Java 17, Parchment mappings, shaded audio codecs, optional compat layers for Cloth
Config, Sophisticated Backpacks, and Touhou Little Maid.

## STRUCTURE

```text
./
├── src/main/java/com/github/tartaricacid/netmusic/
│   ├── api/           # NetEase HTTP/API wrappers, lyric/search/POJO parsing
│   ├── client/        # client-only audio, GUI, renderers, layer registration
│   ├── compat/        # optional integrations grouped by external mod
│   ├── config/        # Forge config spec + music list persistence
│   ├── event/         # server-side startup hooks
│   ├── init/          # deferred registers and common lifecycle hooks
│   ├── network/       # packet channel and message types
│   └── NetMusic.java  # @Mod entrypoint
├── src/main/resources/
│   ├── META-INF/      # mods.toml, access transformer, audio SPI service files
│   ├── assets/netmusic/
│   ├── data/netmusic/
│   └── licenses/      # bundled third-party license texts
├── src/generated/resources/  # configured as resources input, currently empty
└── .github/workflows/        # single Gradle build/release workflow
```

## WHERE TO LOOK

| Task                        | Location                                                                     | Notes                                                       |
|-----------------------------|------------------------------------------------------------------------------|-------------------------------------------------------------|
| Mod bootstrap               | `src/main/java/com/github/tartaricacid/netmusic/NetMusic.java`               | Registers blocks/items/menus/sounds/config and early compat |
| Common setup                | `src/main/java/com/github/tartaricacid/netmusic/init/CommonRegistry.java`    | `FMLCommonSetupEvent` enqueues `NetworkHandler::init`       |
| Server startup data load    | `src/main/java/com/github/tartaricacid/netmusic/event/ServerEvent.java`      | Loads `music.json`/config songs on dedicated server start   |
| Forge config                | `src/main/java/com/github/tartaricacid/netmusic/config/GeneralConfig.java`   | Common config spec, lyrics/proxy/backpack toggles           |
| Music list persistence      | `src/main/java/com/github/tartaricacid/netmusic/config/MusicListManage.java` | Reads/writes `config/net_music/music.json`                  |
| Packets                     | `src/main/java/com/github/tartaricacid/netmusic/network/NetworkHandler.java` | Core channel + base packet ids                              |
| Client setup                | `src/main/java/com/github/tartaricacid/netmusic/client/init/`                | GUI screens, block entity renderer, model layers            |
| Audio runtime               | `src/main/java/com/github/tartaricacid/netmusic/client/audio/`               | Highest-risk subsystem; custom streams and codec providers  |
| Mod integrations            | `src/main/java/com/github/tartaricacid/netmusic/compat/`                     | Cloth, Sophisticated Backpacks, TLM                         |
| Mod metadata                | `src/main/resources/META-INF/mods.toml`                                      | Mod id, dependency ranges, version token expansion          |
| Audio service loader wiring | `src/main/resources/META-INF/services/`                                      | Registers audio readers and format converters               |
| Default assets              | `src/main/resources/assets/netmusic/`                                        | Lang, models, textures, sounds, bundled `music.json`        |
| Datapack content            | `src/main/resources/data/netmusic/`                                          | Recipes and loot tables                                     |

## CODE MAP

| Symbol                                             | Type        | Location                                                                             | Refs | Role                                                             |
|----------------------------------------------------|-------------|--------------------------------------------------------------------------------------|------|------------------------------------------------------------------|
| `NetMusic`                                         | class       | `src/main/java/com/github/tartaricacid/netmusic/NetMusic.java`                       | -    | `@Mod` entrypoint                                                |
| `NetMusic()`                                       | constructor | same                                                                                 | -    | Initializes API, registers deferred registers, registers config  |
| `NetworkHandler`                                   | class       | `src/main/java/com/github/tartaricacid/netmusic/network/NetworkHandler.java`         | -    | Core packet channel                                              |
| `NetworkHandler.init()`                            | method      | same                                                                                 | -    | Registers base messages and delegates compat packet registration |
| `GeneralConfig.init()`                             | method      | `src/main/java/com/github/tartaricacid/netmusic/config/GeneralConfig.java`           | -    | Builds Forge config spec                                         |
| `MusicListManage.loadConfigSongs(ResourceManager)` | method      | `src/main/java/com/github/tartaricacid/netmusic/config/MusicListManage.java`         | -    | Loads persisted/default song list                                |
| `MusicListManage.add163List(long)`                 | method      | same                                                                                 | -    | Fetches playlist, normalizes, writes config JSON                 |
| `CompatRegistry.initNetwork(SimpleChannel)`        | method      | `src/main/java/com/github/tartaricacid/netmusic/compat/tlm/init/CompatRegistry.java` | -    | Adds compat packets only when TLM is loaded                      |

## CONVENTIONS

- Forge 1.20.1 + Java 17 only in this branch.
- Package root is `com.github.tartaricacid.netmusic`; features split by domain, not layered architecture.
- Resource inputs include both `src/main/resources` and `src/generated/resources`.
- Build output is shaded and reobfuscated; `shadowJarPublish` exists as the explicit packaged-jar task, while CI
  publishes jars from `build/libs` after `./gradlew build`.
- Comments are mixed Chinese/English and often explain why a dependency or run arg exists.
- Optional integrations stay under `compat/*` and are gated by loaded-mod checks before registering behavior.

## ANTI-PATTERNS (THIS PROJECT)

- Do not add new AGENTS/docs per tiny package; most small directories are intentionally covered by root guidance.
- Do not treat `src/generated/resources` as hand-authored content unless a real datagen flow is introduced.
- Do not register compat code unconditionally; existing compat paths all guard on mod presence/version.
- Do not casually edit `client/audio` like normal app code; it includes vendored codec/provider logic and live stream
  handling.

## UNIQUE STYLES

- Startup is intentionally split: root mod constructor for deferred registers, event bus subscribers for
  common/client/server hooks, compat bootstrap in dedicated registries.
- Audio SPI wiring is explicit via `META-INF/services/javax.sound.sampled.spi.*` and points at classes under
  `client/audio`.
- TLM compat is a real subsystem, not a tiny adapter: its own plugin, messages, inventory, models, AI tools, and chat
  bubble registration.
- Assets and code are licensed separately.

## COMMANDS

```bash
./gradlew build
./gradlew runClient
./gradlew runClient2
./gradlew runServer
./gradlew prepareRunClientCompile
./gradlew prepareRunServerCompile
./gradlew shadowJarPublish
./gradlew tasks --all
```

## NOTES

- GitHub Actions workflow `.github/workflows/gradle-publish-1.20.yml` builds with `./gradlew build` and publishes jars
  from `build/libs/*.jar`.
- `client` is the largest Java subtree (~2.7k LOC); `compat` is next (~1.5k LOC). Those are currently the child
  AGENTS.md locations worth the overhead here.
- `client/audio/MpegAudioFileReader.java` is the largest hotspot (~818 lines).
- `mods.toml` says `license = "MIT"`, but `LICENSE` is BSD 3-Clause and `LICENSE-ASSETS` is CC BY-NC-SA 4.0. Treat this
  as metadata drift to reconcile carefully, not something to silently copy elsewhere.

# CLIENT SUBTREE KNOWLEDGE BASE

## OVERVIEW

Client-only rendering, menus, audio playback, codec integration, and visual event handling live here.

## STRUCTURE

```text
client/
├── audio/      # playback engine, stream adapters, SPI codec providers
├── event/      # client event hooks
├── gui/        # menu screens
├── init/       # client lifecycle registration
├── model/      # baked/layer models
└── renderer/   # block entity rendering
```

## WHERE TO LOOK

| Task                           | Location                             | Notes                                                      |
|--------------------------------|--------------------------------------|------------------------------------------------------------|
| Register menu screens          | `init/InitContainerGui.java`         | Uses `FMLClientSetupEvent`, then delegates compat screens  |
| Register renderer/model layers | `init/InitModel.java`                | Block entity renderer + layer definitions                  |
| Audio playback routing         | `audio/MusicPlayManager.java`        | Chooses playback path from URL/source                      |
| Streaming audio                | `audio/NetMusicAudioStream.java`     | Buffered/decoded playback path                             |
| Live stream audio              | `audio/NetMusicLiveAudioStream.java` | M3U8/live handling                                         |
| Codec providers                | `audio/Mpeg*`, `audio/Flac*`         | SPI readers/converters registered from `META-INF/services` |
| Hotspot file                   | `audio/MpegAudioFileReader.java`     | Largest file in repo; vendored decoder logic               |

## CONVENTIONS

- Keep client-only registration in `@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)`
  classes.
- Use `evt.enqueueWork(...)` when registration already follows that pattern.
- Audio code is split by responsibility: manager/router, stream implementation, buffer helper, format provider, file
  reader.
- SPI-backed audio classes must stay aligned with `META-INF/services` declarations.

## ANTI-PATTERNS

- Do not rewrite vendored decoder/provider classes unless the fix clearly belongs there.
- Do not move server-safe logic into this subtree; root/config/network own shared runtime concerns.
- Do not register client screens/models from root/common bootstrap paths.
- Do not forget compat screen hooks: `InitContainerGui` delegates to compat registration on purpose.

## NOTES

- `audio/` is the only subtree here that clearly justifies special caution.
- A break in this package can disable playback for MP3, FLAC, or live-stream sources at once.
- When changing provider/file-reader names, update
  `src/main/resources/META-INF/services/javax.sound.sampled.spi.AudioFileReader` and `...FormatConversionProvider`
  together.

# COMPAT SUBTREE KNOWLEDGE BASE

## OVERVIEW

Optional integrations with external mods. Code here should be isolated, gated, and safe to ignore when the target mod is
absent.

## STRUCTURE

```text
compat/
├── cloth/      # Cloth Config screen integration
├── sbackpack/  # Sophisticated Backpacks version-gated hooks
└── tlm/        # Touhou Little Maid subsystem
```

## WHERE TO LOOK

| Task                           | Location                                                                   | Notes                                                       |
|--------------------------------|----------------------------------------------------------------------------|-------------------------------------------------------------|
| Cloth config UI                | `cloth/MenuIntegration.java`                                               | Registers mod config screen and maps `GeneralConfig` fields |
| Sophisticated Backpacks gating | `sbackpack/SBackpackCompat.java`                                           | Registers only when version range matches                   |
| TLM bootstrap                  | `tlm/init/CompatRegistry.java`                                             | Central gate for containers, screens, layers, network       |
| TLM packet ids                 | `tlm/init/NetworkInit.java`                                                | Registers compat packets on ids 99/100                      |
| TLM extension/plugin           | `tlm/MaidPlugin.java`                                                      | Backpack, AI tool, chat bubble hooks                        |
| TLM domain logic               | `tlm/ai`, `tlm/backpack`, `tlm/chatbubble`, `tlm/inventory`, `tlm/message` | Separate responsibilities under one compat feature          |

## CONVENTIONS

- Guard integration behavior behind loaded-mod or version checks before registering anything.
- Keep each external dependency in its own namespace; do not blur TLM, Cloth, and SBackpack code together.
- TLM compat uses one central registry class and fans out into narrower init helpers.
- Config-screen integration mirrors `GeneralConfig`; keep translation keys/defaults aligned with config fields.

## ANTI-PATTERNS

- Do not call compat registration paths from common code without the existing guards.
- Do not assume a compat mod is present just because the dependency is declared as optional or runtime-only.
- Do not reuse base packet ids inside compat code; TLM currently registers on 99/100 through its own init path.
- Do not add generic mod logic here because it happens to touch an integration class.

## NOTES

- `tlm/` is effectively a full submodule and is the main reason this directory gets its own AGENTS file.
- `cloth/` is UI-focused and small; `sbackpack/` is small but sensitive because of explicit version-range checks.
- If a future compat package grows to TLM-like size, follow the same pattern: one guarded registry entrypoint, then
  feature-local helpers.

