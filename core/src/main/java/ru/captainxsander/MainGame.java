package ru.captainxsander;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.Set;

public class MainGame extends Game {
    private final Deque<MenuId> menuHistory = new ArrayDeque<>();
    private MenuId currentMenuId;

    private boolean soundEnabled = true;
    private float musicVolume = 0.7f;
    private float effectsVolume = 0.8f;
    private Music gameMusic;
    private Sound clawDownSound;
    private Sound clawUpSound;
    private Sound moveToTraySound;
    private Sound failTraySound;
    private Sound toyWinnerSound;

    private double normalBaseSlipChance = GameTuning.BASE_SLIP_CHANCE;
    private float normalClawDropBaseChance = GameTuning.CLAW_DROP_BASE_CHANCE;
    private float normalClawDropMinChance = GameTuning.CLAW_DROP_MIN_CHANCE;
    private float normalBaseFakeGrabChance = GameTuning.BASE_FAKE_GRAB_CHANCE;
    private final Set<ToyType> normalSelectedToyTypes = new LinkedHashSet<>();

    @Override
    public void create() {
        initAudio();
        showMainMenu();
    }

    public void showMainMenu() {
        showMenu(MenuId.MAIN);
    }

    public void showGameModeMenu() {
        showMenu(MenuId.GAME_MODE);
    }

    public void showNormalModeSetupMenu() {
        showMenu(MenuId.NORMAL_MODE_SETUP);
    }

    public void showRescueModeSetupMenu() {
        showMenu(MenuId.RESCUE_MODE_SETUP);
    }

    public void showFindAnimalModeSetupMenu() {
        showMenu(MenuId.FIND_ANIMAL_MODE_SETUP);
    }

    public void showCatchCatModeSetupMenu() {
        showMenu(MenuId.CATCH_CAT_MODE_SETUP);
    }

    public void showSettings() {
        showMenu(MenuId.SETTINGS);
    }

    public void showMenagerie() {
        showMenu(MenuId.MENAGERIE);
    }

    public void showPreviousMenu() {
        if (menuHistory.isEmpty()) {
            showMainMenu();
            return;
        }

        MenuId previousMenuId = menuHistory.pop();
        currentMenuId = previousMenuId;
        switchScreen(createMenuScreen(previousMenuId));
    }

    public boolean startRescueGame() {
        MenagerieProgress progress = new MenagerieProgress();
        if (!progress.canSpendCoinForRescueAttempt()) {
            return false;
        }

        clearMenuNavigation();
        switchScreen(new GameScreen(this, GameMode.RESCUE, GameSessionSettings.defaults()));
        return true;
    }

    public void startNormalGame() {
        MenagerieProgress progress = new MenagerieProgress();
        ensureNormalToySelection(progress.getNormalModePool());

        clearMenuNavigation();
        switchScreen(new GameScreen(this, GameMode.NORMAL, buildNormalModeSettings(progress.getNormalModePool())));
    }

    public void startFindAnimalGame() {
        clearMenuNavigation();
        switchScreen(new GameScreen(this, GameMode.FIND_ANIMAL, GameSessionSettings.defaults()));
    }

