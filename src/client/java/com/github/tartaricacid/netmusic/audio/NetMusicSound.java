package com.github.tartaricacid.netmusic.audio;

import com.github.tartaricacid.netmusic.api.lyric.LyricRecord;
import com.github.tartaricacid.netmusic.init.InitSounds;
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.audio.ClientMusicPlaybackManager;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.AudioStream;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundLoader;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

/**
 * @author : IMG
 * @create : 2024/10/2
 */
public class NetMusicSound extends MovingSoundInstance {
    private final URL songUrl;
    private final int tickTimes;
    private final BlockPos pos;
    private final java.util.UUID entityUuid;
    private final boolean isEntityBased;
    private final @Nullable LyricRecord lyricRecord;
    // 如果声音是在实体实例存在时创建的，保留对实体的弱引用以便在 tick 中优先使用
    private transient net.minecraft.entity.Entity initialEntity = null;
    private int tick;
    // 本地运行的 tick 计数（用于防止瞬态状态导致的立即停止）
    private int localTicks = 0;
    // 仅在前 few ticks 输出调试信息，避免日志泛滥
    private int debugTickLogged = 0;
    private final int startProgress; // 开始播放的进度（以 tick 为单位）
    // 解析实体的重试计数（当实体因区块卸载导致不存在时使用）
    private int resolveAttempts = 0;
    private static final int MAX_RESOLVE_ATTEMPTS = 200; // 大约 10 秒（200 ticks）
    // 音频流就绪标志：仅在音频实际创建并跳转到进度后为 true
    private volatile boolean audioReady = false;
    // 是否优先使用立体声解码（仅在本地玩家播放时为 true）
    private final boolean preferStereo;

    public NetMusicSound(BlockPos pos, URL songUrl, int timeSecond, @Nullable LyricRecord lyricRecord) {
        this(pos, songUrl, timeSecond, lyricRecord, 0);
    }

    public NetMusicSound(BlockPos pos, URL songUrl, int timeSecond, @Nullable LyricRecord lyricRecord, int startProgress) {
        super(InitSounds.NET_MUSIC, SoundCategory.RECORDS, SoundInstance.createRandom());
        this.songUrl = songUrl;
        this.x = pos.getX() + 0.5f;
        this.y = pos.getY() + 0.5f;
        this.z = pos.getZ() + 0.5f;
        this.tickTimes = timeSecond * 20;
        this.volume = 4.0f;
        this.relative = false; // ensure positional audio so attenuation applies
        this.attenuationType = SoundInstance.AttenuationType.LINEAR; // 启用距离衰减
        this.tick = startProgress; // 从保存的进度开始
        this.pos = pos;
        this.entityUuid = null;
        this.isEntityBased = false;
        this.lyricRecord = lyricRecord;
        this.startProgress = startProgress;
        this.preferStereo = false;
        NetMusic.LOGGER.info("[NetMusicSound] Created with startProgress={} ticks ({}s), total duration={}s, volume={} at pos {}", 
            startProgress, startProgress / 20, timeSecond, this.volume, pos);
        // 注意：由上层的 MusicPlayManager 负责原子注册，构造函数不再进行注册以避免重复播放竞态
        
        // 如果从非零进度开始，立即更新歌词到正确位置
        // 歌词时间单位：(milliseconds / 50)，即 50ms = 1个歌词单位
        // Minecraft tick：1 tick = 50ms（1/20秒）
        // 所以：歌词单位 = Minecraft ticks
        if (startProgress > 0 && lyricRecord != null) {
            lyricRecord.updateCurrentLine(startProgress);  // startProgress 已经是正确的单位
            NetMusic.LOGGER.info("[NetMusicSound] Initialized lyric position to: tick={}", startProgress);
        }
    }

