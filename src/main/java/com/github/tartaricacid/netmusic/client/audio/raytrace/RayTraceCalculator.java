package com.github.tartaricacid.netmusic.client.audio.raytrace;

import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.function.Consumer;

import static com.github.tartaricacid.netmusic.client.audio.raytrace.RayTraceManager.*;

public class RayTraceCalculator {
    private static final ArrayList<Runnable> DEFAULT = new ArrayList<>();
    private static final ArrayList<Runnable> PRE = new ArrayList<>();
    private static final ArrayList<Consumer<HitPoint>> LOOP1 = new ArrayList<>();
    private static final ArrayList<Runnable> MID = new ArrayList<>();
    private static final ArrayList<Consumer<HitPoint>> LOOP2 = new ArrayList<>();
    private static final ArrayList<Runnable> POST = new ArrayList<>();

    static volatile int pointTotalAmount = 0;

    public static void run() {
        synchronized (HIT_POINTS) {
            pointTotalAmount = HIT_POINTS.size();
            if (pointTotalAmount < 2) {
                DEFAULT.forEach(Runnable::run);
                return;
            }
            PRE.forEach(Runnable::run);
            for (HitPoint hitPoint : HIT_POINTS) {
                LOOP1.forEach(c -> c.accept(hitPoint));
            }
            MID.forEach(Runnable::run);
            for (HitPoint hitPoint : HIT_POINTS) {
                LOOP2.forEach(c -> c.accept(hitPoint));
            }
            POST.forEach(Runnable::run);
        }
    }

    //----------Density----------
    static volatile float density;
    static double journeySum;
    static float journeyMean;
    static float journeySquaredDiffSum;
    static float journeyStandardDeviation;

    static {
        def(() -> density = 0);
        pre(() -> journeySum = 0);
        loop1(h -> journeySum += h.journey());
        mid(() -> {
            journeyMean = (float) (journeySum / pointTotalAmount);
            journeySquaredDiffSum = 0;
        });
        loop2(h -> {
            float diff = h.journey() - journeyMean;
            journeySquaredDiffSum += diff * diff;
        });
        post(() -> {
            journeyStandardDeviation = (float) Math.sqrt(journeySquaredDiffSum / pointTotalAmount);
            density = Mth.clamp(journeyStandardDeviation / (MAX_DISTANCE / 2f), 0, 1);
        });
    }

    //----------Diffusion----------
    static volatile float diffusion;
    static double roughnessWeightedTotal;
    static double weightTotal;
    static double absorptionWeightedTotal;

    static {
        def(() -> diffusion = 0);
        pre(() -> {
            weightTotal = 0;
            roughnessWeightedTotal = 0;
            absorptionWeightedTotal = 0;
        });
        loop1(h -> {
            double weight = h.weight();
            roughnessWeightedTotal += h.soundProperty().roughness() * weight;
            absorptionWeightedTotal += h.soundProperty().absorption() * weight;
            weightTotal += weight;
        });
        post(() -> diffusion = Mth.clamp((float) (roughnessWeightedTotal / weightTotal), 0, 1));
    }

    //----------HF Gain----------
    static volatile float hfGain;
    static double hfWeightedTotal;

    static {
        def(() -> hfGain = 0.9f);
        pre(() -> hfWeightedTotal = 0);
        loop1(h -> {
            double weight = h.weight();
            hfWeightedTotal += h.soundProperty().hfGain() * weight;
        });
        post(() -> {
            double airHfGain = Math.pow(0.99, journeyMean);
            hfGain = Mth.clamp((float) Math.sqrt(airHfGain * hfWeightedTotal / weightTotal), 0, 2);
        });
    }

    //----------RT60----------
    static volatile float rt60;
    static double distanceSum;
    static float distanceMean;
    static float openSpaceCorrection;

    static {
        def(() -> rt60 = 1.5f);
        pre(() -> {
            distanceSum = 0;
            distanceMean = 0;
        });
        loop1(h -> distanceSum += h.distance());
        mid(() -> distanceMean = (float) (distanceSum / pointTotalAmount));
        post(() -> {
            double collisionCount = -6 / Math.log10(1 - absorptionWeightedTotal / weightTotal);
            openSpaceCorrection = Mth.clamp(pointTotalAmount / ((float) getRayAmount() * MAX_BOUNCE_ROUND * 2), 0, 1);
            double r = collisionCount * distanceMean / 340 * openSpaceCorrection;
            rt60 = (float) Mth.clamp(20 - 400 / (r + 20), 0.1, 20);
        });
    }

