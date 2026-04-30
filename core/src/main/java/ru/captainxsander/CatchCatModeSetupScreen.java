package ru.captainxsander;

import com.badlogic.gdx.math.Rectangle;

class CatchCatModeSetupScreen extends AbstractDetailMenuScreen {
    private final Rectangle playBounds = new Rectangle(4.3f, 2.0f, 7.4f, 0.9f);
    private final Rectangle backBounds = new Rectangle(4.3f, 0.9f, 7.4f, 0.9f);

    CatchCatModeSetupScreen(MainGame game) {
        super(game);
        actionBounds.add(playBounds);
        actionBounds.add(backBounds);
    }

    @Override
    protected void drawContent() {
        drawMenuTitle("Режим: Поймать кота");
        drawParagraph(
            "Поймайте кота с нужной эмоцией из движущейся кучи и доставьте его в лоток."
                + " Коты резвятся по полю, поэтому действовать нужно быстро и точно.",
            new Rectangle(2.5f, 5.1f, 11.0f, 1.0f)
        );

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
            game.startCatchCatGame();
            return;
        }
        game.showPreviousMenu();
    }
}
