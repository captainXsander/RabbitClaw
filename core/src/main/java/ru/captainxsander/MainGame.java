package ru.captainxsander;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;

public class MainGame extends Game {
    @Override
    public void create() {
        // При запуске игры сразу показываем главное меню.
        showMainMenu();
    }

    public void showMainMenu() {
        // Главное меню приложения.
        switchScreen(new MainMenuScreen(this));
    }

    public void showGameModeMenu() {
        // Подменю выбора режима игры.
        switchScreen(new GameModeMenuScreen(this));
    }

    public void showSettings() {
        // Временный экран настроек.
        switchScreen(new PlaceholderScreen(this, "menu_settings_title.png"));
    }

    public void showMenagerie() {
        // Полноценный экран зверинца с карточками игрушек.
        switchScreen(new MenagerieScreen(this));
    }

    public void startRescueGame() {
        // Запускаем режим спасения зверей с открытием карточек.
        switchScreen(new GameScreen(GameMode.RESCUE));
    }

    public void startNormalGame() {
        // Запускаем текущую обычную игру.
        switchScreen(new GameScreen(GameMode.NORMAL));
    }

    private void switchScreen(Screen newScreen) {
        // Сохраняем старый экран, чтобы освободить его ресурсы после переключения.
        Screen previousScreen = getScreen();
        // Передаём управление новому экрану.
        setScreen(newScreen);
        if (previousScreen != null) {
            // Освобождаем текстуры, батчи и прочие ресурсы предыдущего экрана.
            previousScreen.dispose();
        }
    }
}
