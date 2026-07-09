# NetMusic NeoForge 1.21.1 移植版 — Fork / PR / 介绍 全流程指南

## 一、Fork 工作流

### 1.1 从原仓库 Fork

原仓库地址：`https://github.com/TartaricAcid/NetMusic`

1. 访问原仓库页面，点击右上角 **Fork** 按钮
2. 选择你的 GitHub 账户作为 Fork 目标
3. Clone 你的 Fork 到本地：
   ```bash
   git clone https://github.com/<你的用户名>/NetMusic.git
   cd NetMusic
   ```
4. 添加原仓库为上游（用于后续同步更新）：
   ```bash
   git remote add upstream https://github.com/TartaricAcid/NetMusic.git
   ```

### 1.2 创建特性分支

基于原仓库的默认分支创建你的移植分支：

```bash
git checkout -b neoforge-1.21.1
```

### 1.3 提交你的改动

将你的 NeoForge 1.21.1 移植代码复制/整理到该分支中，然后：

```bash
git add .
git commit -m "feat: port NetMusic to NeoForge 1.21.1 with playlist & Create compat"
git push origin neoforge-1.21.1
```

### 1.4 同步上游更新（如需要）

```bash
git fetch upstream
git merge upstream/master  # 或 upstream/main
# 解决冲突后
git push origin neoforge-1.21.1
```

---

## 二、PR 描述模板

向原仓库提交 Pull Request 时，建议使用以下模板：

---

**标题：** `[NeoForge 1.21.1] Port NetMusic with playlist support & Create mod compatibility`

**正文：**

```markdown
## 概述

将 NetMusic 移植到 NeoForge 1.21.1，并新增播放列表、网易云歌单解析、Create 模组兼容等功能。

## 主要变更

### 核心移植
- 从 Forge 迁移至 NeoForge 1.21.1（基于 NeoForge 21.1.x API）
- 适配 DataComponent 系统替代旧的 Capabilities
- 适配 NeoForge 新的网络包注册机制（PayloadRegistrar / CustomPacketPayload）

### 新增功能
- **播放列表系统**：唱片支持存储多首歌曲，支持顺序播放、随机播放、单曲循环三种模式
- **网易云歌单解析**：服务端懒加载解析网易云语义 URL 为真实播放直链（eapi 接口）
- **Create 模组兼容**：唱片机可在 Create 的 Contraption（列车/装置）上正常播放、放入/取出唱片、自动切歌

### Bug 修复
- 修复 DeferredHolder 在模组构造函数中未绑定导致 NPE 崩溃的问题
- 修复 Contraption 上唱片机 NeoForgeData 子标签读取失败的问题
- 修复 Contraption 上取出唱片后声音无法停止的问题

## 技术要点

| 问题 | 原因 | 修复方案 |
|------|------|----------|
| 模组加载崩溃 NPE | `CreateCompat.register()` 在构造函数中调用 `InitBlocks.MUSIC_PLAYER.get()`，此时 DeferredHolder 尚未绑定 | 移至 `FMLCommonSetupEvent.enqueueWork()` 中调用 |
| Contraption 上无法检测唱片 | NeoForge 1.21 的 `BlockEntity.getPersistentData()` 存储在 `NeoForgeData` 子标签下，而非顶层 NBT | 添加 `getNeoForgeData()` 辅助方法正确导航子标签 |
| Contraption 上取出唱片后声音不停 | `stopPlaying()` 仅更新服务端数据，未通知客户端 | 新增 `ContraptionMusicStopMessage` 网络包，在停止播放时发送至客户端 |
| Contraption 运行时唱片数据丢失 | `getCdStack/setCdStack` 读写 `blockEntityData`（持久数据），但 Contraption 运行时使用 `context.data` | 改为读写 `context.data`，并在 `startMoving/stopMoving/writeExtraData` 中同步 |

## 兼容性

- Minecraft 1.21.1
- NeoForge 21.1.x
- Create 0.5.1+（可选依赖，仅当 Create 存在时激活兼容模块）

## 测试

- [x] 唱片机基础播放功能正常
- [x] 播放列表顺序/随机/单曲循环模式正常
- [x] 网易云歌单解析与播放正常
- [x] Create Contraption 上放入/取出唱片正常
- [x] Create Contraption 上自动切歌正常
- [x] Create Contraption 上取出唱片后声音正确停止
- [x] 模组在未安装 Create 时正常加载（无崩溃）
```

