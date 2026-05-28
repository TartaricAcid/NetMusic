package com.github.tartaricacid.netmusic.client.audio.raytrace;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Util;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.tartaricacid.netmusic.client.audio.raytrace.RayTraceCalculator.*;
import static org.lwjgl.openal.AL11.*;
import static org.lwjgl.openal.EXTEfx.*;

public class RayTraceManager {
    public static final boolean RAYTRACE = true;

    static final int MAX_DISTANCE = 64;
    static final int MAX_BOUNCE_ROUND = 5;
    static final ArrayList<HitPoint> HIT_POINTS = new ArrayList<>();
    private static int AUX_SLOT = -1;
    private static int REVERB_EFFECT = -1;
    private static final ConcurrentHashMap<Vec3, SourceAudioData> SOURCE_AUDIO_DATA_CACHE = new ConcurrentHashMap<>();

    private static long lastUpdate = 0;

    public static int getSlot() {
        if (AUX_SLOT == -1) {
            AUX_SLOT = alGenAuxiliaryEffectSlots();
            REVERB_EFFECT = alGenEffects();
            alEffecti(REVERB_EFFECT, AL_EFFECT_TYPE, AL_EFFECT_EAXREVERB);
        }
        return AUX_SLOT;
    }

    public static void tick() {
        if (!RAYTRACE) {
            return;
        }
        if (Minecraft.getInstance().level == null) {
            return;
        }
        OpenAlEngine.executeOnAlThread(RayTraceManager::updateReflectionPan);
        if (Util.getMillis() - lastUpdate < 500) {
            return;
        }
        lastUpdate = Util.getMillis();
        clear();
        update();
    }

    private static void clear() {
        synchronized (HIT_POINTS) {
            HIT_POINTS.clear();
        }
        SOURCE_AUDIO_DATA_CACHE.clear();
    }

    private static void update() {
        generateHitPoints();
        RayTraceCalculator.run();
        OpenAlEngine.executeOnAlThread(RayTraceManager::setEaxReverb);
    }

    private static void generateHitPoints() {
        synchronized (HIT_POINTS) {
            ClientLevel level = Minecraft.getInstance().level;
            Vec3 earPos = getEarPos();
            for (Vector3d ray : generateRays(getRayAmount(), 0)) {
                generateOneRay(earPos, ray, level, ClipContext.Block.OUTLINE, 0x80ffffff);
            }
            for (Vector3d ray : generateRays(getRayAmount(), 0.5)) {
                generateOneRay(earPos, ray, level, ClipContext.Block.VISUAL, 0x8000ff00);
            }
        }
    }

    private static void generateOneRay(Vec3 earPos, Vector3d ray, ClientLevel level, ClipContext.Block blockCollision, int color) {
        float journey = 0;
        Vec3 startPos = new Vec3(earPos.x, earPos.y, earPos.z);
        for (int round = 0; round < MAX_BOUNCE_ROUND; round++) {
            if (journey >= MAX_DISTANCE) {
                break;
            }
            BlockHitResult hitResult = RayTraceHelper.shoot(startPos, startPos.add(ray.x * MAX_DISTANCE, ray.y * MAX_DISTANCE, ray.z * MAX_DISTANCE), blockCollision);
            if (hitResult.getType() == HitResult.Type.MISS) {
                break;
            }
            float distance = (float) hitResult.getLocation().distanceTo(startPos);
            if (distance == 0) {
                break;
            }
            journey += distance;
            Vec3 hitPos = hitResult.getLocation();
            var blockPos = hitResult.getBlockPos();
            var block = level.getBlockState(blockPos);
            var soundType = block.getSoundType(level, blockPos, null);
            HIT_POINTS.add(new HitPoint(round, new Vec3(hitPos.x, hitPos.y, hitPos.z), journey, distance, BlockSoundProperty.get(soundType), hitResult.getDirection()));
            switch (hitResult.getDirection()) {
                case UP, DOWN -> ray.mul(1, -1, 1);
                case NORTH, SOUTH -> ray.mul(1, 1, -1);
                case EAST, WEST -> ray.mul(-1, 1, 1);
            }
            startPos = hitPos;
        }
    }

    private static void setEaxReverb() {
        getSlot();
        alEffectf(REVERB_EFFECT, AL_EAXREVERB_DENSITY, density);
        alEffectf(REVERB_EFFECT, AL_EAXREVERB_DIFFUSION, diffusion);
        alEffectf(REVERB_EFFECT, AL_EAXREVERB_GAIN, 1);
        alEffectf(REVERB_EFFECT, AL_EAXREVERB_GAINHF, hfGain);
        alEffectf(REVERB_EFFECT, AL_EAXREVERB_GAINLF, 1);
        alEffectf(REVERB_EFFECT, AL_EAXREVERB_DECAY_TIME, rt60);
        alEffectf(REVERB_EFFECT, AL_EAXREVERB_DECAY_HFRATIO, hfGain);
        alEffectf(REVERB_EFFECT, AL_EAXREVERB_DECAY_LFRATIO, 1);
        alEffectf(REVERB_EFFECT, AL_EAXREVERB_REFLECTIONS_GAIN, earlyRefGain);
        alEffectf(REVERB_EFFECT, AL_EAXREVERB_REFLECTIONS_DELAY, earlyRefDelay);
        alEffectf(REVERB_EFFECT, AL_EAXREVERB_LATE_REVERB_GAIN, lateRefGain);
        alEffectf(REVERB_EFFECT, AL_EAXREVERB_LATE_REVERB_DELAY, lateRefDelay);
        alEffectf(REVERB_EFFECT, AL_EAXREVERB_ECHO_TIME, echoTime);
        alEffectf(REVERB_EFFECT, AL_EAXREVERB_ECHO_DEPTH, echoDepth);
        alEffectf(REVERB_EFFECT, AL_EAXREVERB_MODULATION_TIME, 0.4f);
        alEffectf(REVERB_EFFECT, AL_EAXREVERB_MODULATION_DEPTH, 0.025f);
        alAuxiliaryEffectSloti(AUX_SLOT, AL_EFFECTSLOT_EFFECT, REVERB_EFFECT);
    }

