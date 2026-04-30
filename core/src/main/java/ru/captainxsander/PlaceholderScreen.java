package ru.captainxsander;

class PlaceholderScreen extends AbstractMenuScreen {
    PlaceholderScreen(MainGame game, String titleTexturePath) {
        // Этот экран временно показывает раздел, который еще не реализован.
        super(game, titleTexturePath);
        // Сообщаем пользователю, что раздел пока в разработке.
        addOption("В разработке", game::showPreviousMenu);
        // Явная кнопка возврата в предыдущее меню.
        addOption("Назад", game::showPreviousMenu);
    }

    @Override
    protected boolean onBackRequested() {
        // Back/Escape тоже возвращает в предыдущее меню.
        game.showPreviousMenu();
        return true;
    }
}