    //----------Early Reflection----------
    static volatile float earlyRefGain;
    static double earlyRefGainWeightedTotal;
    static int earlyRefPointAmount;

    static volatile float earlyRefDelay;
    static double earlyRefWeightedTotal;
    static double earlyRefJourneyWeightedTotal;

    static volatile double earlyRefX, earlyRefY, earlyRefZ;
    static double earlyRefXWeightedTotal, earlyRefYWeightedTotal, earlyRefZWeightedTotal;

    static {
        def(() -> {
            earlyRefGain = 0.05f;
            earlyRefDelay = 0.007f;
            earlyRefX = earlyRefY = earlyRefZ = 0;
        });
        pre(() -> {
            earlyRefGainWeightedTotal = 0;
            earlyRefPointAmount = 0;
            earlyRefWeightedTotal = 0;
            earlyRefJourneyWeightedTotal = 0;
            earlyRefXWeightedTotal = 0;
            earlyRefYWeightedTotal = 0;
            earlyRefZWeightedTotal = 0;
        });
        loop1(h -> {
            if (h.journey() <= 17) {
                double weight = h.weight();
                earlyRefGainWeightedTotal += (1 - h.soundProperty().absorption()) * weight;
                earlyRefPointAmount++;
                earlyRefJourneyWeightedTotal += h.journey() * weight;
                earlyRefWeightedTotal += weight;
                earlyRefXWeightedTotal += h.pos().x * weight;
                earlyRefYWeightedTotal += h.pos().y * weight;
                earlyRefZWeightedTotal += h.pos().z * weight;
            }
        });
        post(() -> {
            earlyRefGain = Mth.clamp(2 * (float) Math.sqrt(earlyRefGainWeightedTotal / earlyRefPointAmount) * openSpaceCorrection, 0, 3.16f);
            double earlyRefJourneyWeightedMean = earlyRefJourneyWeightedTotal / earlyRefWeightedTotal;
            earlyRefDelay = (float) (earlyRefJourneyWeightedMean / 340);
            earlyRefX = earlyRefXWeightedTotal / earlyRefWeightedTotal;
            earlyRefY = earlyRefYWeightedTotal / earlyRefWeightedTotal;
            earlyRefZ = earlyRefZWeightedTotal / earlyRefWeightedTotal;
        });
    }

    //----------Late Reflection----------
    static volatile float lateRefGain;
    static double lateRefGainWeightedTotal;
    static int lateRefPointAmount;

    static volatile float lateRefDelay;
    static double lateRefWeightedTotal;
    static double lateRefJourneyWeightedTotal;

    static volatile double lateRefX, lateRefY, lateRefZ;
    static double lateRefXWeightedTotal, lateRefYWeightedTotal, lateRefZWeightedTotal;

    static {
        def(() -> {
            lateRefGain = 0.5f;
            lateRefDelay = 0.01f;
            lateRefX = lateRefY = lateRefZ = 0;
        });
        pre(() -> {
            lateRefGainWeightedTotal = 0;
            lateRefPointAmount = 0;
            lateRefWeightedTotal = 0;
            lateRefJourneyWeightedTotal = 0;
            lateRefXWeightedTotal = 0;
            lateRefYWeightedTotal = 0;
            lateRefZWeightedTotal = 0;
        });
        loop1(h -> {
            if (h.journey() > 17 && h.journey() <= 34) {
                double weight = h.weight();
                lateRefGainWeightedTotal += (1 - h.soundProperty().absorption()) * weight;
                lateRefPointAmount++;
                lateRefJourneyWeightedTotal += h.journey() * weight;
                lateRefWeightedTotal += weight;
                lateRefXWeightedTotal += h.pos().x * weight;
                lateRefYWeightedTotal += h.pos().y * weight;
                lateRefZWeightedTotal += h.pos().z * weight;
            }
        });
        post(() -> {
            lateRefGain = Mth.clamp(20 * (float) Math.sqrt(lateRefGainWeightedTotal / lateRefPointAmount) * openSpaceCorrection, 0, 10);
            double lateRefJourneyWeightedMean = lateRefJourneyWeightedTotal / lateRefWeightedTotal;
            lateRefDelay = (float) (lateRefJourneyWeightedMean / 340) - earlyRefDelay;
            lateRefX = lateRefXWeightedTotal / lateRefWeightedTotal;
            lateRefY = lateRefYWeightedTotal / lateRefWeightedTotal;
            lateRefZ = lateRefZWeightedTotal / lateRefWeightedTotal;
        });
    }

