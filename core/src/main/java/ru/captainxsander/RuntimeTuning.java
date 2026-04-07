package ru.captainxsander;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class RuntimeTuning {

    private static RuntimePreset preset = RuntimePreset.BALANCED;

    public static RuntimePreset getPreset() {
        return preset;
    }

    public static void updateInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) preset = RuntimePreset.SOFT;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) preset = RuntimePreset.BALANCED;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) preset = RuntimePreset.HARD;
    }

    public static float toyGravity() {
        switch (preset) {
            case SOFT: return 0.18f;
            case HARD: return 0.34f;
            default: return 0.26f;
        }
    }

    public static float damping() {
        switch (preset) {
            case SOFT: return 1.2f;
            case HARD: return 0.7f;
            default: return 0.9f;
        }
    }

    public static float swingDamping() {
        switch (preset) {
            case SOFT: return 0.85f;
            case HARD: return 0.75f;
            default: return 0.80f;
        }
    }
}
