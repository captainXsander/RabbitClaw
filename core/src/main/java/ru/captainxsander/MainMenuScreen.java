package ru.captainxsander;

import com.badlogic.gdx.Gdx;

class MainMenuScreen extends AbstractMenuScreen {
    MainMenuScreen(MainGame game) {
        // Заголовок главного меню и его основные разделы.
        super(game, "menu_main_title.png");
        // Переход в выбор режима игры.
        addOption("menu_play.png", game::showGameModeMenu);
        // Переход к настройкам.
        addOption("menu_settings.png", game::showSettings);
        // Переход в зверинец.
        addOption("menu_menagerie.png", game::showMenagerie);
        // Полное завершение приложения.
        addOption("menu_exit.png", Gdx.app::exit);
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
        return 0.016f;
    }
}
