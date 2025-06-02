package com.github.tartaricacid.netmusic.audio;

import net.minecraft.client.sound.AbstractSoundInstance;
import net.minecraft.client.sound.TickableSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.random.Random;

/**
 * @author : IMG
 * @create : 2024/10/2
 */

public abstract class AbstractTickableSoundInstance extends AbstractSoundInstance implements TickableSoundInstance {
    private boolean stopped;

    protected AbstractTickableSoundInstance(SoundEvent sound, SoundCategory soundCategory, Random random) {
        super(sound, soundCategory, random);
    }

    @Override
    public boolean isDone() {
        return this.stopped;
    }

    protected final void stop() {
        this.stopped = true;
        this.repeat = false;
    }
}
