package ru.captainxsander;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.Gdx;

class AboutGameScreen extends AbstractDetailMenuScreen {
    private static final String TITLE = "Об игре";
    private final Texture backTexture = new Texture(Gdx.files.internal("menu_back.png"));
    private static final String BODY =
        "Цель: аккуратно захватывайте игрушки клешнёй и переносите их в лоток.\n"
        + "\nРежимы:\n"
        + "\nОбычная Игра — классический автомат с настройкой пула игрушек и шансов.\n"
        + "\nСпасти Зверей — 5 уровней со стоимостью игры в одну монетку, на каждом уровне необходимо поймать 5 уникальных зверей. Каждое новое животное добавляется в Зверинец.\n"
        + "\nНайти Зверей — поймайте игрушку по соответствующему ей интересному факту. Клешней нужно управлять самостоятельно, можно отпускать схваченные игрушки, разгребая кучу. За каждую правильную игрушку дается 1 монета, которую можно использовать в режиме Спасти Зверей.\n"
        + "\nПоймать кота — поймайте кота по описанию, но котята не будут стоять на месте, а будут уворачиваться от клешни. \n"
        + "\nАвторские права: © captainXsander RabbitClaw\n"
        + "\nПосвящается моей жене Алёночке";

    AboutGameScreen(MainGame game) {
        super(game);
    }

    @Override
    protected void drawContent() {
        actionBounds.clear();

        drawMenuTitle(TITLE);

        Rectangle textBounds = new Rectangle(1.55f, 2.1f, 12.9f, 4.55f);
        drawParagraph(BODY, textBounds);

        Rectangle backBounds = new Rectangle(2.05f, 0.82f, 2.9f, 0.86f);
        actionBounds.add(backBounds);
        batch.draw(panelTexture, backBounds.x, backBounds.y, backBounds.width, backBounds.height);
        if (selectedIndex == 0) {
            batch.draw(highlightTexture, backBounds.x + 0.04f, backBounds.y + 0.04f,
                backBounds.width - 0.08f, backBounds.height - 0.08f);
        }
        batch.draw(backTexture, backBounds.x + 0.14f, backBounds.y + 0.18f,
            backBounds.width - 0.28f, backBounds.height - 0.36f);
    }

    @Override
    protected int getActionCount() {
        return 1;
    }

    @Override
    protected void onActionTriggered(int actionIndex) {
        game.showPreviousMenu();
    }

    @Override
    public void dispose() {
        backTexture.dispose();
        super.dispose();
    }
}
