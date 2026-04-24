package ru.captainxsander;

import com.badlogic.gdx.math.Rectangle;

class FindAnimalModeSetupScreen extends AbstractDetailMenuScreen {
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
            new Rectangle(1.3f, 5.1f, 13.4f, 1.3f)
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
            game.startFindAnimalGame();
            return;
        }
        game.showPreviousMenu();
    }
}
