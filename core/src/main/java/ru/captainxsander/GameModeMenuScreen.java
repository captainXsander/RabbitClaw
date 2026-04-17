package ru.captainxsander;

class GameModeMenuScreen extends AbstractMenuScreen {
    GameModeMenuScreen(MainGame game) {
        // Заголовок и варианты запуска из подменю "Играть".
        super(game, "menu_mode_title.png");
        // Запускаем существующий игровой экран.
        addOption("menu_mode_normal.png", game::startNormalGame);
        // Здесь открываем обычную игру, но в режиме спасения зверей.
        addOption("menu_mode_rescue.png", game::startRescueGame);
    }

    @Override
    protected boolean onBackRequested() {
        // Возврат из выбора режима обратно в главное меню.
        game.showMainMenu();
        return true;
    }
}
