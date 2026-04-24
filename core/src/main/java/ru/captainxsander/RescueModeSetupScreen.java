package ru.captainxsander;

import com.badlogic.gdx.math.Rectangle;

class RescueModeSetupScreen extends AbstractDetailMenuScreen {
    private final MenagerieProgress progress = new MenagerieProgress();

    private final Rectangle resetBounds = new Rectangle(4.3f, 2.9f, 7.4f, 0.9f);
    private final Rectangle playBounds = new Rectangle(4.3f, 1.85f, 7.4f, 0.9f);
    private final Rectangle backBounds = new Rectangle(4.3f, 0.8f, 7.4f, 0.9f);

    RescueModeSetupScreen(MainGame game) {
        super(game);
        actionBounds.add(resetBounds);
        actionBounds.add(playBounds);
        actionBounds.add(backBounds);
        selectedIndex = 1;
    }

    @Override
    protected void drawContent() {
        drawMenuTitle("Режим: Спасти зверей");
        drawParagraph(
            "Соберите уникальных зверей текущего уровня, чтобы открыть их в зверинце и перейти к следующему уровню."
                + " На уровне появляются звери только из активного набора.",
            new Rectangle(1.3f, 5.45f, 13.4f, 1.1f)
        );

        String levelInfo = "Текущий уровень: " + (progress.getCurrentRescueLevelIndex() + 1)
            + " из " + progress.getRescueLevelCount();
        drawCenteredText(bodyFont, levelInfo, new Rectangle(1.3f, 4.8f, 13.4f, 0.5f), 0.011f, 1);

        drawButton(resetBounds, "Сбросить прогресс", selectedIndex == 0);
        drawButton(playBounds, "Играть", selectedIndex == 1);
        drawButton(backBounds, "Назад", selectedIndex == 2);
    }

    @Override
    protected int getActionCount() {
        return 3;
    }

    @Override
    protected void onActionTriggered(int actionIndex) {
        if (actionIndex == 0) {
            progress.resetAllProgress();
            return;
        }
        if (actionIndex == 1) {
            game.startRescueGame();
            return;
        }
        game.showPreviousMenu();
    }
}