    public NetMusicSound(java.util.UUID entityUuid, URL songUrl, int timeSecond, @Nullable LyricRecord lyricRecord, int startProgress) {
        super(InitSounds.NET_MUSIC, SoundCategory.RECORDS, SoundInstance.createRandom());
        this.songUrl = songUrl;
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.tickTimes = timeSecond * 20;
        this.volume = 4.0f;
        // 如果目标是本地玩家，则禁用衰减并设为相对声源（听筒播放），否则使用线性衰减的定位声源
        boolean targetIsLocalPlayer = false;
        try {
            net.minecraft.client.network.ClientPlayerEntity localPlayer = MinecraftClient.getInstance().player;
            if (localPlayer != null && localPlayer.getUuid().equals(entityUuid)) {
                targetIsLocalPlayer = true;
            }
        } catch (Throwable ignored) {}
        if (targetIsLocalPlayer) {
            this.relative = true;
            this.attenuationType = SoundInstance.AttenuationType.NONE;
            NetMusic.LOGGER.info("[NetMusicSound] Entity {} is local player: using relative/no-attenuation sound", entityUuid);
        } else {
            this.relative = false;
            this.attenuationType = SoundInstance.AttenuationType.LINEAR;
        }
        this.tick = startProgress;
        this.pos = null;
        this.entityUuid = entityUuid;
        this.isEntityBased = true;
        this.lyricRecord = lyricRecord;
        this.startProgress = startProgress;
        NetMusic.LOGGER.info("[NetMusicSound] Created entity-based sound for entity {} with startProgress={} ticks ({}s), total duration={}s, volume={}",
                entityUuid, startProgress, startProgress / 20, timeSecond, this.volume);
        // 注意：由上层的 MusicPlayManager 负责原子注册，构造函数不再进行注册以避免重复播放竞态
        if (startProgress > 0 && lyricRecord != null) {
            lyricRecord.updateCurrentLine(startProgress);
            NetMusic.LOGGER.info("[NetMusicSound] Initialized lyric position to: tick={}", startProgress);
        }
        // 本地玩家播放使用立体声（若配置允许）
        this.preferStereo = targetIsLocalPlayer;
    }

    // 新增：使用实体实例初始化的构造函数，以便声音创建时立即拥有正确位置
    public NetMusicSound(net.minecraft.entity.Entity entity, URL songUrl, int timeSecond, @Nullable LyricRecord lyricRecord, int startProgress) {
        super(InitSounds.NET_MUSIC, SoundCategory.RECORDS, SoundInstance.createRandom());
        this.songUrl = songUrl;
        this.x = (float) entity.getX();
        this.y = (float) entity.getY();
        this.z = (float) entity.getZ();
        this.tickTimes = timeSecond * 20;
        this.volume = 4.0f;
        // 如果绑定的实体是本地玩家，则设置为相对声源并禁用衰减
        if (entity instanceof net.minecraft.client.network.ClientPlayerEntity) {
            this.relative = true;
            this.attenuationType = SoundInstance.AttenuationType.NONE;
            NetMusic.LOGGER.info("[NetMusicSound] Created for local player entity {}: relative/no-attenuation", entity.getUuid());
        } else {
            this.relative = false;
            this.attenuationType = SoundInstance.AttenuationType.LINEAR;
        }
        this.tick = startProgress;
        this.pos = null;
        this.entityUuid = entity.getUuid();
        this.isEntityBased = true;
        this.lyricRecord = lyricRecord;
        this.startProgress = startProgress;
        NetMusic.LOGGER.info("[NetMusicSound] Created entity-based sound for entity {} (initial pos={}, {}, {}) with startProgress={} ticks ({}s), total duration={}s, volume={}",
                entity.getUuid(), this.x, this.y, this.z, startProgress, startProgress / 20, timeSecond, this.volume);
        // 保存实体引用；由上层的 MusicPlayManager 负责原子注册，构造函数不再进行注册以避免重复播放竞态
        this.initialEntity = entity;
        if (startProgress > 0 && lyricRecord != null) {
            lyricRecord.updateCurrentLine(startProgress);
            NetMusic.LOGGER.info("[NetMusicSound] Initialized lyric position to: tick={}", startProgress);
        }
        this.preferStereo = entity instanceof net.minecraft.client.network.ClientPlayerEntity;
    }

    public BlockPos getPos() {
        return pos;
    }

    public java.util.UUID getEntityUuid() {
        return entityUuid;
    }

    /**
     * 事件驱动地将此声音绑定到已解析的实体上（由外部事件触发）。
     */
    public void bindToEntity(net.minecraft.entity.Entity ent) {
        if (ent == null) return;
        try {
            this.initialEntity = ent;
            this.resolveAttempts = 0;
            this.x = (float) ent.getX();
            this.y = (float) ent.getY();
            this.z = (float) ent.getZ();
            NetMusic.LOGGER.info("[NetMusicSound] bindToEntity: bound sound for {} to entity {} at pos={},{},{}", this.entityUuid, ent.getUuid(), this.x, this.y, this.z);
        } catch (Throwable ignored) {}
    }

