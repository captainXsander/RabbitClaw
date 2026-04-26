package ru.captainxsander;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Хранилище прогресса игрока:
 * - открытые карточки зверинца,
 * - прогресс уровней режима "Спасти зверей",
 * - доступные игрушки для обычного режима и режима "Найти зверей".
 */
public class MenagerieProgress {
    // Имя файла локального хранилища libGDX.
    private static final String PREFERENCES_NAME = "rabbit-claw-menagerie";

    private static final String UNLOCKED_PREFIX = "unlocked.";
    private static final String RESCUED_PREFIX = "rescued.";
    private static final String NORMAL_UNLOCKED_PREFIX = "normalUnlocked.";

    private static final String RESCUE_ORDER_KEY = "rescue.order";
    private static final String RESCUE_ORDER_INITIALIZED_KEY = "rescue.order.initialized";
    private static final String COIN_BALANCE_KEY = "economy.coins.balance";
    private static final String COIN_LAST_REFILL_DAY_KEY = "economy.coins.lastRefillDay";

    // Настройки уровней режима спасения.
    private static final int RESCUE_LEVEL_SIZE = 5;
    private static final int RESCUE_NEW_ANIMAL_PERCENT = 20;
    private static final int[] RESCUE_LEVEL_DAILY_COINS = {50, 40, 30, 20, 10};

    private final Preferences preferences;

    public MenagerieProgress() {
        preferences = Gdx.app.getPreferences(PREFERENCES_NAME);
        ensureRescueOrderInitialized();
    }

    /**
     * Проверяет, открыта ли карточка игрушки в зверинце.
     */
    public boolean isUnlocked(ToyType toyType) {
        return preferences.getBoolean(UNLOCKED_PREFIX + toyType.name(), false);
    }

    /**
     * Открывает карточку игрушки в зверинце.
     */
    public boolean unlock(ToyType toyType) {
        if (isUnlocked(toyType)) {
            return false;
        }

        preferences.putBoolean(UNLOCKED_PREFIX + toyType.name(), true);
        preferences.flush();
        return true;
    }

    /**
     * Отмечает, что игрушка впервые спасена в режиме RESCUE.
     */
    public boolean markRescued(ToyType toyType) {
        String key = RESCUED_PREFIX + toyType.name();
        if (preferences.getBoolean(key, false)) {
            return false;
        }

        preferences.putBoolean(key, true);
        preferences.flush();
        return true;
    }

    /**
     * Возвращает текущий набор зверей для уровня режима спасения.
     */
    public ToyType[] getCurrentRescueLevelAnimals() {
        ToyType[] order = getRescueOrder();
        int levelIndex = getCurrentRescueLevelIndex();
        int start = levelIndex * RESCUE_LEVEL_SIZE;
        if (start >= order.length) {
            return new ToyType[0];
        }

        int end = Math.min(order.length, start + RESCUE_LEVEL_SIZE);
        return Arrays.copyOfRange(order, start, end);
    }

    /**
     * Возвращает уже пройденные наборы зверей (все уровни до текущего).
     */
    public ToyType[] getCompletedRescueAnimals() {
        ToyType[] order = getRescueOrder();
        int completedCount = Math.min(order.length, getCurrentRescueLevelIndex() * RESCUE_LEVEL_SIZE);
        return Arrays.copyOfRange(order, 0, completedCount);
    }

