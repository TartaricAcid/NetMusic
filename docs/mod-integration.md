# 模组集成指南：如何调用方块播放器与实体播放器

本文档面向希望与 NetMusic 交互的第三方模组开发者，介绍如何在服务端/服务端与客户端场景下调用 `TileEntityMusicPlayer`、`EntityMusicPlayerManager` 及触发客户端恢复播放的推荐方法。

总体原则
- 优先使用服务端 API：在服务器侧修改 TE 状态并调用库方法，保持服务器为播放状态的单一真源（source of truth）。
- 触发客户端恢复通过 BE NBT（推荐）或在兼容场景下发送播放消息（回退）。
- 避免直接在客户端强制创建声音实例；务必通过服务端写 NBT 或官方 API 让客户端在 BE/实体加载时恢复。

一、服务端：在方块播放器上触发播放

1) 在你的服务器侧逻辑中获取 `TileEntityMusicPlayer`（示例：在交互或命令处理里）：

```java
// 假设在服务端上下文（ServerWorld world, BlockPos pos）
TileEntityMusicPlayer te = (TileEntityMusicPlayer) world.getBlockEntity(pos);
if (te == null) return; // 非播放器方块

// 插入 CD（示例）：
ItemStack cd = ...; // 你的 CD ItemStack，必须兼容 NetMusic 的 ItemMusicCD
te.setStack(0, cd);

// 让 TE 在服务器端开始播放（会写入 NBT 并触发区块更新）
ItemMusicCD.SongInfo info = ItemMusicCD.getSongInfo(cd);
if (info != null) {
    te.setPlayToClient(info); // 服务端记录 startTick 并 markDirty 触发客户端 NBT 更新
}
```

说明：`setPlayToClient` 会把播放状态写入 TE NBT 并在服务器侧注册为活动播放，客户端加载该区块时会在 `NetMusicClient.onBlockEntityPlaybackNbt(...)` 路径中恢复播放。

二、服务端：将实体注册为随身播放目标（随声听）

有两种方式：较高层的推荐方式是使用 `EntityMusicPlayerManager.registerEntityToMusicPlayer`，它会处理持久化虚拟会话并尽可能广播通知；低层方式是直接调用 `TileEntityMusicPlayer.registerActiveEntity`（需要在同一世界线程）。示例：

```java
// 高层：推荐调用（它会持久化虚拟会话并广播 nearby 通知）
EntityMusicPlayerManager.registerEntityToMusicPlayer(entity, te);

// 或者：低层直接注册（在你已经在服务器世界上下文时）
te.registerActiveEntity((ServerWorld) world, entity.getUuid(), "your-mod:attach-reason");
```

对应取消绑定：

```java
EntityMusicPlayerManager.unregisterEntityFromMusicPlayer(entity, te); // 推荐
// 或
te.unregisterActiveEntity((ServerWorld) world, entity.getUuid(), "your-mod:detach-reason");
```

扩展：直接通过歌曲 ID 在实体上播放

如果你只知道歌曲在网易云的 ID（例如通过外部接口或命令），可以直接调用 `EntityMusicPlayerManager.playEntityBySongId`，该方法会尝试通过内置的网易云 API 获取歌曲信息并为实体注册一个虚拟播放会话（持久化），使实体在重连或世界重载后仍可恢复播放。

示例：在服务端命令或事件中调用

```java
// 假设在服务端上下文 (ServerWorld world)
Entity target = ...; // 目标实体
long songId = 123456L; // 网易云歌曲ID
boolean started = EntityMusicPlayerManager.playEntityBySongId(target, songId);
if (started) {
    // 可选：记录日志或给玩家反馈
}
```

行为说明：
- **前提**: 只能在服务端调用；如果实体不在 `ServerWorld` 上会返回 `false`。
- **持久化**: 成功时会将会话保存为虚拟实体会话（写入 `netmusic_entity_players.dat`），并在实体在线时向附近玩家广播播放消息。
- **恢复**: 客户端重连或实体重载时会尝试从持久化会话恢复播放。


三、服务端：持久化虚拟会话（跨重启恢复）

如果你希望随身播放在服务器重启后依然可恢复，请使用 `EntityMusicPlayerManager.registerVirtualEntitySession(ServerWorld, UUID, NbtRecord)`。
但通常不需要直接构造 `NbtRecord`：调用 `registerEntityToMusicPlayer(...)` 时，NetMusic 会尝试从 TE/项目信息创建并持久化会话。

四、如何为非方块/非实体场景触发播放

如果你的模组需要给指定玩家播放（不属于世界实体），你可以使用现有的消息通路（`MusicToClientMessage`），但推荐仅在以下情形使用：
- 兼容旧客户端或即时 UI 音效；
- 无法或不适合写入 BE NBT 的短时播放。 

示例：发送播放消息给单个玩家

```java
MusicToClientMessage msg = new MusicToClientMessage(player.getBlockPos(), url, timeSecond, songName, playProgress, player.getId(), player.getUuid().toString());
NetworkHandler.sendToClientPlayer(msg, player);
```

注意：在实体播放场景下，若实体不可用，客户端会把消息转为 pending 并在实体加载时恢复；但服务端优先写 NBT 才是最稳妥的做法。

五、客户端注意事项（给集成者的说明）

- 如果你在客户端需要检测播放状态或为 GUI 显示歌词/进度，建议通过读取 TE 的 NBT（`toInitialChunkDataNbt` / `BlockEntityUpdateS2CPacket`）或监听 `NetMusicClient` 提供的回调点；避免直接实例化 `NetMusicSound`。
- 若你的模组创建/移动实体并希望音乐随身，请在服务端更新实体与 TE 的关联（调用 `registerActiveEntity`），并确保在世界上下文（ServerWorld）执行。

六、调试与日志

- 如需调试交互：在服务端调用 API 后，检查服务端日志是否有 `Registered virtual session` / `Registered entity` 等信息；客户端应在 BE 更新时输出 `Added pending playback` 或 `Reserved entity` 等日志。

注意：播放创建失败的处理

- 在少数网络或解码错误场景下，客户端创建声音可能失败。客户端已实现由主线程 tick 驱动的短延迟健康检查并在失败时回滚注册（取消 reservation/unregister 并停止声音），同时将请求重新加入 pending 以便后续重试。通常不需要第三方模组介入；若需要人工干预，请在服务端重写 TE NBT 或重新调用注册 API 以触发客户端恢复。

七、示例工作流程（完整）

1. 玩家 A 在方块上插入 CD，服务端在 TE 中调用 `setPlayToClient(info)`。TE 写入 NBT 并 markDirty。 
2. 客户端 B 加载区块并接收 BE 更新，触发 `NetMusicClient.onBlockEntityPlaybackNbt(...)`，客户端尝试恢复播放（优先按 ownerUuid 定位实体，否则按 pos 播放）。
3. 若玩家 A 将播放绑定到某实体，服务端调用 `EntityMusicPlayerManager.registerEntityToMusicPlayer(entity, te)` 并持久化虚拟会话；新加入/返回的客户端会据此恢复随身播放。

八、兼容性与版本注意

- 本指南基于 NetMusic `1.21-fabric` 分支的实现；API 名称与行为在未来可能变化，请以源码为准。