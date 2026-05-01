package ru.captainxsander;

public class GameSessionSettings {
    private final ToyType[] normalToyPool;
    private final double baseSlipChance;
    private final float clawDropBaseChance;
    private final float clawDropMinChance;
    private final float baseFakeGrabChance;
    private final float clawGrabXMargin;
    private final float clawCatchChanceMult;

    public GameSessionSettings(
        ToyType[] normalToyPool,
        double baseSlipChance,
        float clawDropBaseChance,
        float clawDropMinChance,
        float baseFakeGrabChance,
        float clawGrabXMargin,
        float clawCatchChanceMult
    ) {
        this.normalToyPool = normalToyPool;
        this.baseSlipChance = baseSlipChance;
        this.clawDropBaseChance = clawDropBaseChance;
        this.clawDropMinChance = clawDropMinChance;
        this.baseFakeGrabChance = baseFakeGrabChance;
        this.clawGrabXMargin = clawGrabXMargin;
        this.clawCatchChanceMult = clawCatchChanceMult;
    }

    public static GameSessionSettings defaults() {
        return new GameSessionSettings(
            null,
            GameTuning.NORMAL_BASE_SLIP_CHANCE,
            GameTuning.NORMAL_CLAW_DROP_BASE_CHANCE,
            GameTuning.NORMAL_CLAW_DROP_MIN_CHANCE,
            GameTuning.NORMAL_BASE_FAKE_GRAB_CHANCE,
            GameTuning.NORMAL_CLAW_GRAB_X_MARGIN,
            GameTuning.NORMAL_CLAW_CATCH_CHANCE_MULT
        );
    }

    public ToyType[] getNormalToyPool() { return normalToyPool; }
    public double getBaseSlipChance() { return baseSlipChance; }
    public float getClawDropBaseChance() { return clawDropBaseChance; }
    public float getClawDropMinChance() { return clawDropMinChance; }
    public float getBaseFakeGrabChance() { return baseFakeGrabChance; }
    public float getClawGrabXMargin() { return clawGrabXMargin; }
    public float getClawCatchChanceMult() { return clawCatchChanceMult; }
}
