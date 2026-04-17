package ru.captainxsander;

class PlaceholderScreen extends AbstractMenuScreen {
    PlaceholderScreen(MainGame game, String titleTexturePath) {
        // Этот экран временно показывает раздел, который ещё не реализован.
        super(game, titleTexturePath);
        // Сообщаем пользователю, что раздел пока в разработке.
        addOption("menu_in_development.png", game::showMainMenu);
        // Явная кнопка возврата в главное меню.
        addOption("menu_back.png", game::showMainMenu);
    }

    @Override
    protected boolean onBackRequested() {
        // Back/Escape тоже возвращает в главное меню.
        game.showMainMenu();
        return true;
    }
}
