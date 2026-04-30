package ru.captainxsander;

class GameModeMenuScreen extends AbstractMenuScreen {
    GameModeMenuScreen(MainGame game) {
        super(game, "menu_mode_title.png");
        MenagerieProgress progress = new MenagerieProgress();

        addOption("Обычный режим", game::showNormalModeSetupMenu);
        addOption("Режим спасения", game::showRescueModeSetupMenu);

        if (progress.isFindAnimalModeUnlocked()) {
            addOption("Найди животных", game::showFindAnimalModeSetupMenu);
        }
        addOption("Поймай кота", game::showCatchCatModeSetupMenu);

        addOption("Назад", game::showPreviousMenu);
    }

    @Override
    protected boolean onBackRequested() {
        game.showPreviousMenu();
        return true;
    }
}
