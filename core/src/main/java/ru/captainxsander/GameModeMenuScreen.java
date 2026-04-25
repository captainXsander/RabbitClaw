package ru.captainxsander;

class GameModeMenuScreen extends AbstractMenuScreen {
    GameModeMenuScreen(MainGame game) {
        super(game, "menu_mode_title.png");
        MenagerieProgress progress = new MenagerieProgress();

        addOption("menu_mode_normal.png", game::showNormalModeSetupMenu);
        addOption("menu_mode_rescue.png", game::showRescueModeSetupMenu);

        if (progress.isFindAnimalModeUnlocked()) {
            addOption("find_animals.png", game::showFindAnimalModeSetupMenu);
        }
        addOption("catch_cat.png", game::showCatchCatModeSetupMenu);

        addOption("menu_back.png", game::showPreviousMenu);
    }

    @Override
    protected boolean onBackRequested() {
        game.showPreviousMenu();
        return true;
    }
}
