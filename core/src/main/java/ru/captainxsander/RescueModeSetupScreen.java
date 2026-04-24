package ru.captainxsander;

import com.badlogic.gdx.math.Rectangle;

class RescueModeSetupScreen extends AbstractDetailMenuScreen {
    private final MenagerieProgress progress = new MenagerieProgress();

    private final Rectangle resetBounds = new Rectangle(4.3f, 2.9f, 7.4f, 0.9f);
    private final Rectangle playBounds = new Rectangle(4.3f, 1.85f, 7.4f, 0.9f);
    private final Rectangle backBounds = new Rectangle(4.3f, 0.8f, 7.4f, 0.9f);
    private final Rectangle confirmYesBounds = new Rectangle(4.3f, 1.85f, 3.55f, 0.9f);
    private final Rectangle confirmNoBounds = new Rectangle(8.15f, 1.85f, 3.55f, 0.9f);

    private boolean resetConfirmationVisible;

    RescueModeSetupScreen(MainGame game) {
        super(game);
        actionBounds.add(resetBounds);
        actionBounds.add(playBounds);
        actionBounds.add(backBounds);
        actionBounds.add(confirmYesBounds);
        actionBounds.add(confirmNoBounds);
        selectedIndex = 1;
    }

    @Override
    protected void drawContent() {
        drawMenuTitle("Режим: Спасти зверей");
        drawParagraph(
            "Соберите уникальных зверей текущего уровня, чтобы открыть их в зверинце и перейти к следующему уровню."
                + " На уровне появляются звери только из активного набора.",
            new Rectangle(2.5f, 5.45f, 11.0f, 1.05f)
        );

        String levelInfo = "Текущий уровень: " + (progress.getCurrentRescueLevelIndex() + 1)
            + " из " + progress.getRescueLevelCount();
        drawCenteredText(bodyFont, levelInfo, new Rectangle(1.3f, 4.8f, 13.4f, 0.5f), 0.011f, 1);

        if (!resetConfirmationVisible) {
            drawButton(resetBounds, "Сбросить прогресс", selectedIndex == 0);
            drawButton(playBounds, "Играть", selectedIndex == 1);
            drawButton(backBounds, "Назад", selectedIndex == 2);
            return;
        }

        drawParagraph(
            "Подтвердите сброс прогресса. Это действие нельзя отменить.",
            new Rectangle(3.2f, 2.95f, 9.6f, 0.8f)
        );
        drawButton(confirmYesBounds, "Да, сбросить", selectedIndex == 3);
        drawButton(confirmNoBounds, "Отмена", selectedIndex == 4);
    }

    @Override
    protected int getActionCount() {
        return 5;
    }

    @Override
    protected void onActionTriggered(int actionIndex) {
        if (resetConfirmationVisible) {
            if (actionIndex == 3) {
                progress.resetAllProgress();
            }
            if (actionIndex == 3 || actionIndex == 4) {
                resetConfirmationVisible = false;
                selectedIndex = 1;
            }
            return;
        }

        if (actionIndex == 0) {
            resetConfirmationVisible = true;
            selectedIndex = 4;
            return;
        }
        if (actionIndex == 1) {
            game.startRescueGame();
            return;
        }
        if (actionIndex == 2) {
            game.showPreviousMenu();
        }
    }

    @Override
    protected boolean onBackRequested() {
        if (resetConfirmationVisible) {
            resetConfirmationVisible = false;
            selectedIndex = 1;
            return true;
        }
        return super.onBackRequested();
    }
}

