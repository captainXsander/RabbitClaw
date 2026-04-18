package ru.captainxsander;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;

import java.util.ArrayDeque;
import java.util.Deque;

public class MainGame extends Game {
    private final Deque<MenuId> menuHistory = new ArrayDeque<>();
    private MenuId currentMenuId;

    @Override
    public void create() {
        // При запуске игры сразу показываем главное меню.
        showMainMenu();
    }

    public void showMainMenu() {
        // Главное меню приложения.
        showMenu(MenuId.MAIN);
    }

    public void showGameModeMenu() {
        // Подменю выбора режима игры.
        showMenu(MenuId.GAME_MODE);
    }

    public void showSettings() {
        // Временный экран настроек.
        showMenu(MenuId.SETTINGS);
    }

    public void showMenagerie() {
        // Полноценный экран зверинца с карточками игрушек.
        showMenu(MenuId.MENAGERIE);
    }

    public void showPreviousMenu() {
        // Возвращаемся в то меню, из которого пользователь пришел.
        if (menuHistory.isEmpty()) {
            showMainMenu();
            return;
        }

        MenuId previousMenuId = menuHistory.pop();
        currentMenuId = previousMenuId;
        switchScreen(createMenuScreen(previousMenuId));
    }

    public void startRescueGame() {
        // Запускаем режим спасения зверей с открытием карточек.
        clearMenuNavigation();
        switchScreen(new GameScreen(GameMode.RESCUE));
    }

    public void startNormalGame() {
        // Запускаем текущую обычную игру.
        clearMenuNavigation();
        switchScreen(new GameScreen(GameMode.NORMAL));
    }

    private void showMenu(MenuId menuId) {
        if (currentMenuId != null && currentMenuId != menuId) {
            menuHistory.push(currentMenuId);
        }

        currentMenuId = menuId;
        switchScreen(createMenuScreen(menuId));
    }

    private Screen createMenuScreen(MenuId menuId) {
        switch (menuId) {
            case MAIN:
                return new MainMenuScreen(this);
            case GAME_MODE:
                return new GameModeMenuScreen(this);
            case SETTINGS:
                return new PlaceholderScreen(this, "menu_settings_title.png");
            case MENAGERIE:
                return new MenagerieScreen(this);
            default:
                throw new IllegalStateException("Unsupported menu: " + menuId);
        }
    }

    private void clearMenuNavigation() {
        currentMenuId = null;
        menuHistory.clear();
    }

    private void switchScreen(Screen newScreen) {
        // Сохраняем старый экран, чтобы освободить его ресурсы после переключения.
        Screen previousScreen = getScreen();
        // Передаем управление новому экрану.
        setScreen(newScreen);
        if (previousScreen != null) {
            // Освобождаем текстуры, батчи и прочие ресурсы предыдущего экрана.
            previousScreen.dispose();
        }
    }

    private enum MenuId {
        MAIN,
        GAME_MODE,
        SETTINGS,
        MENAGERIE
    }
}
