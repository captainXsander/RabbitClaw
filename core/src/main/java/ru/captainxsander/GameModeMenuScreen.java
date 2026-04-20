package ru.captainxsander;

class GameModeMenuScreen extends AbstractMenuScreen {
    GameModeMenuScreen(MainGame game) {
        // Заголовок и варианты запуска из подменю "Играть".
        super(game, "menu_mode_title.png");
        MenagerieProgress progress = new MenagerieProgress();

        // Запускаем базовый режим с дефолтами + открытыми через RESCUE зверями.
        addOption("menu_mode_normal.png", game::startNormalGame);
        // Режим поэтапного спасения зверей (уровни по 5 новых уникальных животных).
        addOption("menu_mode_rescue.png", game::startRescueGame);

        // Режим поиска зверей становится доступен только после завершения 1-го уровня RESCUE.
        if (progress.isFindAnimalModeUnlocked()) {
            addOption("find_animals.png", game::startFindAnimalGame);
        }

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
