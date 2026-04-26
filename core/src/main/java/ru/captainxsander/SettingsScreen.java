package ru.captainxsander;

import com.badlogic.gdx.math.Rectangle;

class SettingsScreen extends AbstractDetailMenuScreen {
    private final Rectangle soundBounds = new Rectangle(3.0f, 4.25f, 10.0f, 0.85f);
    private final Rectangle musicMinusBounds = new Rectangle(3.0f, 3.15f, 1.2f, 0.85f);
    private final Rectangle musicBounds = new Rectangle(4.3f, 3.15f, 7.4f, 0.85f);
    private final Rectangle musicPlusBounds = new Rectangle(11.8f, 3.15f, 1.2f, 0.85f);
    private final Rectangle effectsMinusBounds = new Rectangle(3.0f, 2.05f, 1.2f, 0.85f);
    private final Rectangle effectsBounds = new Rectangle(4.3f, 2.05f, 7.4f, 0.85f);
    private final Rectangle effectsPlusBounds = new Rectangle(11.8f, 2.05f, 1.2f, 0.85f);
    private final Rectangle backBounds = new Rectangle(3.0f, 0.95f, 10.0f, 0.85f);

    SettingsScreen(MainGame game) {
        super(game);
        actionBounds.add(soundBounds);
        actionBounds.add(musicMinusBounds);
        actionBounds.add(musicPlusBounds);
        actionBounds.add(effectsMinusBounds);
        actionBounds.add(effectsPlusBounds);
        actionBounds.add(backBounds);
    }

    @Override
    protected void drawContent() {
        drawMenuTitle("Настройки");
        drawParagraph(
            "Общие настройки звука меню и игрового процесса.",
            new Rectangle(2.4f, 5.75f, 11.2f, 0.85f)
        );

        drawButton(soundBounds,
            "Звук игры: " + (game.isSoundEnabled() ? "Включен" : "Выключен"),
            selectedIndex == 0);

        drawButton(musicMinusBounds, "-", selectedIndex == 1);
        drawButton(musicBounds,
            "Громкость музыки: " + Math.round(game.getMusicVolume() * 100f) + "%",
            false);
        drawButton(musicPlusBounds, "+", selectedIndex == 2);

        drawButton(effectsMinusBounds, "-", selectedIndex == 3);
        drawButton(effectsBounds,
            "Громкость эффектов: " + Math.round(game.getEffectsVolume() * 100f) + "%",
            false);
        drawButton(effectsPlusBounds, "+", selectedIndex == 4);

        drawButton(backBounds, "Назад", selectedIndex == 5);
    }

    @Override
    protected int getActionCount() {
        return 6;
    }

    @Override
    protected void onActionTriggered(int actionIndex) {
        if (actionIndex == 0) {
            game.setSoundEnabled(!game.isSoundEnabled());
            return;
        }
        if (actionIndex == 1) {
            game.setMusicVolume(game.getMusicVolume() - 0.05f);
            return;
        }
        if (actionIndex == 2) {
            game.setMusicVolume(game.getMusicVolume() + 0.05f);
            return;
        }
        if (actionIndex == 3) {
            game.setEffectsVolume(game.getEffectsVolume() - 0.05f);
            return;
        }
        if (actionIndex == 4) {
            game.setEffectsVolume(game.getEffectsVolume() + 0.05f);
            return;
        }
        game.showPreviousMenu();
    }
}