    //----------Echo----------
    static double xEnergy, yEnergy, zEnergy;
    static double xDistance, yDistance, zDistance;
    static int xCount, yCount, zCount;
    static double xDistanceSqSum, yDistanceSqSum, zDistanceSqSum;
    static double xRoughnessSum, yRoughnessSum, zRoughnessSum;

    static volatile float echoTime;
    static volatile float echoDepth;

    static {
        def(() -> {
            echoTime = 0.25f;
            echoDepth = 0;
        });
        pre(() -> {
            xEnergy = 0;
            yEnergy = 0;
            zEnergy = 0;
            xDistance = 0;
            yDistance = 0;
            zDistance = 0;
            xCount = 0;
            yCount = 0;
            zCount = 0;
            xDistanceSqSum = 0;
            yDistanceSqSum = 0;
            zDistanceSqSum = 0;
            xRoughnessSum = 0;
            yRoughnessSum = 0;
            zRoughnessSum = 0;
        });
        loop1(h -> {
            switch (h.faceDirection().getAxis()) {
                case X -> {
                    xEnergy += h.weight() * (1 - h.soundProperty().absorption());
                    if (h.round() > 0) {
                        xDistance += h.distance();
                        xCount++;
                        xDistanceSqSum += h.distance() * h.distance();
                        xRoughnessSum += h.soundProperty().roughness();
                    }
                }
                case Y -> {
                    yEnergy += h.weight() * (1 - h.soundProperty().absorption());
                    if (h.round() > 0) {
                        yDistance += h.distance();
                        yCount++;
                        yDistanceSqSum += h.distance() * h.distance();
                        yRoughnessSum += h.soundProperty().roughness();
                    }
                }
                case Z -> {
                    zEnergy += h.weight() * (1 - h.soundProperty().absorption());
                    if (h.round() > 0) {
                        zDistance += h.distance();
                        zCount++;
                        zDistanceSqSum += h.distance() * h.distance();
                        zRoughnessSum += h.soundProperty().roughness();
                    }
                }
            }
        });
        mid(() -> {
            double totalEnergy = xEnergy + yEnergy + zEnergy;
            xEnergy = xEnergy / totalEnergy;
            yEnergy = yEnergy / totalEnergy;
            zEnergy = zEnergy / totalEnergy;
        });
        loop2(h -> {
        });
        post(() -> {
            double avgXDistance = xDistance / xCount;
            double avgYDistance = yDistance / yCount;
            double avgZDistance = zDistance / zCount;
            double avgDistance = avgXDistance * xEnergy + avgYDistance * yEnergy + avgZDistance * zEnergy;
            echoTime = (float) Mth.clamp(4 * avgDistance / 340, 0.075, 0.25);

            double xVar = xDistanceSqSum / xCount - avgXDistance * avgXDistance;
            double xCv = Math.sqrt(xVar) / avgXDistance;
            double xEchoDepth = (1.0 / (1.0 + xCv)) * (1.0 - xRoughnessSum / xCount);

            double yVar = yDistanceSqSum / yCount - avgYDistance * avgYDistance;
            double yCv = Math.sqrt(yVar) / avgYDistance;
            double yEchoDepth = (1.0 / (1.0 + yCv)) * (1.0 - yRoughnessSum / yCount);

            double zVar = zDistanceSqSum / zCount - avgZDistance * avgZDistance;
            double zCv = Math.sqrt(zVar) / avgZDistance;
            double zEchoDepth = (1.0 / (1.0 + zCv)) * (1.0 - zRoughnessSum / zCount);

            echoDepth = (float) Mth.clamp(xEchoDepth * xEnergy + yEchoDepth * yEnergy + zEchoDepth * zEnergy, 0, 1);
        });
    }