    @Override
    public void tick() {
        ClientWorld world = MinecraftClient.getInstance().world;
        if (world == null) {
            return;
        }
        tick++;
        localTicks++;
        // 客户端只负责播放音频，进度由服务器计算并同步（PlayProgressMessage）
        // 这样可以避免多客户端并发上传导致的同步问题
        if (!isEntityBased) {
            // 基于方块位置的播放：确保 TileEntity 存在并且处于播放状态
            BlockEntity te = world.getBlockEntity(pos);
            if (te instanceof TileEntityMusicPlayer musicPlayer) {
                if (localTicks == 1) {
                    NetMusic.LOGGER.info("[NetMusicSound] Started playback (localTicks=1), recovered progress from {}", startProgress);
                }

                // 检查播放是否停止
                if (!musicPlayer.isPlay()) {
                    if (localTicks <= 5) {
                        NetMusic.LOGGER.info("[NetMusicSound] Detected musicPlayer.isPlay()==false but localTicks={} <= 5, deferring stop", localTicks);
                    } else {
                        NetMusic.LOGGER.info("[NetMusicSound] Music player stopped at tick={}", tick);
                        musicPlayer.lyricRecord = null;
                        ClientMusicPlaybackManager.unregister(pos);
                        this.setDone();
                        return;
                    }
                }

                // 首次同步歌词记录
                musicPlayer.lyricRecord = lyricRecord;
            } else {
                // TileEntity 未就绪：短延迟内忽略，延迟后停止
                if (localTicks <= 5) {
                    NetMusic.LOGGER.warn("[NetMusicSound] TileEntityMusicPlayer not found at pos {} but localTicks={} <= 5, deferring stop", pos, localTicks);
                } else {
                    NetMusic.LOGGER.warn("[NetMusicSound] TileEntityMusicPlayer not found at pos {}, stopping sound", pos);
                    ClientMusicPlaybackManager.unregister(pos);
                    this.setDone();
                    return;
                }
            }
        } else {
            // entity-based: 优先检查本地玩家（避免遍历全世界）
            // 优先使用在构造时保存的实体引用（如果存在且仍有效）
            net.minecraft.entity.Entity entity = null;
            try {
                    if (this.initialEntity != null) {
                    try {
                        if (!this.initialEntity.isRemoved()) {
                            entity = this.initialEntity;
                        } else {
                            // 若初始实体被标记为已移除，认为实体已死亡/永久消失或被临时卸载，先将播放请求加入 pending 以便重新加载时恢复
                            NetMusic.LOGGER.info("[NetMusicSound] initialEntity {} isRemoved=true, scheduling pending resume and stopping sound", this.initialEntity.getUuid());
                            try {
                                int timeSecond = tickTimes / 20;
                                com.github.tartaricacid.netmusic.receiver.PendingEntityPlaybackManager.addPending(this.entityUuid, this.songUrl, "", timeSecond, this.tick);
                            } catch (Throwable ignored) {}
                            try {
                                if (this.entityUuid != null) ClientMusicPlaybackManager.unregisterForEntity(this.entityUuid);
                            } catch (Throwable ignored) {}
                            this.setDone();
                            return;
                        }
                    } catch (Throwable ignored) {
                        this.initialEntity = null;
                        this.resolveAttempts = 0;
                    }
                }
                if (entity == null) {
                    net.minecraft.client.network.ClientPlayerEntity clientPlayer = MinecraftClient.getInstance().player;
                    if (clientPlayer != null && clientPlayer.getUuid().equals(entityUuid)) {
                        entity = clientPlayer;
                    }
                }
            } catch (Throwable ignored) {
            }
            if (entity == null) {
                // 如果 initialEntity 和本地玩家都未命中，则尝试基于 UUID 在 world 中解析实体（有重试机制）
                try {
                    // 仅每隔若干 tick 做一次尝试以减少开销
                    // 每 20 ticks 进行一次解析尝试（约 1 秒），减少频繁遍历开销
                    if (localTicks % 20 == 0 && resolveAttempts < MAX_RESOLVE_ATTEMPTS) {
                        resolveAttempts++;
                        Object entObj = null;
                        try {
                            java.lang.reflect.Method m = world.getClass().getMethod("getEntity", java.util.UUID.class);
                            entObj = m.invoke(world, entityUuid);
                        } catch (NoSuchMethodException nsme) {
                            try {
                                java.lang.reflect.Method m2 = world.getClass().getMethod("getEntityByUuid", java.util.UUID.class);
                                entObj = m2.invoke(world, entityUuid);
                            } catch (NoSuchMethodException ignored) {}
                        }
                        if (entObj instanceof net.minecraft.entity.Entity resolved) {
                            entity = resolved;
                            this.initialEntity = resolved;
                            // 解析成功，重置解析计数以便未来再次丢失时还能重新尝试
                            this.resolveAttempts = 0;
                            // 立即同步位置到声音实例
                            try {
                                this.x = (float) resolved.getX();
                                this.y = (float) resolved.getY();
                                this.z = (float) resolved.getZ();
                            } catch (Throwable ignored) {}
                            NetMusic.LOGGER.info("[NetMusicSound] Resolved entity {} after {} attempts, resuming playback", entityUuid, resolveAttempts);
                        }
                    }
                } catch (Throwable t) {
                    NetMusic.LOGGER.debug("[NetMusicSound] Exception while resolving entity {}: {}", entityUuid, t.getMessage());
                }

                if (entity == null) {
                    NetMusic.LOGGER.debug("[NetMusicSound] Entity {} not currently present (attempts={}), will retry", entityUuid, resolveAttempts);
                }
            }
            // 若多次重试仍未找到实体，则停止播放以释放资源（不再基于短期 localTicks 停止）
            if (entity == null && resolveAttempts >= MAX_RESOLVE_ATTEMPTS) {
                NetMusic.LOGGER.warn("[NetMusicSound] Entity {} not found after {} attempts, stopping sound", entityUuid, resolveAttempts);
                // 在停止前将播放请求加入 pending，以便实体再次出现在客户端时能够恢复播放
                try {
                    int timeSecond = tickTimes / 20;
                    com.github.tartaricacid.netmusic.receiver.PendingEntityPlaybackManager.addPending(entityUuid, this.songUrl, "", timeSecond, this.tick);
                    NetMusic.LOGGER.debug("[NetMusicSound] Added pending resume for entity {} before stopping", entityUuid);
                } catch (Throwable ignored) {}
                ClientMusicPlaybackManager.unregisterForEntity(entityUuid);
                this.setDone();
                return;
            }
            if (entity != null) {
                // 更新声音坐标以跟随实体
                try {
                    this.x = (float) entity.getX();
                    this.y = (float) entity.getY();
                    this.z = (float) entity.getZ();
                } catch (Throwable ignored) {}

                // 在前几次 tick 输出调试信息，帮助诊断为何后续找不到实体
                if (debugTickLogged < 6) {
                    boolean lookupMatches = false;
                    try {
                        net.minecraft.entity.Entity byId = null;
                        try {
                            byId = world.getEntityById(entity.getId());
                        } catch (Throwable ignored) {}
                        lookupMatches = (byId == entity);
                    } catch (Throwable ignored) {}
                    NetMusic.LOGGER.info("[NetMusicSound DEBUG] tick={} localTicks={} entity id={} uuid={} isRemoved={} lookupMatches={}", tick, localTicks, entity.getId(), entity.getUuid(), entity.isRemoved(), lookupMatches);
                    debugTickLogged++;
                }

                // 可尝试同步歌词到附近的 TileEntity（若存在）
                // BlockPos currentPos = new BlockPos((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
                // BlockEntity be = world.getBlockEntity(currentPos);
                // if (be instanceof TileEntityMusicPlayer musicPlayer) {
                //     musicPlayer.lyricRecord = lyricRecord;
                // }
            } else {
                // 实体仍未解析到，但解析次数尚未耗尽；继续重试，不在此处停止
                NetMusic.LOGGER.debug("[NetMusicSound] Entity {} not found (attempts={}), deferring stop until attempts exhausted", entityUuid, resolveAttempts);
            }
        }
        
        if (tick > tickTimes + 50) {
            if (pos != null) {
                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (blockEntity instanceof TileEntityMusicPlayer endMusicPlayer) {
                    endMusicPlayer.lyricRecord = null;
                    NetMusic.LOGGER.info("[NetMusicSound] Music playback finished at tick={}", tick);
                }
                ClientMusicPlaybackManager.unregister(pos);
            }
            // 如果是实体绑定播放，确保也注销实体注册，释放预占/注册键
            if (entityUuid != null) {
                ClientMusicPlaybackManager.unregisterForEntity(entityUuid);
            }
            this.setDone();
        } else {
            if (world.getTime() % 8 == 0) {
                for (int i = 0; i < 2; i++) {
                    world.addParticle(ParticleTypes.NOTE,
                            x - 0.5f + world.random.nextDouble(),
                            y + world.random.nextDouble() + 1,
                            z - 0.5f + world.random.nextDouble(),
                            world.random.nextGaussian(), world.random.nextGaussian(), world.random.nextInt(3));
                }
            }
        }

        // 依据 tick 更新歌词显示（仅在音频流就绪后开始持续更新）
        // 歌词时间单位与 Minecraft tick 单位相同（都是 50ms），所以直接传递 tick
        if (lyricRecord != null && audioReady) {
            lyricRecord.updateCurrentLine(tick);
        }
    }

