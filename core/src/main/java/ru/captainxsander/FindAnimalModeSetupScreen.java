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
        int level = progress.getCurrentRescueLevelNumber();
        int coins = progress.getCoinBalance();
        int maxCoins = progress.getCurrentRescueDailyCoinLimit();
        drawCenteredText(bodyFont, "Текущий уровень: " + level, new Rectangle(1.5f, 4.5f, 13f, 0.45f), 0.0108f, 1);
        batch.draw(moneyTexture, 5.2f, 4.06f, 0.36f, 0.36f);
        drawCenteredText(bodyFont, "Монеты: " + coins + "/" + maxCoins, new Rectangle(5.6f, 4.0f, 5.2f, 0.45f), 0.0108f, 0);

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
