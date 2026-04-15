# AGENTS.md

## Language policy

- 默认使用简体中文回答。
- 除非我明确要求英文，否则不要切换英文叙述。
- 代码、命令、报错、API 名称保持原文，不要强行翻译。
- 提问澄清时也使用中文。

## Operating rules

- Work from the repo root.
- Treat committed files and the verified Gradle command baseline as source of truth.
- Keep changes repo-specific. Don't paste generic Java, ForgeGradle, or outdated Forge advice.
- Focus on the NeoForge ecosystem.
- Don't invent tooling. This repo has no dedicated lint or formatter Gradle task.
- Don't treat local ignored files as policy. `src/test` is gitignored.

## Policy files

- No `.cursorrules`, no `.cursor/rules/**`, and no `.github/copilot-instructions.md` were found.

## Project overview

- This repo builds a Minecraft NeoForge mod (e.g., NetMusic / Touhou Little Maid).
- The active build here is the 26.1.x NeoForge setup using the `net.neoforged.moddev` plugin in `build.gradle`.
- Main code lives under `src/main/java` and resources live under `src/main/resources`.
- Generated resources are dynamically created and loaded from `src/generated/resources`.
- Mod metadata is dynamically generated via the `generateModMetadata` task from `src/main/templates`.

## Environment and toolchain

- Build tool: Gradle (Version 8.x+).
- Java toolchain: Java 25, strictly set by `java.toolchain.languageVersion = JavaLanguageVersion.of(25)`.
- Applied Gradle plugins include:
    - `net.neoforged.moddev` (NeoForge standard development plugin).
    - `com.gradleup.shadow` (For shading third-party libraries like audio codecs).
    - `java-library`, `eclipse`, `idea`, `maven-publish`.
- Run configurations are defined within the `neoForge.runs` block (`client`, `server`, `data`, `gameTestServer`).
- CI uses GitHub Actions, sets up JDK 25, then runs `./gradlew build`.

## Canonical commands

### List tasks

Unix:
```bash
./gradlew tasks --all