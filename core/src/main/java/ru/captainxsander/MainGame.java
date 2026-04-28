package ru.captainxsander;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
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
    private Music gameMusicA;
    private Music gameMusicB;
    private Sound clawDownSound;
    private Sound clawUpSound;
    private Sound moveToTraySound;
    private Sound failTraySound;
    private Sound toyWinnerSound;
    private long clawDownSoundId = -1L;
    private long clawUpSoundId = -1L;
    private long moveToTraySoundId = -1L;
    private float clawDownFadeRemaining = -1f;
    private float clawUpFadeRemaining = -1f;
    private float moveToTrayFadeRemaining = -1f;

    private static final float CLAW_DOWN_FADE_DURATION = 0.10f;
    private static final float CLAW_UP_FADE_DURATION = 0.14f;
    private static final float MOVE_TO_TRAY_FADE_DURATION = 0.18f;
    private static final float MUSIC_OVERLAP_DURATION_DESKTOP = 0.08f;
    private static final float MUSIC_OVERLAP_DURATION_ANDROID = 0.14f;
    private static final float MUSIC_CROSSFADE_START_GUARD_ANDROID = 0.03f;
    private static final float MIN_TRACK_DURATION_FOR_OVERLAP = 1.0f;
    private static final String GAME_MUSIC_PATH = "sound/game_music.wav";

    private int activeMusicIndex = -1;
    private float gameMusicDuration = -1f;
    private boolean crossfadeInProgress = false;
    private float crossfadeProgress = 0f;
    private boolean backgroundMusicPausedBySettings = false;

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
        if (!soundEnabled) {
            forceStopClawMotionSounds();
        }
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
        forceStopClawDownSound();
        clawDownFadeRemaining = -1f;
        clawDownSoundId = playEffect(clawDownSound, effectsVolume, false);
    }

    public void playClawUpSound() {
        forceStopClawUpSound();
        clawUpFadeRemaining = -1f;
        clawUpSoundId = playEffect(clawUpSound, effectsVolume, false);
    }

    public void playMoveToTraySound() {
        forceStopMoveToTraySound();
        moveToTrayFadeRemaining = -1f;
        moveToTraySoundId = playEffect(moveToTraySound, effectsVolume, true);
    }

    public void stopClawDownSound() {
        startFadeOut(clawDownSoundId, CLAW_DOWN_FADE_DURATION, true, false, false);
    }

    public void stopClawUpSound() {
        startFadeOut(clawUpSoundId, CLAW_UP_FADE_DURATION, false, true, false);
    }

    public void stopMoveToTraySound() {
        startFadeOut(moveToTraySoundId, MOVE_TO_TRAY_FADE_DURATION, false, false, true);
    }

    public void stopClawMotionSounds() {
        stopClawDownSound();
        stopClawUpSound();
        stopMoveToTraySound();
    }

    public void playFailTraySound() {
        playEffect(failTraySound, effectsVolume, false);
    }

    public void playToyWinnerSound() {
        playEffect(toyWinnerSound, effectsVolume, false);
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
        gameMusicDuration = readWavDurationSeconds(GAME_MUSIC_PATH);

        gameMusicA = com.badlogic.gdx.Gdx.audio.newMusic(com.badlogic.gdx.Gdx.files.internal(GAME_MUSIC_PATH));
        gameMusicA.setLooping(false);
        gameMusicB = com.badlogic.gdx.Gdx.audio.newMusic(com.badlogic.gdx.Gdx.files.internal(GAME_MUSIC_PATH));
        gameMusicB.setLooping(false);
        clawDownSound = com.badlogic.gdx.Gdx.audio.newSound(com.badlogic.gdx.Gdx.files.internal("sound/claw_down.wav"));
        clawUpSound = com.badlogic.gdx.Gdx.audio.newSound(com.badlogic.gdx.Gdx.files.internal("sound/claw_up.wav"));
        moveToTraySound = com.badlogic.gdx.Gdx.audio.newSound(com.badlogic.gdx.Gdx.files.internal("sound/move_to_tray.wav"));
        failTraySound = com.badlogic.gdx.Gdx.audio.newSound(com.badlogic.gdx.Gdx.files.internal("sound/fail_tray.wav"));
        toyWinnerSound = com.badlogic.gdx.Gdx.audio.newSound(com.badlogic.gdx.Gdx.files.internal("sound/toy_win.mp3"));
        applyAudioSettings();
    }

    private void applyAudioSettings() {
        if (gameMusicA == null || gameMusicB == null) {
            return;
        }

        if (!soundEnabled || musicVolume <= 0f) {
            gameMusicA.pause();
            gameMusicB.pause();
            backgroundMusicPausedBySettings = true;
            return;
        }

        ensureMusicLoopStarted();
        if (backgroundMusicPausedBySettings) {
            resumeBackgroundMusic();
            return;
        }
        updateMusicVolumes(0f);
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        updateBackgroundMusic(delta);
        updateMotionSoundFades(delta);
        super.render();
    }


    private void ensureMusicLoopStarted() {
        if (activeMusicIndex != -1) {
            return;
        }

        activeMusicIndex = 0;
        crossfadeInProgress = false;
        crossfadeProgress = 0f;
        Music activeMusic = getMusicByIndex(activeMusicIndex);
        activeMusic.stop();
        activeMusic.setVolume(musicVolume);
        activeMusic.play();
    }

    private void updateBackgroundMusic(float delta) {
        if (gameMusicA == null || gameMusicB == null) {
            return;
        }

        if (!soundEnabled || musicVolume <= 0f) {
            gameMusicA.pause();
            gameMusicB.pause();
            backgroundMusicPausedBySettings = true;
            return;
        }

        ensureMusicLoopStarted();
        if (backgroundMusicPausedBySettings) {
            resumeBackgroundMusic();
            return;
        }

        Music activeMusic = getMusicByIndex(activeMusicIndex);
        Music standbyMusic = getMusicByIndex(1 - activeMusicIndex);

        if (!activeMusic.isPlaying()) {
            forceSwitchToStandby();
            return;
        }

        if (isOverlapAllowed() && !crossfadeInProgress && shouldStartCrossfade(activeMusic)) {
            standbyMusic.stop();
            standbyMusic.setVolume(0f);
            standbyMusic.play();
            crossfadeInProgress = true;
            crossfadeProgress = 0f;
        }

        updateMusicVolumes(delta);
    }

    private void forceSwitchToStandby() {
        Music previousActive = getMusicByIndex(activeMusicIndex);
        previousActive.stop();
        previousActive.setVolume(0f);

        activeMusicIndex = 1 - activeMusicIndex;
        Music newActive = getMusicByIndex(activeMusicIndex);
        if (!newActive.isPlaying() || !crossfadeInProgress) {
            newActive.stop();
            newActive.play();
        }
        newActive.setVolume(musicVolume);

        if (crossfadeInProgress) {
            getMusicByIndex(1 - activeMusicIndex).setVolume(0f);
        }
        crossfadeInProgress = false;
        crossfadeProgress = 0f;
    }

    private void updateMusicVolumes(float delta) {
        if (activeMusicIndex == -1) {
            return;
        }

        Music activeMusic = getMusicByIndex(activeMusicIndex);
        Music standbyMusic = getMusicByIndex(1 - activeMusicIndex);
        if (!crossfadeInProgress) {
            activeMusic.setVolume(musicVolume);
            standbyMusic.setVolume(0f);
            return;
        }

        float overlapDuration = getMusicOverlapDuration();
        crossfadeProgress = clamp01(crossfadeProgress + (delta / overlapDuration));
        float smoothProgress = crossfadeProgress * crossfadeProgress * (3f - 2f * crossfadeProgress);
        activeMusic.setVolume(musicVolume * (1f - smoothProgress));
        standbyMusic.setVolume(musicVolume * smoothProgress);

        if (crossfadeProgress >= 1f) {
            forceSwitchToStandby();
        }
    }

    private boolean shouldStartCrossfade(Music activeMusic) {
        float activePosition = activeMusic.getPosition();
        return activePosition >= gameMusicDuration - getMusicOverlapDuration() - getCrossfadeStartGuard();
    }

    private boolean isOverlapAllowed() {
        return gameMusicDuration >= MIN_TRACK_DURATION_FOR_OVERLAP
            && gameMusicDuration > getMusicOverlapDuration();
    }

    private float getMusicOverlapDuration() {
        return isAndroidRuntime() ? MUSIC_OVERLAP_DURATION_ANDROID : MUSIC_OVERLAP_DURATION_DESKTOP;
    }

    private float getCrossfadeStartGuard() {
        return isAndroidRuntime() ? MUSIC_CROSSFADE_START_GUARD_ANDROID : 0f;
    }

    private boolean isAndroidRuntime() {
        return Gdx.app != null && Gdx.app.getType() == com.badlogic.gdx.Application.ApplicationType.Android;
    }

    private void resumeBackgroundMusic() {
        if (activeMusicIndex == -1) {
            return;
        }

        Music activeMusic = getMusicByIndex(activeMusicIndex);
        if (!activeMusic.isPlaying()) {
            activeMusic.play();
        }

        if (crossfadeInProgress) {
            Music standbyMusic = getMusicByIndex(1 - activeMusicIndex);
            if (!standbyMusic.isPlaying()) {
                standbyMusic.play();
            }
        }

        backgroundMusicPausedBySettings = false;
        updateMusicVolumes(0f);
    }

    private Music getMusicByIndex(int index) {
        return index == 0 ? gameMusicA : gameMusicB;
    }

    private float readWavDurationSeconds(String path) {
        try (DataInputStream input = new DataInputStream(new BufferedInputStream(Gdx.files.internal(path).read()))) {
            byte[] riffHeader = new byte[12];
            input.readFully(riffHeader);
            if (!matchesChunkId(riffHeader, 0, 'R', 'I', 'F', 'F') || !matchesChunkId(riffHeader, 8, 'W', 'A', 'V', 'E')) {
                return -1f;
            }

            int byteRate = -1;
            int dataSize = -1;

            while (input.available() > 0) {
                byte[] chunkHeader = new byte[8];
                input.readFully(chunkHeader);
                int chunkSize = toLittleEndianInt(chunkHeader, 4);

                if (matchesChunkId(chunkHeader, 0, 'f', 'm', 't', ' ')) {
                    byte[] fmtChunk = new byte[chunkSize];
                    input.readFully(fmtChunk);
                    if (fmtChunk.length >= 12) {
                        byteRate = toLittleEndianInt(fmtChunk, 8);
                    }
                } else if (matchesChunkId(chunkHeader, 0, 'd', 'a', 't', 'a')) {
                    dataSize = chunkSize;
                    input.skipBytes(chunkSize);
                } else {
                    input.skipBytes(chunkSize);
                }

                if ((chunkSize & 1) == 1) {
                    input.skipBytes(1);
                }
            }

            if (byteRate <= 0 || dataSize <= 0) {
                return -1f;
            }

            return (float) dataSize / (float) byteRate;
        } catch (IOException ignored) {
            return -1f;
        }
    }

    private static boolean matchesChunkId(byte[] bytes, int offset, char c1, char c2, char c3, char c4) {
        return bytes[offset] == (byte) c1
            && bytes[offset + 1] == (byte) c2
            && bytes[offset + 2] == (byte) c3
            && bytes[offset + 3] == (byte) c4;
    }

    private static int toLittleEndianInt(byte[] bytes, int offset) {
        return (bytes[offset] & 0xFF)
            | ((bytes[offset + 1] & 0xFF) << 8)
            | ((bytes[offset + 2] & 0xFF) << 16)
            | ((bytes[offset + 3] & 0xFF) << 24);
    }

    private long playEffect(Sound sound, float volume, boolean loop) {
        if (sound == null || !soundEnabled || effectsVolume <= 0f) {
            return -1L;
        }
        float clampedVolume = clamp01(volume);
        return loop ? sound.loop(clampedVolume) : sound.play(clampedVolume);
    }

    private void startFadeOut(long soundId, float duration, boolean clawDown, boolean clawUp, boolean moveToTray) {
        if (soundId == -1L) {
            return;
        }
        if (clawDown) {
            clawDownFadeRemaining = duration;
        }
        if (clawUp) {
            clawUpFadeRemaining = duration;
        }
        if (moveToTray) {
            moveToTrayFadeRemaining = duration;
        }
    }

    private void updateMotionSoundFades(float delta) {
        clawDownFadeRemaining = updateSingleFade(clawDownSound, clawDownSoundId, clawDownFadeRemaining, CLAW_DOWN_FADE_DURATION, effectsVolume, delta);
        if (clawDownFadeRemaining == 0f) {
            forceStopClawDownSound();
        }
        clawUpFadeRemaining = updateSingleFade(clawUpSound, clawUpSoundId, clawUpFadeRemaining, CLAW_UP_FADE_DURATION, effectsVolume, delta);
        if (clawUpFadeRemaining == 0f) {
            forceStopClawUpSound();
        }

        moveToTrayFadeRemaining = updateSingleFade(moveToTraySound, moveToTraySoundId, moveToTrayFadeRemaining, MOVE_TO_TRAY_FADE_DURATION, effectsVolume, delta);
        if (moveToTrayFadeRemaining == 0f) {
            forceStopMoveToTraySound();
        }
    }

    private float updateSingleFade(Sound sound, long soundId, float fadeRemaining, float fadeDuration, float targetVolume, float delta) {
        if (sound == null || soundId == -1L || fadeRemaining < 0f) {
            return fadeRemaining;
        }

        float next = fadeRemaining - delta;
        if (next <= 0f) {
            sound.setVolume(soundId, 0f);
            return 0f;
        }

        float progress = next / fadeDuration;
        sound.setVolume(soundId, clamp01(targetVolume * progress));
        return next;
    }

    private void forceStopClawMotionSounds() {
        forceStopClawDownSound();
        forceStopClawUpSound();
        forceStopMoveToTraySound();
    }

    private void forceStopClawDownSound() {
        if (clawDownSound != null && clawDownSoundId != -1L) {
            clawDownSound.stop(clawDownSoundId);
            clawDownSoundId = -1L;
        }
        clawDownFadeRemaining = -1f;
    }

    private void forceStopClawUpSound() {
        if (clawUpSound != null && clawUpSoundId != -1L) {
            clawUpSound.stop(clawUpSoundId);
            clawUpSoundId = -1L;
        }
        clawUpFadeRemaining = -1f;
    }

    private void forceStopMoveToTraySound() {
        if (moveToTraySound != null && moveToTraySoundId != -1L) {
            moveToTraySound.stop(moveToTraySoundId);
            moveToTraySoundId = -1L;
        }
        moveToTrayFadeRemaining = -1f;
    }

    @Override
    public void dispose() {
        forceStopClawMotionSounds();
        Screen currentScreen = getScreen();
        if (currentScreen != null) {
            currentScreen.dispose();
        }
        if (gameMusicA != null) {
            gameMusicA.dispose();
        }
        if (gameMusicB != null) {
            gameMusicB.dispose();
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
