package ru.captainxsander;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

class RescueModeSetupScreen extends AbstractDetailMenuScreen {
    private final MenagerieProgress progress = new MenagerieProgress();
    private final Texture moneyTexture = new Texture(Gdx.files.internal("money.png"));

    private final Rectangle resetBounds = new Rectangle(4.3f, 2.9f, 7.4f, 0.9f);
    private final Rectangle playBounds = new Rectangle(4.3f, 1.85f, 7.4f, 0.9f);
    private final Rectangle backBounds = new Rectangle(4.3f, 0.8f, 7.4f, 0.9f);
    private final Rectangle confirmYesBounds = new Rectangle(4.3f, 2.35f, 3.55f, 0.9f);
    private final Rectangle confirmNoBounds = new Rectangle(8.15f, 2.35f, 3.55f, 0.9f);

    private boolean resetConfirmationVisible;
    private String actionMessage;
    private String nextRefillInfo;

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
    public void show() {
        super.show();
        nextRefillInfo = "До пополнения: " + progress.getTimeUntilNextRefillRu();
    }

    @Override
    protected void drawContent() {
        drawMenuTitle("Режим: Спасти зверей");
        drawParagraph(
            "Соберите уникальных зверей текущего уровня, чтобы открыть их в зверинце и перейти к следующему уровню."
                + " На уровне появляются звери только из активного набора.",
            new Rectangle(2.5f, 5.45f, 11.0f, 1.05f)
        );

        String levelInfo = "Текущий уровень: " + progress.getCurrentRescueLevelNumber()
            + " из " + progress.getRescueLevelCount();
        drawCenteredText(bodyFont, levelInfo, new Rectangle(1.3f, 4.95f, 13.4f, 0.5f), 0.011f, 1);
        drawMoneyInfo(4.45f);
        if (nextRefillInfo != null) {
            drawCenteredText(bodyFont, nextRefillInfo, new Rectangle(2.8f, 4.08f, 10.4f, 0.42f), 0.0102f, 1);
        }

        if (progress.isRescueFullyCompleted()) {
            drawCenteredText(
                bodyFont,
                "Режим успешно пройден. Новые попытки бесплатны!",
                new Rectangle(2.0f, 3.6f, 12.0f, 0.45f),
                0.0102f,
                1
            );
        }

        if (actionMessage != null && !actionMessage.isBlank()) {
            drawCenteredText(bodyFont, actionMessage, new Rectangle(1.8f, 0.3f, 12.4f, 0.5f), 0.0098f, 1);
        }

        if (!resetConfirmationVisible) {
            drawButton(resetBounds, "Сбросить прогресс", selectedIndex == 0);
            drawButton(playBounds, "Играть", selectedIndex == 1);
            drawButton(backBounds, "Назад", selectedIndex == 2);
            return;
        }

        drawParagraph(
            "Подтвердите сброс прогресса. Это действие нельзя отменить.",
            new Rectangle(3.2f, 2.95f, 9.6f, 0.7f)
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
                actionMessage = "Прогресс и монеты сброшены.";
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
            actionMessage = null;
            return;
        }
        if (actionIndex == 1) {
            boolean started = game.startRescueGame();
            if (!started) {
                actionMessage = "Монеты закончились. Зайдите позже после ежедневного пополнения.";
            }
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

    @Override
    public void dispose() {
        super.dispose();
        moneyTexture.dispose();
    }

    private void drawMoneyInfo(float y) {
        int coins = progress.getCoinBalance();
        int dailyLimit = progress.getCurrentRescueDailyCoinLimit();
        String coinText = "Монеты: " + coins + "/" + dailyLimit;
        bodyFont.getData().setScale(0.0108f);
        glyphLayout.setText(bodyFont, coinText);
        float iconSize = 0.34f;
        float gap = 0.14f;
        float groupWidth = glyphLayout.width + gap + iconSize;
        float textX = (UI_WIDTH - groupWidth) * 0.5f;
        float textY = y + 0.29f;
        bodyFont.draw(batch, glyphLayout, textX, textY);
        batch.draw(moneyTexture, textX + glyphLayout.width + gap, y, iconSize, iconSize);
    }
}