---

## 三、项目介绍（可用于 README 或 CurseForge/Modrinth 页面）

---

### NetMusic — NeoForge 1.21.1

一个 Minecraft 音乐播放机模组，支持在线音乐播放、播放列表和 Create 模组兼容。

#### 功能特性

**🎵 在线音乐播放**
- 支持通过 URL 播放在线音乐
- 内置网易云音乐解析器，自动将网易云歌曲链接解析为可播放直链
- 服务端懒加载解析：播一首解析一首，避免批量请求

**📋 播放列表系统**
- 一张唱片可存储多首歌曲
- 三种播放模式：
  - **顺序播放**（Sequential）：按列表顺序依次播放
  - **随机播放**（Random）：随机选择下一首
  - **单曲循环**（Single Loop）：循环播放当前歌曲
- 支持 Shift+右键切换播放模式
- 支持通过电脑方块编辑播放列表

**🚂 Create 模组兼容**
- 唱片机可在 Create 的 Contraption（列车、装置等）上正常工作
- 支持放入/取出唱片
- 支持自动切歌和播放列表
- Contraption 停止/重启时正确保持播放状态
- 取出唱片时声音正确停止

**🔧 技术细节**
- 基于 NeoForge 1.21.1 API
- 使用 DataComponent 存储唱片数据
- 异步 URL 解析机制，不阻塞主线程
- 播放代数（Generation）机制防止过期回调

#### 依赖

| 模组 | 类型 | 版本 |
|------|------|------|
| Minecraft | 必须 | 1.21.1 |
| NeoForge | 必须 | 21.1.x |
| Create | 可选 | 0.5.1+ |

#### 配置

- `musicQuality`：网易云音质选择（标准/较高/极高/无损）
- `musicUCookie`：网易云音乐 MUSIC_U Cookie（用于 VIP 歌曲解析）

---

## 四、关键技术变更摘要

### 4.1 NeoForge DataComponent 适配

NeoForge 1.21.1 移除了 Capabilities 系统，改用 DataComponent 存储物品附加数据。唱片数据（`SongInfo`、`PlaylistData`）通过 `DataComponentType` 注册并附加到 `ItemStack` 上。

### 4.2 网络包注册

NeoForge 新版网络系统使用 `CustomPacketPayload` + `PayloadRegistrar`：

```java
// 注册示例
registrar.playToClient(Message.TYPE, Message.STREAM_CODEC, Message::handle);
```

所有网络消息均为 `record` 类型，实现 `TYPE`（`CustomPacketPayload.Type<T>`）和 `STREAM_CODEC`（`StreamCodec<ByteBuf, T>`）。

### 4.3 Create Contraption 数据流

```
组装时:  blockEntityData (NeoForgeData子标签) → context.data (运行时)
运行中:  getCdStack/setCdStack 读写 context.data
tick中:  自动切歌、播放计时
拆卸时:  context.data → blockEntityData (NeoForgeData子标签)
```

关键点：
- NeoForge 1.21 的 `BlockEntity.getPersistentData()` 数据存储在 NBT 的 `NeoForgeData` 子标签下
- Contraption 运行时必须使用 `context.data`，不能直接读写 `blockEntityData`
- `startMoving` / `stopMoving` / `writeExtraData` 负责两个存储之间的同步

### 4.4 播放代数机制

为防止异步 URL 解析回调过期（如快速切换歌曲时），使用 `PlayGeneration` 计数器：

```java
int gen = context.data.getInt(PLAY_GENERATION_TAG) + 1;
context.data.putInt(PLAY_GENERATION_TAG, gen);
// 异步回调中检查
if (context.data.getInt(PLAY_GENERATION_TAG) != gen) return; // 过期，忽略
```

### 4.5 客户端声音停止

Contraption 上的声音由客户端 `ContraptionMusicSound` 管理（基于 `entityId` 的 `ConcurrentHashMap`）。停止播放时需要发送 `ContraptionMusicStopMessage` 通知客户端调用 `stopPreviousSound(entityId)`。