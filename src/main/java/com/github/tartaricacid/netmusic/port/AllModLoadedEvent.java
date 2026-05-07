package com.github.tartaricacid.netmusic.port;

import com.github.tartaricacid.netmusic.NetMusic;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.loader.api.FabricLoader;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 此事件会在所有模组初始化后触发, 且仅触发一次
 * <br>
 * 在客户端由 {@link net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents#CLIENT_STARTED ClientLifecycleEvents.CLIENT_STARTED} 触发
 * <br>
 * 在专用服务端由 {@link net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents#SERVER_STARTED ServerLifecycleEvents.SERVER_STARTED} 触发
 */
public class AllModLoadedEvent {
    private static final Event<Callback> CALLBACK = EventFactory.createArrayBacked(Callback.class, listeners -> event -> {
        for (Callback listener : listeners) {
            listener.post(event);
        }
    });

    private static final AtomicBoolean INVOKED = new AtomicBoolean(false);

    public EnvType getEnvironmentType() {
        return FabricLoader.getInstance().getEnvironmentType();
    }

    public interface Callback {
        void post(AllModLoadedEvent event);
    }

    public static void register(Callback callback) {
        CALLBACK.register(callback);
    }

    public static void invoke() {
        // 确保事件只被触发一次
        if (INVOKED.compareAndSet(false, true)) {
            CALLBACK.invoker().post(new AllModLoadedEvent());
        } else {
            RuntimeException e = new RuntimeException("already invoked!");
            NetMusic.LOGGER.error("Unexpected invocation of AllModLoadedEvent: {}", e.getMessage(), e);
        }
    }
}
