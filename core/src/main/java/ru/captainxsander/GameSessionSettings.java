package ru.captainxsander;

public class GameSessionSettings {
    private final ToyType[] normalToyPool;
    private final double baseSlipChance;
    private final float clawDropBaseChance;
    private final float clawDropMinChance;
    private final float baseFakeGrabChance;

    public GameSessionSettings(
        ToyType[] normalToyPool,
        double baseSlipChance,
        float clawDropBaseChance,
        float clawDropMinChance,
        float baseFakeGrabChance
    ) {
        this.normalToyPool = normalToyPool;
        this.baseSlipChance = baseSlipChance;
        this.clawDropBaseChance = clawDropBaseChance;
        this.clawDropMinChance = clawDropMinChance;
        this.baseFakeGrabChance = baseFakeGrabChance;
    }

    public static GameSessionSettings defaults() {
        return new GameSessionSettings(
            null,
            GameTuning.BASE_SLIP_CHANCE,
            GameTuning.CLAW_DROP_BASE_CHANCE,
            GameTuning.CLAW_DROP_MIN_CHANCE,
            GameTuning.BASE_FAKE_GRAB_CHANCE
        );
    }

    public ToyType[] getNormalToyPool() {
        return normalToyPool;
    }

    public double getBaseSlipChance() {
        return baseSlipChance;
    }

    public float getClawDropBaseChance() {
        return clawDropBaseChance;
    }

    public float getClawDropMinChance() {
        return clawDropMinChance;
    }

    public float getBaseFakeGrabChance() {
        return baseFakeGrabChance;
    }
}