    @Override
    public CompletableFuture<AudioStream> getAudioStream(SoundLoader soundBuffers, Identifier id, boolean looping) {
        NetMusic.LOGGER.debug("[NetMusicSound] getAudioStream called: startProgress={} ticks ({}s), looping={}", startProgress, startProgress / 20, looping);
        NetMusic.LOGGER.debug("[NetMusicSound] AUDIO STREAM REQUEST startProgress={}, songUrl={}, looping={}", startProgress, songUrl, looping);
        
        return CompletableFuture.supplyAsync(() -> {
            NetMusic.LOGGER.debug("[NetMusicSound] INSIDE ASYNC SUPPLIER: starting audio stream creation");
            try {
                NetMusic.LOGGER.debug("[NetMusicSound] Creating NetMusicAudioStream (preferStereo={})", this.preferStereo);
                NetMusicAudioStream audioStream = new NetMusicAudioStream(this.songUrl, this.preferStereo);
                NetMusic.LOGGER.debug("[NetMusicSound] NetMusicAudioStream created successfully");
                
                // 如果需要从进度中间开始播放，跳过前面的音频数据
                if (startProgress > 0) {
                    NetMusic.LOGGER.debug("[NetMusicSound] About to skip to progress: {} ticks", startProgress);
                    NetMusic.LOGGER.info("[NetMusicSound] Attempting to skip to progress: {} ticks ({}s)", startProgress, startProgress / 20);
                    audioStream.skipToProgress(startProgress);
                    NetMusic.LOGGER.debug("[NetMusicSound] Skip completed");
                    NetMusic.LOGGER.info("[NetMusicSound] Successfully skipped to progress: {} ticks", startProgress);
                } else {
                    NetMusic.LOGGER.debug("[NetMusicSound] Starting from beginning (startProgress=0)");
                    NetMusic.LOGGER.info("[NetMusicSound] Starting from beginning (startProgress=0)");
                }
                
                // 标记音频已准备就绪（此处在音频线程中设置，tick() 会看到此变更并开始持续更新歌词）
                try {
                    this.audioReady = true;
                    NetMusic.LOGGER.info("[NetMusicSound] Audio ready for songUrl={}, startProgress={}", songUrl, startProgress);
                } catch (Throwable ignored) {}
                NetMusic.LOGGER.debug("[NetMusicSound] Returning audioStream object: {}", audioStream);
                NetMusic.LOGGER.debug("[NetMusicSound] ========== END ASYNC SUPPLIER (SUCCESS) ==========");
                return audioStream;
            } catch (Exception e) {
                NetMusic.LOGGER.error("[NetMusicSound] ERROR IN ASYNC SUPPLIER: {}", e.getMessage(), e);
                NetMusic.LOGGER.error("[NetMusicSound] Failed to create audio stream: {}", e.getMessage(), e);
                NetMusic.LOGGER.debug("[NetMusicSound] ========== END ASYNC SUPPLIER (ERROR) ==========");
            }
            NetMusic.LOGGER.debug("[NetMusicSound] Returning null from async supplier");
            return null;
        }, Util.getMainWorkerExecutor());
    }
}
