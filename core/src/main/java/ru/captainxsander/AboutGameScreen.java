package ru.captainxsander;

import com.badlogic.gdx.math.Rectangle;

class AboutGameScreen extends AbstractDetailMenuScreen {
    private static final String TITLE = "Об игре";
    private static final String BODY = "Правила Игры:\n"
        + "Цель: аккуратно захватывайте игрушки клешнёй и переносите их в лоток.\n"
        + "\nРежимы:\n"
        + "\nОбычная Игра — классический автомат с настройкой пула игрушек и шансов.\n"
        + "\nСпасти Зверей — 5 уровней со стоимостью игры в одну монетку, на каждом уровне необходимо поймать 5 уникальных зверей. Каждое новое животное добавляется в Зверинец.\n"
        + "\nНайти Зверей — поймайте игрушку по соответствующему ей интересному факту. Клешней нужно управлять самостоятельно, можно отпускать схваченные игрушки, разгребая кучу. За каждую правильную игрушку дается 1 монета, которую можно использовать в режиме Спасти Зверей.\n"
        + "\nПоймать кота — поймайте кота по описанию, но котята не будут стоять на месте, а будут уворачиваться от клешни. \n"
        + "\nАвторские права: © captainXsander RabbitClaw\n"
        + "Посвящается моей жене Алёночке";

    AboutGameScreen(MainGame game) {
        super(game);
    }

    @Override
    protected void drawContent() {
        actionBounds.clear();

        drawMenuTitle(TITLE);

        Rectangle textBounds = new Rectangle(1.55f, 2.45f, 12.9f, 4.2f);
        drawParagraph(BODY, textBounds);

        Rectangle backBounds = new Rectangle(5.4f, 1.2f, 5.2f, 0.9f);
        actionBounds.add(backBounds);
        drawButton(backBounds, "Назад", selectedIndex == 0);
    }

    @Override
    protected int getActionCount() {
        return 1;
    }

    @Override
    protected void onActionTriggered(int actionIndex) {
        game.showPreviousMenu();
    }
}
