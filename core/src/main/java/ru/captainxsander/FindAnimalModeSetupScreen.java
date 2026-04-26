package ru.captainxsander;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

class FindAnimalModeSetupScreen extends AbstractDetailMenuScreen {
    private final MenagerieProgress progress = new MenagerieProgress();
    private final Texture moneyTexture = new Texture(Gdx.files.internal("money.png"));
    private final Rectangle playBounds = new Rectangle(4.3f, 2.0f, 7.4f, 0.9f);
    private final Rectangle backBounds = new Rectangle(4.3f, 0.9f, 7.4f, 0.9f);

    FindAnimalModeSetupScreen(MainGame game) {
        super(game);
        actionBounds.add(playBounds);
        actionBounds.add(backBounds);
    }

    @Override
    protected void drawContent() {
        drawMenuTitle("Режим: Найти зверей");
        drawParagraph(
            "По факту в верхней части экрана найдите нужного зверя в куче, захватите и доставьте в лоток."
                + " После результата режим автоматически вернёт вас в меню.",
            new Rectangle(2.5f, 5.1f, 11.0f, 1.0f)
        );
        int coins = progress.getCoinBalance();
        int maxCoins = progress.getCurrentRescueDailyCoinLimit();
        String coinText = "Монеты: " + coins + "/" + maxCoins;
        bodyFont.getData().setScale(0.0108f);
        glyphLayout.setText(bodyFont, coinText);
        float iconSize = 0.34f;
        float gap = 0.14f;
        float groupWidth = glyphLayout.width + gap + iconSize;
        float textX = (UI_WIDTH - groupWidth) * 0.5f;
        float textY = 4.55f;
        bodyFont.draw(batch, glyphLayout, textX, textY);
        batch.draw(moneyTexture, textX + glyphLayout.width + gap, 4.24f, iconSize, iconSize);

        drawButton(playBounds, "Играть", selectedIndex == 0);
        drawButton(backBounds, "Назад", selectedIndex == 1);
    }

    @Override
    protected int getActionCount() {
        return 2;
    }

    @Override
    protected void onActionTriggered(int actionIndex) {
        if (actionIndex == 0) {
            game.startFindAnimalGame();
            return;
        }
        game.showPreviousMenu();
    }

    @Override
    public void dispose() {
        super.dispose();
        moneyTexture.dispose();
    }
}
