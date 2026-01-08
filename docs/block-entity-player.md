# 方块播放器（TileEntity / Block Entity Music Player）技术文档

概述
- `TileEntityMusicPlayer` 是世界中的方块播放器（块实体），负责在其所在方块处播放音乐并可被实体“随身收听”。本模块既支持本地方块播放，也与实体绑定/虚拟会话协作以实现随身播放与持久化恢复。

核心职责
- 存储与管理播放内容：CD 插槽、播放开关、进度、音量等状态。
- 维护“active map”：在服务端为每个世界维护一个位置到 `TileEntityMusicPlayer` 的映射，便于 JOIN 时通知玩家。
- 与 `EntityMusicPlayerManager` 协作：当玩家或实体被注册为随身播放目标时，调用 `registerActiveEntity`、`unregisterActiveEntity` 并触发虚拟会话持久化（若需要）。

重要 API/方法（摘要）
- `isPlay()`：是否正在播放。
- `getPlayProgress()`：返回当前播放进度（tick）。
- `getPlayStartWorldTick()`：返回播放开始时的世界 tick（用于恢复进度计算）。
- `registerActiveEntity(ServerWorld, UUID, String)`：将实体 UUID 与该 TE 关联（用于随身播放）。
- `unregisterActiveEntity(ServerWorld, UUID, String)`：解除关联。
- `getAssociatedEntities(ServerWorld)`：返回与本 TE 关联的实体 UUID 列表。
- `toInitialChunkDataNbt()` / `readNbt(...)`：用于将播放元数据写入 BE NBT（客户端可通过该 NBT 恢复/触发播放）。

NBT 与同步（客户端/服务端交互）
- 推荐写入字段（至少包含）：`songUrl`, `songTime`, `songName`, `playProgress`, `playStartWorldTick`, `ownerUuid`（若随身）。
- 当 TE 的播放状态变化时应调用 `markDirty()` 并确保区块/BE NBT 会被发送到客户端，从而触发 `NetMusicClient.onBlockEntityPlaybackNbt(...)` 的处理路径。

持久化与虚拟会话
- 在需要随身播放且需跨重启恢复时，`EntityMusicPlayerManager.registerVirtualEntitySession(...)` 会将会话信息持久化到服务器保存文件（`netmusic_entity_players.dat`）。
- TE 本身也会通过 NBT 提供即时同步，使客户端能在区块加载或玩家加入时尽快获知播放信息。

生命周期与事件流
1. 玩家在 TE 插入 CD 并开始播放，TE 更新 NBT 并更新 `TileEntityMusicPlayer` 的运行时状态。
2. 若玩家调用随身注册（attach to entity），TE 会调用 `registerActiveEntity`，并由 `EntityMusicPlayerManager` 创建/持久化虚拟会话并（可选）广播 `MusicToClientMessage`。
3. 客户端在收到 BE NBT 或播放消息时，会通过 `NetMusicClient.onBlockEntityPlaybackNbt(...)` 或 `PendingEntityPlaybackManager` 恢复播放。
4. 当播放停止或 TE 被清理时，TE 应清除相应 NBT 并调用 `unregisterActiveEntity`（必要时同时从持久化会话中移除）。

参考文件
- 代码：`TileEntityMusicPlayer`、`EntityMusicPlayerManager`、`NetMusicClient`、`PendingEntityPlaybackManager`、`ClientMusicPlaybackManager`。

作者与版本
- 更新：2026-01-08
