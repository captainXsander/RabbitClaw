package ru.captainxsander;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;

class SettingsScreen extends AbstractDetailMenuScreen {
    private final Rectangle soundBounds = new Rectangle(3.0f, 3.8f, 10.0f, 0.9f);
    private final Rectangle musicBounds = new Rectangle(3.0f, 2.7f, 10.0f, 0.9f);
    private final Rectangle effectsBounds = new Rectangle(3.0f, 1.6f, 10.0f, 0.9f);
    private final Rectangle backBounds = new Rectangle(3.0f, 0.5f, 10.0f, 0.9f);

    SettingsScreen(MainGame game) {
        super(game);
        actionBounds.add(soundBounds);
        actionBounds.add(musicBounds);
        actionBounds.add(effectsBounds);
        actionBounds.add(backBounds);
    }

    @Override
    protected void drawContent() {
        drawMenuTitle("Настройки");
        drawParagraph(
            "Общие настройки звука интерфейса. Реализация влияния на аудиосистему будет добавлена отдельно.",
            new Rectangle(1.3f, 5.5f, 13.4f, 1.0f)
        );

        drawButton(soundBounds,
            "Звук игры: " + (game.isSoundEnabled() ? "Включен" : "Выключен"),
            selectedIndex == 0);
        drawButton(musicBounds,
            "Громкость музыки: " + Math.round(game.getMusicVolume() * 100f) + "%",
            selectedIndex == 1);
        drawButton(effectsBounds,
            "Громкость эффектов: " + Math.round(game.getEffectsVolume() * 100f) + "%",
            selectedIndex == 2);
        drawButton(backBounds, "Назад", selectedIndex == 3);
    }

    @Override
    protected int getActionCount() {
        return 4;
    }

    @Override
    protected void onActionTriggered(int actionIndex) {
        if (actionIndex == 0) {
            game.setSoundEnabled(!game.isSoundEnabled());
            return;
        }
        if (actionIndex == 1) {
            game.setMusicVolume(game.getMusicVolume() + 0.05f);
            return;
        }
        if (actionIndex == 2) {
            game.setEffectsVolume(game.getEffectsVolume() + 0.05f);
            return;
        }
        game.showPreviousMenu();
    }

    @Override
    protected boolean onExtraKeyDown(int keycode) {
        if (keycode == Input.Keys.LEFT || keycode == Input.Keys.A) {
            adjustVolume(-0.05f);
            return true;
        }
        if (keycode == Input.Keys.RIGHT || keycode == Input.Keys.D) {
            adjustVolume(0.05f);
            return true;
        }
        return false;
    }

    private void adjustVolume(float delta) {
        if (selectedIndex == 1) {
            game.setMusicVolume(game.getMusicVolume() + delta);
            return;
        }
        if (selectedIndex == 2) {
            game.setEffectsVolume(game.getEffectsVolume() + delta);
        }
    }
}
