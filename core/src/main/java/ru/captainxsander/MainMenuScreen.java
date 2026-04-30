package ru.captainxsander;

import com.badlogic.gdx.Gdx;

class MainMenuScreen extends AbstractMenuScreen {
    MainMenuScreen(MainGame game) {
        // Заголовок главного меню и его основные разделы.
        super(game, "menu_main_title.png");
        // Переход в выбор режима игры.
        addOption("Играть", game::showGameModeMenu);
        // Переход к настройкам.
        addOption("Настройки", game::showSettings);
        // Переход в зверинец.
        addOption("Зверинец", game::showMenagerie);
        // Полное завершение приложения.
        addOption("Выход", Gdx.app::exit);
    }

    @Override
    protected boolean onBackRequested() {
        // На главном меню Back/Escape означает выход из игры.
        Gdx.app.exit();
        return true;
    }

    @Override
    protected String getBrandTitleText() {
        return "RabbitClaw";
    }

    @Override
    protected float getBrandTitleScale() {
        return 0.020f;
    }
}
