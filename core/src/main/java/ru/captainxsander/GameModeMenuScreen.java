package ru.captainxsander;

class GameModeMenuScreen extends AbstractMenuScreen {
    GameModeMenuScreen(MainGame game) {
        // Заголовок и варианты запуска из подменю "Играть".
        super(game, "menu_mode_title.png");
        // Запускаем существующий игровой экран.
        addOption("menu_mode_normal.png", game::startNormalGame);
        // Здесь открываем обычную игру, но в режиме спасения зверей.
        addOption("menu_mode_rescue.png", game::startRescueGame);
        // Новый режим: поиск конкретной игрушки с ручным доворотом клешни.
        // Используем существующий ассет кнопки, который точно есть в assets.
        addOption("menu_menagerie.png", game::startFindAnimalGame);
        // Явная кнопка возврата в предыдущее меню.
        addOption("menu_back.png", game::showPreviousMenu);
    }

    @Override
    protected boolean onBackRequested() {
        // Back/Escape возвращает именно в предыдущее меню.
        game.showPreviousMenu();
        return true;
    }
}
