
# 实体驱动播放（Event-driven / BE-NBT First）说明

目的
- 说明将实体随身播放行为改为“事件驱动 + BE/区块 NBT 为首要来源”的设计，并记录和块播放器（TileEntityMusicPlayer）协作的行为、回调点与验证步骤。

核心思想
- 优先从实体/方块实体（BE）持久化的 NBT 恢复播放；在网络消息作为兼容或回退通路存在时，消息会被转为“挂起”并等到实体可用后由客户端触发真正的播放。
- 事件驱动：客户端通过 BE 更新回调、实体出现事件或定期扫描触发 pending 恢复；不再依赖服务器即时广播作为唯一信源，从而避免在玩家加入/区块延迟时产生的竞态与重复注册问题。

关键行为与触发点
- 服务端写入持久化虚拟会话：`EntityMusicPlayerManager.registerVirtualEntitySession(...)` 会将播放元数据（`songUrl`、`songTime`、`songName`、`playProgress`、`playStartWorldTick`、`uuid` 等）保存在运行时映射并持久化到世界存档。
- 服务端广播（兼容）：为了兼容旧客户端或作为即时提示，服务端仍可发送 `MusicToClientMessage`；客户端收到此消息时若检测到实体不可用，会将请求转为 pending（由 `PendingEntityPlaybackManager` 保存）。
- 客户端触发点：
  - `NetMusicClient.onBlockEntityPlaybackNbt(...)`：TileEntity 在 NBT 更新时通过反射或事件调用该函数，将数据放入 `pendingPlayback`（或立即尝试创建，如果实体已存在）。
  - 实体出现事件：`NetMusicClient` 每 20 tick 扫描新出现的实体，并在新实体出现时调用 `PendingEntityPlaybackManager.onEntityLoaded(uuid, entity)`（以及 `ClientMusicPlaybackManager.notifyEntityLoaded`）。
  - `PendingEntityPlaybackManager`：收到挂起请求并在实体出现时负责预占、获取歌词（可选）、创建绑定 `NetMusicSound` 并交由 `ClientMusicPlaybackManager` 注册。

  集成注意：Voice Chat 与音频预检
  - 客户端在启动或玩家加入时会对本地音频管线做 preflight（音频探针）与网络音频可用性检查。为避免与第三方模组（如 voicechat）在音频管线初始化期间发生竞态，客户端已实现以下策略：
    - 若检测到 `voicechat` 模组存在且尚未完成连接/初始化，客户端会延迟执行本地 preflight 与网络探测，并将播放请求优先放入 `pending`。
    - 一旦 `VOICECHAT_CONNECTED` 事件触发，客户端会开始所有被延迟的探针并在主线程触发 `tickPendingCreations(...)` 以尽快恢复挂起的播放请求。
    - 可通过配置调整重试策略：`GeneralConfig.AUDIO_PREFLIGHT_PERSISTENT`、`AUDIO_PREFLIGHT_RETRY_INITIAL_MS`、`AUDIO_PREFLIGHT_RETRY_MAX_MS`。


NBTF 字段与语义（服务端写入）
- `songUrl` (String)：来源 URL
- `songTime` (int)：歌曲时长（秒）或 0 表示未知
- `songName` (String)
- `playProgress` (int)：以 tick 为单位的已播放进度
- `playStartWorldTick` (long)：写入时的世界 tick，用于计算客户端到达时的实际进度
- `uuid` (String)：所属实体 UUID（若为随身播放）

客户端预占与注册语义
- 预占（`ClientMusicPlaybackManager.reserveEntity` / `reservePos`）：在开始异步创建音频前占位以避免重复路径。
- 原子注册（`registerIfAbsentEntity` / `registerIfAbsentPos`）：创建完成后尝试原子注册，如注册失败会回滚并释放预占。
- 陈旧清理（`cleanupStaleEntityRegistration`）：如果预占或残留注册超过阈值会被清理以允许重试。

对旧消息通路的兼容处理
- `MusicToClientMessageReceiver` 仍然存在，但在实体不可用时将消息加入 `PendingEntityPlaybackManager` 而非直接以 UUID 构建立即播放。

验证与调试要点
- 在客户端日志中关注：
  - `[PendingEntityPlaybackManager] Added pending playback for entity ...`（消息被挂起）
  - `[PendingEntityPlaybackManager] Reserving entity ... on load: true/false`（预占结果）
  - `[ClientMusicPlaybackManager] Notified and bound sound to entity ...`（绑定成功）
  - `Cancelled reservation` / `Failed to create bound sound` 等错误；必要时检查 `NetMusicSound` 的构造异常。
