package ru.captainxsander;

import com.badlogic.gdx.math.Rectangle;

class AboutGameScreen extends AbstractDetailMenuScreen {
    private static final String TITLE = "Об игре";
    private static final String BODY = "Правила Игры:\n"
        + "• Цель: аккуратно захватывайте игрушки клешнёй и переносите их в лоток.\n"
        + "• Управление: стрелки/WASD — выбор и движение по меню, Enter/Space — подтвердить, Esc/Back — назад.\n"
        + "• Во время игры следите за таймингом: чем точнее движение, тем выше шанс успешного захвата.\n"
        + "\nРежимы:\n"
        + "• Обычный — классический автомат с настройкой пула игрушек и шансов.\n"
        + "• Спасение — попытка за монетку с прогрессом зверинца.\n"
        + "• Найди животное — ищите нужную игрушку по заданию.\n"
        + "• Поймай кота — отдельный динамичный режим повышенной сложности.\n"
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