    //----------Util----------
    private static void def(Runnable r) {
        DEFAULT.add(r);
    }

    private static void pre(Runnable r) {
        PRE.add(r);
    }

    private static void loop1(Consumer<HitPoint> c) {
        LOOP1.add(c);
    }

    private static void mid(Runnable r) {
        MID.add(r);
    }

    private static void loop2(Consumer<HitPoint> c) {
        LOOP2.add(c);
    }

    private static void post(Runnable r) {
        POST.add(r);
    }

    //----------Direct Sound----------
    static SourceAudioData calculateSourceAudioData(Vec3 sourcePos) {
        var cameraPos = getEarPos();
        var tuple = calculateVirtualDirection(sourcePos);
        double meanReverbWeight = tuple.getA();
        var hitA = RayTraceHelper.shoot(sourcePos, cameraPos, ClipContext.Block.OUTLINE);
        float wallThickness;
        if (hitA.getType() == HitResult.Type.MISS) {
            wallThickness = 0;
        } else {
            var hitB = RayTraceHelper.shoot(cameraPos, sourcePos, ClipContext.Block.OUTLINE);
            wallThickness = (float) hitA.getLocation().distanceTo(hitB.getLocation());
        }
        double wallDecay = Math.pow(0.25, wallThickness);
        double directWeight = wallDecay / Math.max(1, cameraPos.distanceTo(sourcePos));
        //double directWeight = wallDecay / Math.max(1, Mth.sqrt((float) cameraPos.distanceTo(sourcePos)));
        var virtualPos = tuple.getB();
        double directDirectionWeight = 10 * Math.pow(0.1, wallThickness);
        var finalPos = virtualPos.scale(meanReverbWeight).add(sourcePos.scale(directDirectionWeight)).scale(1 / (meanReverbWeight + directDirectionWeight));
        float directGain = (float) (Math.min(directWeight, 1));
        float directHF = (float) wallDecay;
        float reverbGain = (float) (Math.sqrt(Math.min(1 - Math.pow(0.001, meanReverbWeight), 1)));
        return new SourceAudioData(directGain, directHF, reverbGain, finalPos);
    }

    private static Tuple<Double, Vec3> calculateVirtualDirection(Vec3 sourcePos) {
        synchronized (HIT_POINTS) {
            double weightXSum = 0, weightYSum = 0, weightZSum = 0, totalWeight = 0;
            int n = 0;
            flag:
            for (int i = 0; i < HIT_POINTS.size(); ++i) {
                var hitPoint = HIT_POINTS.get(i);
                if (hitPoint.round() != 0) {
                    continue;
                }
                HitPoint subHitPoint = null;
                double journeyDecay = 1;

                if (RayTraceHelper.canSee(sourcePos, hitPoint.pos())) {
                    subHitPoint = hitPoint;
                    journeyDecay = 1 - hitPoint.soundProperty().absorption();
                } else if (i == HIT_POINTS.size() - 1) {
                    break;
                } else {
                    for (int j = 1; j < MAX_BOUNCE_ROUND; j++) {
                        var p = HIT_POINTS.get(i + j);
                        if (p.round() == 0) {
                            continue flag;
                        }
                        journeyDecay *= (1 - p.soundProperty().absorption());
                        if (RayTraceHelper.canSee(sourcePos, p.pos())) {
                            subHitPoint = p;
                            break;
                        }
                    }
                    if (subHitPoint == null) {
                        continue;
                    }
                }
                double distance = sourcePos.distanceTo(subHitPoint.pos());
                double journey = distance + subHitPoint.journey();
                double weight = journeyDecay / (journey * journey);
                weightXSum += weight * hitPoint.pos().x;
                weightYSum += weight * hitPoint.pos().y;
                weightZSum += weight * hitPoint.pos().z;
                totalWeight += weight;
                n++;
            }
            if (totalWeight == 0) {
                return new Tuple<>(0d, new Vec3(0, 0, 0));
            }
            return new Tuple<>(totalWeight / n, new Vec3(weightXSum / totalWeight, weightYSum / totalWeight, weightZSum / totalWeight));
        }
    }
}
