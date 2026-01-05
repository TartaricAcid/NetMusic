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

**测试要点与方案**
- 本地 `runClient` 测试场景：
  - 在有玩家附近的 BE 上触发 `playfollow`，验证附近客户端立即收到并播放（通过 `sendToNearby` 或 BE 同步）。
  - 重连/重载世界后（单人转服务端重启），验证 BE 驱动的恢复路径能恢复播放且客户端不会重复创建播放实例。
  - 多客户端同时触发通知，验证仅有一个 `SoundInstance` 被注册（无重复播放）。
- 日志关注：注册成功/失败、重复检测、音频流创建与销毁。

**迁移与兼容性注意**
- 移除持久化字段（如 `CURRENT_TIME`）前保留兼容读取逻辑：读取旧字段但不再写入，确保老存档平滑过渡。

---

如需我把本次重写提交到仓库并运行一次 `gradlew build -x test` 以验证构建，请告诉我，我可以继续执行。也可以把文档进一步精简成英文版或加入示例代码片段。 