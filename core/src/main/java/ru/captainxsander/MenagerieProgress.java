package ru.captainxsander;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/**
 * Хранилище прогресса зверинца.
 * Отвечает только за сохранение и чтение факта,
 * открыта карточка конкретной игрушки или нет.
 */
public class MenagerieProgress {
    // Имя файла настроек, в котором хранится прогресс.
    private static final String PREFERENCES_NAME = "rabbit-claw-menagerie";

    // Общий префикс для ключей вида unlocked.BEAR, unlocked.RABBIT_BIG и так далее.
    private static final String UNLOCKED_PREFIX = "unlocked.";

    // Объект Preferences даёт доступ к сохранённым данным libGDX.
    private final Preferences preferences;

    public MenagerieProgress() {
        // Загружаем или создаём локальное хранилище прогресса.
        preferences = Gdx.app.getPreferences(PREFERENCES_NAME);
    }

    /**
     * Проверяет, открыта ли карточка игрушки.
     */
    public boolean isUnlocked(ToyType toyType) {
        // Для каждой игрушки читаем отдельный флаг из Preferences.
        return preferences.getBoolean(UNLOCKED_PREFIX + toyType.name(), false);
    }

    /**
     * Открывает карточку игрушки.
     * Возвращает true, если это было первое открытие.
     */
    public boolean unlock(ToyType toyType) {
        // Если карточка уже была открыта раньше, ничего не меняем.
        if (isUnlocked(toyType)) {
            return false;
        }

        // Сохраняем новое состояние карточки.
        preferences.putBoolean(UNLOCKED_PREFIX + toyType.name(), true);

        // Сразу записываем изменения на диск.
        preferences.flush();
        return true;
    }
}