    /**
     * Возвращает true, если на текущем уровне уже спасены все уникальные звери.
     */
    public boolean isCurrentRescueLevelCompleted() {
        ToyType[] levelAnimals = getCurrentRescueLevelAnimals();
        if (levelAnimals.length == 0) {
            return false;
        }

        for (ToyType toyType : levelAnimals) {
            if (!preferences.getBoolean(RESCUED_PREFIX + toyType.name(), false)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Продвигает прогресс: звери текущего уровня добавляются в NORMAL/FIND,
     * затем открывается следующий уровень RESCUE.
     */
    public boolean completeCurrentRescueLevelIfNeeded() {
        if (!isCurrentRescueLevelCompleted()) {
            return false;
        }

        ToyType[] levelAnimals = getCurrentRescueLevelAnimals();
        for (ToyType toyType : levelAnimals) {
            preferences.putBoolean(NORMAL_UNLOCKED_PREFIX + toyType.name(), true);
        }

        int nextLevel = Math.min(getCurrentRescueLevelIndex() + 1, getRescueLevelCount());
        preferences.putInteger(getCurrentRescueLevelKey(), nextLevel);
        preferences.flush();
        return true;
    }

    /**
     * Возвращает пул для обычной игры: дефолт + открытые через RESCUE звери.
     */
    public ToyType[] getNormalModePool() {
        List<ToyType> pool = new ArrayList<>();
        pool.addAll(Arrays.asList(ToyType.DEFAULT_POOL));

        for (ToyType animal : ToyType.ANIMAL_POOL) {
            if (preferences.getBoolean(NORMAL_UNLOCKED_PREFIX + animal.name(), false)) {
                pool.add(animal);
            }
        }

        return pool.toArray(new ToyType[0]);
    }

    /**
     * Возвращает пул для режима "Найти зверей".
     */
    public ToyType[] getFindAnimalPool() {
        List<ToyType> pool = new ArrayList<>();
        for (ToyType animal : ToyType.ANIMAL_POOL) {
            if (preferences.getBoolean(NORMAL_UNLOCKED_PREFIX + animal.name(), false)) {
                pool.add(animal);
            }
        }
        return pool.toArray(new ToyType[0]);
    }

    /**
     * Режим "Найти зверей" открывается после полного прохождения 1-го уровня RESCUE.
     */
    public boolean isFindAnimalModeUnlocked() {
        return getCurrentRescueLevelIndex() >= 1;
    }

    /**
     * Индекс текущего уровня режима спасения (0..N).
     */
    public int getCurrentRescueLevelIndex() {
        return preferences.getInteger(getCurrentRescueLevelKey(), 0);
    }

    public int getRescueLevelCount() {
        return (int) Math.ceil(ToyType.ANIMAL_POOL.length / (float) RESCUE_LEVEL_SIZE);
    }

    public boolean isRescueFullyCompleted() {
        return getCurrentRescueLevelIndex() >= getRescueLevelCount();
    }

    public int getCurrentRescueLevelNumber() {
        return Math.min(getCurrentRescueLevelIndex() + 1, getRescueLevelCount());
    }

    public int getCurrentRescueDailyCoinLimit() {
        int levelNumber = getCurrentRescueLevelNumber();
        int levelIndex = Math.max(0, Math.min(levelNumber - 1, RESCUE_LEVEL_DAILY_COINS.length - 1));
        return RESCUE_LEVEL_DAILY_COINS[levelIndex];
    }

    public int getCoinBalance() {
        refreshCoinsForCurrentDay();
        return preferences.getInteger(COIN_BALANCE_KEY, getCurrentRescueDailyCoinLimit());
    }

    public boolean canSpendCoinForRescueAttempt() {
        if (isRescueFullyCompleted()) {
            return true;
        }
        return getCoinBalance() > 0;
    }

    public boolean spendCoinForRescueAttempt() {
        if (isRescueFullyCompleted()) {
            return true;
        }

        int current = getCoinBalance();
        if (current <= 0) {
            return false;
        }

        preferences.putInteger(COIN_BALANCE_KEY, current - 1);
        preferences.flush();
        return true;
    }

    public boolean awardCoinFromFindAnimalWin() {
        refreshCoinsForCurrentDay();

        int current = preferences.getInteger(COIN_BALANCE_KEY, getCurrentRescueDailyCoinLimit());
        int limit = getCurrentRescueDailyCoinLimit();
        if (current >= limit) {
            return false;
        }

        preferences.putInteger(COIN_BALANCE_KEY, current + 1);
        preferences.flush();
        return true;
    }

    public String getTimeUntilNextRefillRu() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRefill = getNextRefillDateTime(now);
        Duration duration = Duration.between(now, nextRefill);

        long totalMinutes = duration.toMinutes();
        if (duration.getSeconds() % 60 != 0) {
            totalMinutes += 1;
        }

        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        return hours + " ч " + minutes + " мин";
    }

    /**
     * Доля новых зверей на уровнях 2+ в RESCUE.
     */
    public int getRescueNewAnimalPercent() {
        return RESCUE_NEW_ANIMAL_PERCENT;
    }

    /**
     * Полный сброс прогресса игрока.
     */
    public void resetAllProgress() {
        preferences.clear();
        preferences.flush();
        ensureRescueOrderInitialized();
        refreshCoinsForCurrentDay();
    }

    private String getCurrentRescueLevelKey() {
        return "rescue.currentLevel";
    }

    private void ensureRescueOrderInitialized() {
        if (preferences.getBoolean(RESCUE_ORDER_INITIALIZED_KEY, false)) {
            return;
        }

        // Формируем случайный, но фиксируемый порядок уникальных зверей режима RESCUE.
        List<ToyType> animals = new ArrayList<>(Arrays.asList(ToyType.ANIMAL_POOL));
        Collections.shuffle(animals, new Random());

        StringBuilder orderValue = new StringBuilder();
        for (int i = 0; i < animals.size(); i++) {
            if (i > 0) {
                orderValue.append(',');
            }
            orderValue.append(animals.get(i).name());
        }

        preferences.putString(RESCUE_ORDER_KEY, orderValue.toString());
        preferences.putBoolean(RESCUE_ORDER_INITIALIZED_KEY, true);
        preferences.putInteger(getCurrentRescueLevelKey(), 0);
        preferences.flush();
    }

    private ToyType[] getRescueOrder() {
        String serialized = preferences.getString(RESCUE_ORDER_KEY, "");
        if (serialized.isEmpty()) {
            ensureRescueOrderInitialized();
            serialized = preferences.getString(RESCUE_ORDER_KEY, "");
        }

        String[] names = serialized.split(",");
        List<ToyType> order = new ArrayList<>();
        for (String name : names) {
            if (name == null || name.isBlank()) {
                continue;
            }
            try {
                order.add(ToyType.valueOf(name));
            } catch (IllegalArgumentException ignored) {
                // Если набор типов поменялся между версиями, пропускаем неизвестные значения.
            }
        }

        // Защитный fallback на случай повреждённых данных сохранения.
        if (order.isEmpty()) {
            order.addAll(Arrays.asList(ToyType.ANIMAL_POOL));
        }

        return order.toArray(new ToyType[0]);
    }

    private void refreshCoinsForCurrentDay() {
        String currentCycleDay = getRefillCycleDay();
        String savedCycleDay = preferences.getString(COIN_LAST_REFILL_DAY_KEY, "");
        int limit = getCurrentRescueDailyCoinLimit();
        int currentBalance = preferences.getInteger(COIN_BALANCE_KEY, limit);

        boolean dayChanged = !currentCycleDay.equals(savedCycleDay);
        if (dayChanged) {
            currentBalance = limit;
        } else if (currentBalance > limit) {
            currentBalance = limit;
        }

        if (dayChanged || currentBalance != preferences.getInteger(COIN_BALANCE_KEY, limit)) {
            preferences.putString(COIN_LAST_REFILL_DAY_KEY, currentCycleDay);
            preferences.putInteger(COIN_BALANCE_KEY, currentBalance);
            preferences.flush();
        }
    }

    private String getRefillCycleDay() {
        LocalDateTime now = LocalDateTime.now();
        LocalTime refillTime = LocalTime.of(
            GameTuning.DAILY_COIN_REFILL_HOUR,
            GameTuning.DAILY_COIN_REFILL_MINUTE
        );
        LocalDate cycleDate = now.toLocalDate();
        if (now.toLocalTime().isBefore(refillTime)) {
            cycleDate = cycleDate.minusDays(1);
        }
        return cycleDate.toString();
    }

    private LocalDateTime getNextRefillDateTime(LocalDateTime now) {
        LocalTime refillTime = LocalTime.of(
            GameTuning.DAILY_COIN_REFILL_HOUR,
            GameTuning.DAILY_COIN_REFILL_MINUTE
        );
        LocalDateTime todayRefill = now.toLocalDate().atTime(refillTime);
        return now.isBefore(todayRefill) ? todayRefill : todayRefill.plusDays(1);
    }
}