- 场景测试：复现“客户端A播放 → 客户端B加入”并观察客户端B是否先收到 pending，再在实体出现（或 BE 更新）时创建绑定声音。

播放失败处理与健康检查

- 预占与已注册分离：客户端在尝试创建播放时会先对目标（entity/pos）做 `reserve`，仅在 `SoundInstance` 创建并原子注册成功后才将其标记为 `playing`（并在内部 `soundMap` 中保存实例）。这避免了在创建失败或注册失败时出现“假阳性”播放标记。
- 延迟健康检查：`MusicPlayManager` 在调用 `SoundManager.play()` 后会启动短延迟（约 350ms）的健康检查，若 `NetMusicSound` 在短时间内未能完成音频流就绪（`isAudioReady()==false`），客户端会回滚注册（`unregister`）并停止该声音，必要时将请求重新放回 `pending` 以便后续重试。
- 延迟健康检查：`MusicPlayManager` 在调用 `SoundManager.play()` 后会计划由客户端主线程 tick 驱动的短延迟（约 350ms）健康检查，若 `NetMusicSound` 在短时间内未能完成音频流就绪（`isAudioReady()==false`），客户端会回滚注册（`unregister`）并停止该声音，必要时将请求重新放回 `pending` 以便后续重试。
- 异常回退与重试：在音频流创建抛出异常时，上层会取消预占并将请求重新加入 `pendingPlayback`（或 `PendingEntityPlaybackManager`），并对重试次数实施退避以避免无限重试。
- 日志与限频：关键失败/回滚日志使用限频策略，既能定位问题又不会在高频失败时刷屏。

调度细节（sentinel 与主线程初始化）

- 在某些启动/加入流程中，健康检查可能在 `world` 或区块/实体尚不可用时就被安排。为避免这种情况下立即过期并触发误回滚，实现中使用了一个 sentinel 机制：当调度时检测到 `world` 不可用，会把 `dueTick` 设置为 `-1` 作为占位。
- 在随后由主线程的 `tickHealthChecks` 处理队列时，会把 `-1` 的占位项初始化为 `currentWorldTime + INITIAL_HEALTHCHECK_DELAY_TICKS`（默认约 7 tick ≈ 350ms），从而保证首次健康检查总是在世界可用且有机会完成音频初始化后才真正执行。
- 因为音频准备可能涉及异步解码与跳过进度（seek），仍然建议保留区块加载与 BE/实体就绪的检查逻辑：当目标区块未加载或 BE/实体尚不可用时，健康检查会短期重试（每次延迟 5 tick，最多重试若干次），避免在加载过程中误回滚。
- 如果仍出现重复/多播或回滚抖动，可通过增大 `INITIAL_HEALTHCHECK_DELAY_TICKS`（例如 10–12）或在服务端重触发 BE NBT 更新来稳妥恢复。

这些机制一同保证：即使网络/解码或实体解析在短期内失败，客户端也不会长期误把目标标记为正在播放，同时会在后续实体可用或 BE 更新时继续恢复播放。

迁移建议（实践）
1. 保持服务端写入 NBT 为首要行为并持久化（已实现）。
2. 保持消息通路仅做兼容与即时提示，确保消息接收端不会在实体不可用时直接创建 UUID 播放（已改为 pending）。
3. 将 `PendingEntityPlaybackManager` 的触发点完整接线到客户端的 BE 回调与实体出现事件（已接入 `NetMusicClient` 的实体扫描回调）。
4. 通过多客户端 + 服务器重启测试验证在常见场景下无重复注册或阻塞。

文件参考
- 代码：`TileEntityMusicPlayer`、`EntityMusicPlayerManager`、`NetMusicClient`、`PendingEntityPlaybackManager`、`ClientMusicPlaybackManager`、`NetMusicSound`。

作者与版本
- 更新：2026-01-08（事件驱动及 pending 行为）

## 实体跟随播放（follow-sound） — 技术说明（已更新）

目的：总结已实现的改动与运行时语义，说明客户端与服务端之间的消息路径、原子注册逻辑、以及新的网络广播策略。

**目标要点**
- **持久恢复**: 实现虚拟实体会话的持久化（在重连/登出/重载世界后恢复播放）。
- **可靠且低噪声的通知**: 优先使用区块/TE 同步与半径内消息，避免不必要的全服广播。
- **客户端去重**: 提供原子注册 API，保证网络消息路径与 BlockEntity 驱动恢复之间互斥，消除重复播放。