    private static void updateReflectionPan() {
        if (AUX_SLOT == -1) {
            return;
        }
        Vec3 earPos = getEarPos();
        Quaternionf inverseRotation = new Quaternionf(Minecraft.getInstance().gameRenderer.getMainCamera().rotation()).conjugate();

        Vector3f earlyPos = new Vector3f(
                (float) (earlyRefX - earPos.x),
                (float) (earlyRefY - earPos.y),
                (float) (earlyRefZ - earPos.z)
        );
        earlyPos.rotate(inverseRotation);
        alEffectfv(REVERB_EFFECT, AL_EAXREVERB_REFLECTIONS_PAN, new float[]{earlyPos.x, earlyPos.y, earlyPos.z});

        Vector3f latePos = new Vector3f(
                (float) (lateRefX - earPos.x),
                (float) (lateRefY - earPos.y),
                (float) (lateRefZ - earPos.z)
        );
        latePos.rotate(inverseRotation);
        alEffectfv(REVERB_EFFECT, AL_EAXREVERB_LATE_REVERB_PAN, new float[]{latePos.x, latePos.y, latePos.z});
    }

    public static void applySourceAudioData(OpenAlSource source) {
        if (source.isRelativeToListener()) {
            alSourcei(source.getSourceId(), AL_DIRECT_FILTER, AL_FILTER_NULL);
            alSource3i(source.getSourceId(), AL_AUXILIARY_SEND_FILTER, AL_EFFECTSLOT_NULL, 0, AL_FILTER_NULL);
            return;
        }
        Vec3 sourcePos = new Vec3(source.getPosX(), source.getPosY(), source.getPosZ());
        Vec3 earPos = getEarPos();
        if (sourcePos.distanceToSqr(earPos) > (double) MAX_DISTANCE * MAX_DISTANCE) {
            alFilterf(source.getDirectFilter(), AL_LOWPASS_GAIN, 1);
            alFilterf(source.getDirectFilter(), AL_LOWPASS_GAINHF, 1);
            alSourcei(source.getSourceId(), AL_DIRECT_FILTER, source.getDirectFilter());
            alSource3i(source.getSourceId(), AL_AUXILIARY_SEND_FILTER, AL_EFFECTSLOT_NULL, 0, AL_FILTER_NULL);
            return;
        }
        SourceAudioData data = SOURCE_AUDIO_DATA_CACHE.compute(sourcePos, (p, _) -> RayTraceCalculator.calculateSourceAudioData(p));
        alFilterf(source.getDirectFilter(), AL_LOWPASS_GAIN, data.directGain());
        alFilterf(source.getDirectFilter(), AL_LOWPASS_GAINHF, data.directHF());
        alSourcei(source.getSourceId(), AL_DIRECT_FILTER, source.getDirectFilter());

        alFilterf(source.getReverbFilter(), AL_LOWPASS_GAIN, data.reverbGain());
        alFilterf(source.getReverbFilter(), AL_LOWPASS_GAINHF, 1);
        alSource3i(source.getSourceId(), AL_AUXILIARY_SEND_FILTER, getSlot(), 0, source.getReverbFilter());

        alSource3f(source.getSourceId(), AL_POSITION, (float) data.virtualPos().x, (float) data.virtualPos().y, (float) data.virtualPos().z);
    }

    public static void reset() {
        synchronized (HIT_POINTS) {
            HIT_POINTS.clear();
        }
        SOURCE_AUDIO_DATA_CACHE.clear();
        AUX_SLOT = -1;
        REVERB_EFFECT = -1;
    }

    static Vec3 getEarPos() {
        return Minecraft.getInstance().gameRenderer.getMainCamera().position();
    }

    static int getRayAmount() {
        return 100;
    }

    private static ArrayList<Vector3d> generateRays(int totalRayCount, double offset) {
        final double phi = Math.PI * (3 - Math.sqrt(5));
        ArrayList<Vector3d> rays = new ArrayList<>(totalRayCount);
        for (int i = 0; i < totalRayCount; i++) {
            double y = 1 - ((i + offset) / (float) (totalRayCount - 1 + offset)) * 2;
            double r = Math.sqrt(1 - y * y);
            double theta = phi * (i + offset);
            rays.add(new Vector3d(Math.cos(theta) * r, y, Math.sin(theta) * r));
        }
        return rays;
    }

}