    public void startCatchCatGame() {
        clearMenuNavigation();
        switchScreen(new GameScreen(this, GameMode.CATCH_CAT, GameSessionSettings.defaults()));
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void setSoundEnabled(boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
        applyAudioSettings();
    }

    public float getMusicVolume() {
        return musicVolume;
    }

    public void setMusicVolume(float musicVolume) {
        this.musicVolume = clamp01(musicVolume);
        applyAudioSettings();
    }

    public float getEffectsVolume() {
        return effectsVolume;
    }

    public void setEffectsVolume(float effectsVolume) {
        this.effectsVolume = clamp01(effectsVolume);
    }

    public void playClawDownSound() {
        playEffect(clawDownSound);
    }

    public void playClawUpSound() {
        playEffect(clawUpSound);
    }

    public void playMoveToTraySound() {
        playEffect(moveToTraySound);
    }

    public void playFailTraySound() {
        playEffect(failTraySound);
    }

    public void playToyWinnerSound() {
        playEffect(toyWinnerSound);
    }

    public double getNormalBaseSlipChance() {
        return normalBaseSlipChance;
    }

    public void setNormalBaseSlipChance(double normalBaseSlipChance) {
        this.normalBaseSlipChance = clamp(normalBaseSlipChance, 0.0, 0.95);
    }

    public float getNormalClawDropBaseChance() {
        return normalClawDropBaseChance;
    }

    public void setNormalClawDropBaseChance(float normalClawDropBaseChance) {
        this.normalClawDropBaseChance = (float) clamp(normalClawDropBaseChance, 0.0, 1.0);
    }

    public float getNormalClawDropMinChance() {
        return normalClawDropMinChance;
    }

    public void setNormalClawDropMinChance(float normalClawDropMinChance) {
        this.normalClawDropMinChance = (float) clamp(normalClawDropMinChance, 0.0, 1.0);
    }

    public float getNormalBaseFakeGrabChance() {
        return normalBaseFakeGrabChance;
    }

    public void setNormalBaseFakeGrabChance(float normalBaseFakeGrabChance) {
        this.normalBaseFakeGrabChance = (float) clamp(normalBaseFakeGrabChance, 0.0, 0.95);
    }

    public ToyType[] getNormalSelectedToyTypes(ToyType[] availablePool) {
        ensureNormalToySelection(availablePool);
        return normalSelectedToyTypes.toArray(new ToyType[0]);
    }

    public void toggleNormalToy(ToyType toyType, ToyType[] availablePool) {
        ensureNormalToySelection(availablePool);
        if (normalSelectedToyTypes.contains(toyType)) {
            if (normalSelectedToyTypes.size() > 1) {
                normalSelectedToyTypes.remove(toyType);
            }
            return;
        }

        normalSelectedToyTypes.add(toyType);
    }

    public GameSessionSettings buildNormalModeSettings(ToyType[] availablePool) {
        ensureNormalToySelection(availablePool);
        return new GameSessionSettings(
            normalSelectedToyTypes.toArray(new ToyType[0]),
            normalBaseSlipChance,
            normalClawDropBaseChance,
            normalClawDropMinChance,
            normalBaseFakeGrabChance
        );
    }

    private void ensureNormalToySelection(ToyType[] availablePool) {
        if (availablePool == null || availablePool.length == 0) {
            normalSelectedToyTypes.clear();
            normalSelectedToyTypes.addAll(Arrays.asList(ToyType.DEFAULT_POOL));
            return;
        }

        normalSelectedToyTypes.removeIf(toyType -> !Arrays.asList(availablePool).contains(toyType));
        if (!normalSelectedToyTypes.isEmpty()) {
            return;
        }

        normalSelectedToyTypes.addAll(Arrays.asList(availablePool));
    }

    private void showMenu(MenuId menuId) {
        if (currentMenuId != null && currentMenuId != menuId) {
            menuHistory.push(currentMenuId);
        }

        currentMenuId = menuId;
        switchScreen(createMenuScreen(menuId));
    }

    private Screen createMenuScreen(MenuId menuId) {
        switch (menuId) {
            case MAIN:
                return new MainMenuScreen(this);
            case GAME_MODE:
                return new GameModeMenuScreen(this);
            case NORMAL_MODE_SETUP:
                return new NormalModeSetupScreen(this);
            case RESCUE_MODE_SETUP:
                return new RescueModeSetupScreen(this);
            case FIND_ANIMAL_MODE_SETUP:
                return new FindAnimalModeSetupScreen(this);
            case CATCH_CAT_MODE_SETUP:
                return new CatchCatModeSetupScreen(this);
            case SETTINGS:
                return new SettingsScreen(this);
            case MENAGERIE:
                return new MenagerieScreen(this);
            default:
                throw new IllegalStateException("Unsupported menu: " + menuId);
        }
    }

    private void clearMenuNavigation() {
        currentMenuId = null;
        menuHistory.clear();
    }

    private void switchScreen(Screen newScreen) {
        Screen previousScreen = getScreen();
        setScreen(newScreen);
        if (previousScreen != null) {
            previousScreen.dispose();
        }
    }

    private static float clamp01(float value) {
        return Math.max(0f, Math.min(1f, value));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private void initAudio() {
        gameMusic = com.badlogic.gdx.Gdx.audio.newMusic(com.badlogic.gdx.Gdx.files.internal("sound/game_music.wav"));
        gameMusic.setLooping(true);
        clawDownSound = com.badlogic.gdx.Gdx.audio.newSound(com.badlogic.gdx.Gdx.files.internal("sound/claw_down.wav"));
        clawUpSound = com.badlogic.gdx.Gdx.audio.newSound(com.badlogic.gdx.Gdx.files.internal("sound/claw_up.wav"));
        moveToTraySound = com.badlogic.gdx.Gdx.audio.newSound(com.badlogic.gdx.Gdx.files.internal("sound/move_to_tray.wav"));
        failTraySound = com.badlogic.gdx.Gdx.audio.newSound(com.badlogic.gdx.Gdx.files.internal("sound/fail_tray.wav"));
        toyWinnerSound = com.badlogic.gdx.Gdx.audio.newSound(com.badlogic.gdx.Gdx.files.internal("sound/toy_win.mp3"));
        applyAudioSettings();
    }

    private void applyAudioSettings() {
        if (gameMusic == null) {
            return;
        }

        if (!soundEnabled || musicVolume <= 0f) {
            gameMusic.pause();
            return;
        }

        gameMusic.setVolume(musicVolume);
        if (!gameMusic.isPlaying()) {
            gameMusic.play();
        }
    }

    private void playEffect(Sound sound) {
        if (sound == null || !soundEnabled || effectsVolume <= 0f) {
            return;
        }
        sound.play(effectsVolume);
    }

    @Override
    public void dispose() {
        Screen currentScreen = getScreen();
        if (currentScreen != null) {
            currentScreen.dispose();
        }
        if (gameMusic != null) {
            gameMusic.dispose();
        }
        if (clawDownSound != null) {
            clawDownSound.dispose();
        }
        if (clawUpSound != null) {
            clawUpSound.dispose();
        }
        if (moveToTraySound != null) {
            moveToTraySound.dispose();
        }
        if (failTraySound != null) {
            failTraySound.dispose();
        }
        if (toyWinnerSound != null) {
            toyWinnerSound.dispose();
        }
        super.dispose();
    }

    private enum MenuId {
        MAIN,
        GAME_MODE,
        NORMAL_MODE_SETUP,
        RESCUE_MODE_SETUP,
        FIND_ANIMAL_MODE_SETUP,
        CATCH_CAT_MODE_SETUP,
        SETTINGS,
        MENAGERIE
    }
}