**总体设计概览**
- 服务端:
  - 持久化会话（文件/数据结构）用于保存虚拟实体播放会话并在世界重载后恢复。
  - `TileEntityMusicPlayer` 仅在必要时写入持久化字段，并通过标准区块/TE 同步（`markDirty()` + `world.updateListeners(...)`）把恢复信息下发给追踪该区块的客户端。
- 网络消息:
  - `MusicToClientMessage` 携带播放位置、URL、播放进度与实体标识（`entityId` + `entityUuid` 字符串）。
  - 客户端在收到消息时优先使用 `world.getEntityById(entityId)`（O(1)）查找目标实体，避免昂贵的全世界 UUID 遍历。
- 客户端:
  - `ClientMusicPlaybackManager` 维护两类索引：基于位置的 key（区块或坐标）与基于实体的 key（`entity:<uuid>`）。
  - 提供原子注册方法以确保不会重复创建播放实例（详见“原子注册 API”）。

**原子注册 API（行为规范）**
- 核心语义：注册操作应为原子且幂等；只有第一个成功注册者负责持有并启动对应的 `SoundInstance`，后续并发注册者应安全放弃并释放任何已分配资源。
- 推荐方法（示例）：
  - `boolean registerIfAbsentPos(String key, SoundInstance sound)` — 当且仅当 `key` 不存在时注册并返回 true，否则返回 false。
  - `boolean registerIfAbsentEntity(UUID entityUuid, SoundInstance sound)` — 当且仅当该实体未注册时注册；若成功且 sound 带有位置，则同时建立 pos-key 映射。
  - `void unregisterByPos(String key)` / `void unregisterByEntity(UUID entityUuid)` — 注销并停止对应播放，同时清理位置/实体索引。

**客户端消息与恢复路径（两条互补路径）**
- 网络即时路径（优先）：
  - 收到 `MusicToClientMessage` 后，客户端先用 `entityId` 查实体：
    - 若找到实体：构造基于实体的 `NetMusicSound` 并调用 `registerIfAbsentEntity`。
    - 若未找到实体：按当前策略通常直接放弃（减少额外遍历与内存），或将请求放入 `pendingPlayback` 由 BE 驱动路径在后续 tick 中尝试恢复（可根据需要配置）。
- BE 驱动路径（可靠恢复）：
  - 当区块/TE 同步（NBT）到客户端时，BE 会把恢复请求推入客户端的 `pendingPlayback`。
  - 客户端在主线程 tick 中处理 `pendingPlayback`：调用 `registerIfAbsentPos`（或解析 UUID 并调用 `registerIfAbsentEntity`）。

**网络广播与同步策略（已实现）**
- 由全服广播向更精细的通知机制迁移，已降低网络噪声并依赖 Minecraft 标准的区块追踪同步：
  - `TileEntityMusicPlayer.setPlayProgress(...)` 不再直接广播到所有在线玩家；改为 `markDirty()` 并调用 `world.updateListeners(pos, state, state, 3)`，由追踪该区块的客户端通过 TE 的 NBT 恢复状态。
  - 增加了 `NetworkHandler.sendToNearby(world, x, y, z, payload, radius)`，用于在半径内通知附近玩家（实现默认 48 块），用于替代之前对虚拟实体注册/停止的全服广播场景。
  - 会话 owner（实体持有者）仍使用 `NetworkHandler.sendToClientPlayer(...)` 进行直接通知，保证 owner 立即接收并做出响应。
  - `NetworkHandler.broadcastToAll` 保留为显式全服广播场景，但不作为常规路径。

**实现影响的关键文件（已修改/应检查）**
- `src/main/java/com/github/tartaricacid/netmusic/client/ClientMusicPlaybackManager.java` — 添加并实现原子注册/注销 API。
- `src/main/java/com/github/tartaricacid/netmusic/network/MusicToClientMessageReceiver.java` — 在消息接收路径使用实体 id 优先查找并调用原子注册，避免 UUID 全遍历。
- `src/main/java/com/github/tartaricacid/netmusic/client/NetMusicClient.java` — 在处理 BE 驱动的 `pendingPlayback` 时使用原子注册接口。
- `src/main/java/com/github/tartaricacid/netmusic/sound/NetMusicSound.java` — 确保在未注册或注册失败时正确释放音频资源，避免流泄漏。
- `src/main/java/com/github/tartaricacid/netmusic/tileentity/TileEntityMusicPlayer.java` — 只在必要时更新持久化字段；保留对旧字段（如 `CURRENT_TIME`）的兼容读取但不再写入，以便平滑迁移。

**迁移与兼容性注意**
- 移除持久化字段（如 `CURRENT_TIME`）前保留兼容读取逻辑：读取旧字段但不再写入，确保老存档平滑过渡。

---