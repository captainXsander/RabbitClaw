package ru.captainxsander;

/**

 * Все важные настройки автомата в одном месте.
 *
 * 🔥 ВАЖНО:
 * Меняй значения здесь, а не в логике классов.
 * Это позволяет балансить игру без переписывания кода.
 */
public final class GameTuning {

    private GameTuning() {}

    // =========================
    // Мир и камера
    // =========================
    public static final float WORLD_WIDTH = 16f;
    public static final float WORLD_HEIGHT = 9f;

    // =========================
    // Клешня
    // =========================
    public static final float CLAW_HOME_X = 8.0f;
    public static final float CLAW_HOME_Y = 7.7f;
    public static final float CLAW_DOWN_LIMIT_Y = 2.2f;

    public static final float CLAW_MOVE_SPEED_X = 3.25f;
    public static final float CLAW_MOVE_SPEED_Y = 3.45f;

    public static final float CLAW_HEAD_W = 0.95f;
    public static final float CLAW_HEAD_H = 0.28f;

    public static final float CLAW_FINGER_W = 0.16f;
    public static final float CLAW_FINGER_H = 0.95f;

    public static final float CLAW_FINGER_GAP_OPEN = 0.92f;
    public static final float CLAW_FINGER_GAP_CLOSED = 0.56f;

    public static final float CLAW_CLOSE_TIME = 0.24f;
    public static final float CLAW_OPEN_TIME = 0.24f;

    // Защита от раннего срабатывания клешни
    public static final float CLAW_MIN_DROP_BEFORE_CHECK = 0.3f;
    // Точность захвата клешни
    public static final float CLAW_GRAB_X_MARGIN = 0.08f;

    // Сила продавливаемости клешни, на сколько может продавить
    public static final float CLAW_MAX_PRESS_DEPTH = 0.35f;

    // Выбираем игрушку ближе к центру клешни, чем сложнее, чем хуже ловиться
    public static final float CLAW_SCORE_WEIGHT_X = 0.6f;
    public static final float CLAW_SCORE_WEIGHT_Y = 1.2f;
    // чем сложнее, тем хуже ловиться
    public static final float CLAW_SCORE_WEIGHT_DIFFICULTY = 0.3f;

    // Небольшой рывок клешней вниз давление при первом контакте
    public static final float CLAW_INITIAL_PRESS_IMPULSE = 0.02f;

    // Выпадание из клешни, ложный захват
    public static final float BASE_FAKE_GRAB_CHANCE = 0.20f;
    public static final float FAKE_GRAB_DIFFICULTY_MULT = 0.4f;

    // где отпускаем (чуть выше кучи)
    public static final float FAKE_GRAB_RELEASE_Y = 3.2f;

    // =========================
    // Раскачка троса
    // =========================
    // Чем больше INPUT_MULTIPLIER, тем сильнее раскачка от движения
    public static final float SWING_INPUT_MULTIPLIER = 30f;

    // Чем больше SPRING, тем быстрее тянет к вертикали
    public static final float SWING_SPRING = 10f;

    // Чем меньше DAMPING, тем быстрее затухают колебания
    public static final float SWING_DAMPING = 0.998f;

    // Ограничение максимального отклонения
    public static final float SWING_MAX = 0.65f;

    // Почти ноль -> сразу останавливаем
    public static final float SWING_STOP_EPS = 0.0015f;

    // Ограничение раскачки velocity
    public static final float SWING_MAX_VELOCITY = 6f;

    // Коэффициенты импульса раскачки
    public static final float SWING_INPUT_BASE = 8.0f;
    public static final float SWING_ACCEL_MULT = 0.08f;
    public static final float SWING_DIRECTION_CHANGE_MULT = 0.12f;


    // =========================
    // Срыв / ранний сброс
    // =========================
    public static final float SLIP_CHECK_Y = 4.2f;

    public static final double BASE_SLIP_CHANCE = 0.22;
    public static final double SLIP_DIFFICULTY_MULT = 0.45;


    // =========================
    // Лоток
    // =========================
    public static final float TRAY_X = 14.10f;
    public static final float TRAY_Y = 0.45f;
    public static final float TRAY_W = 2.30f;
    public static final float TRAY_H = 2.25f;

    // Точка "позднего" сброса над лотком
    public static final float TRAY_DROP_X = TRAY_X - 0.45f;
    public static final float TRAY_DROP_Y = TRAY_Y + TRAY_H + 0.62f;

    // Узкая зона засчитывания внутри лотка
    public static final float TRAY_INNER_LEFT = TRAY_X - 0.40f;
    public static final float TRAY_INNER_RIGHT = TRAY_X + 0.16f;
    public static final float TRAY_INNER_BOTTOM = TRAY_Y + 0.12f;
    public static final float TRAY_INNER_TOP = TRAY_Y + 1.28f;

    // Физика лотка
    public static final float TRAY_FLOOR_FRICTION = 0.34f;
    public static final float TRAY_FLOOR_RESTITUTION = 0.22f;

    public static final float TRAY_WALL_FRICTION = 0.22f;
    public static final float TRAY_WALL_RESTITUTION = 0.62f;

    // =========================
    // Игрушки (базовая физика)
    // =========================
    public static final float TOY_RADIUS = 0.38f;
    public static final float TOY_DRAW_W = 0.90f;
    public static final float TOY_DRAW_H = 0.90f;

    // Базовая физика игрушек в куче
    public static final float TOY_DENSITY = 0.7f;
    public static final float TOY_FRICTION = 0.6f;
    public static final float TOY_RESTITUTION = 0.12f;
    public static final float TOY_LINEAR_DAMPING = 0.90f;
    public static final float TOY_ANGULAR_DAMPING = 1.4f;

    // =========================
    // Полёт игрушки (🔥 ключевая часть)
    // =========================

    // Случайный разброс по X
    public static final float RELEASE_RANDOM_X = 0.3f;

    // Доп. разброс при раннем сбросе
    public static final float RELEASE_RANDOM_X_EARLY = 0.3f;

    // Импульс "соскальзывания"
    public static final float RELEASE_SLIDE_IMPULSE = 0.25f;

    // Ограничение горизонтальной скорости
    public static final float RELEASE_MAX_VX = 2.0f;

    // Вертикальное падение
    public static final float RELEASE_BASE_VY = -0.9f;
    public static final float RELEASE_RANDOM_VY = 0.2f;


    // =========================
    // Поведение в лотке
    // =========================
    public static final float TOY_TRAY_GRAVITY_SCALE = 0.46f;

    // Когда считаем, что игрушка осела в лотке
    public static final float TOY_SETTLE_SPEED = 0.08f;
    public static final float TOY_SETTLE_ANGULAR_SPEED = 0.10f;
    public static final float TOY_SETTLE_TIME = 0.70f;

    // Когда промахнувшуюся игрушку возвращаем в обычную игру
    public static final float TOY_BACK_ON_FLOOR_Y = 1.25f;
    public static final float TOY_BACK_ON_FLOOR_MAX_VY = 0.8f;

    // =========================
    // Выпадение из клешни (новая физика)
    // =========================
    public static final float CLAW_DROP_BASE_CHANCE = 0.235f;
    public static final float CLAW_DROP_DIFFICULTY_MULT = 0.08f;
    public static final float CLAW_CHECK_DROP_CHANCE_TIMER = 1f;
    public static final float CLAW_DROP_MIN_CHANCE = 0.08f;

    // =========================
    // Болтание игрушки в лапах
    // =========================
    public static final float CLAW_WOBBLE_AMPLITUDE_X = 0.03f;
    public static final float CLAW_WOBBLE_AMPLITUDE_Y = 0.02f;

    public static final float CLAW_WOBBLE_FREQ_X = 18f;
    public static final float CLAW_WOBBLE_FREQ_Y = 14f;

    // Продавливание
    public static final float SUPPORT_CHECK_DY = 0.5f;
    public static final float SUPPORT_CHECK_DX = 0.5f;

    // Передача скорости игрушки от клешни
    public static final float CLAW_VELOCITY_TRANSFER = 3.0f;

    // =========================
    // Фиксированный шаг физики
    // =========================
    // Шаг симуляции Box2D (секунды). Держим его константным, чтобы
    // скорость игры не зависела от FPS устройства.
    public static final float PHYSICS_TIME_STEP = 1f / 60f;
    // Ограничение входящего delta для аккумулятора, чтобы после лагов
    // не запускать слишком много шагов за один кадр.
    public static final float PHYSICS_MAX_ACCUMULATED_TIME = 0.25f;

    // =========================
    // Коты в режиме CATCH_CAT
    // =========================
    public static final float CAT_MOTION_MIN_X = 2.1f;
    public static final float CAT_MOTION_MAX_X = WORLD_WIDTH - 2.1f;
    public static final float CAT_MOTION_MIN_SPEED = 1.44f;
    public static final float CAT_MOTION_MAX_SPEED = 3.75f;
    // Ограничиваем падение отдельно от подъёма:
    // при симметричном clamp коты визуально "топчутся" и почти не набирают высоту.
    public static final float CAT_MOTION_MAX_FALL_SPEED = 1.20f;
    public static final float CAT_MOTION_MAX_RISE_SPEED = 6.00f;
    public static final float CAT_MOTION_GROUND_Y = 1.42f;
    public static final float CAT_MOTION_GROUND_MAX_VY = 0.25f;
    // Погрешность по Y для определения опоры под котом (пол/другой кот).
    public static final float CAT_MOTION_SUPPORT_Y_EPS = 0.02f;
    // Скорость "подтягивания" текущей скорости к целевой (1/сек).
    // Формула в коде использует экспоненциальное сглаживание, поэтому
    // эффект одинаковый при любом FPS.
    public static final float CAT_MOTION_VELOCITY_RESPONSE = 8.5f;
    public static final float CAT_MOTION_STUCK_SPEED = 0.06f;
    // Минимальный прогресс по X за кадр, ниже которого считаем, что кот "топчется".
    public static final float CAT_MOTION_STUCK_PROGRESS_EPS = 0.0035f;
    public static final float CAT_MOTION_STUCK_TIME = 0.42f;
    // Вероятность "перепрыга", когда кот уткнулся в другого кота и залип.
    public static final float CAT_MOTION_STUCK_HOP_CHANCE = 0.88f;
    public static final float CAT_MOTION_UNSTICK_JUMP_MIN = 0.155f;
    public static final float CAT_MOTION_UNSTICK_JUMP_RANDOM = 0.025f;
    public static final float CAT_MOTION_UNSTICK_SIDE_IMPULSE = 0.018f;
    public static final float CAT_MOTION_JUMP_MIN = 0.040f;
    public static final float CAT_MOTION_JUMP_RANDOM = 0.120f;
    public static final float CAT_MOTION_JUMP_SIDE_IMPULSE = 0.014f;
    // Интервалы обычных прыжков (сек).
    public static final float CAT_MOTION_JUMP_INTERVAL_MIN = 0.25f;
    public static final float CAT_MOTION_JUMP_INTERVAL_RANDOM = 0.45f;
    // Пауза после anti-stuck перед следующей попыткой прыжка (сек).
    public static final float CAT_MOTION_UNSTICK_REJUMP_DELAY_MIN = 0.18f;
    public static final float CAT_MOTION_UNSTICK_REJUMP_DELAY_RANDOM = 0.24f;

    // =========================
    // Экономика
    // =========================
    // Ежедневное пополнение монет происходит в локальном времени устройства.
    public static final int DAILY_COIN_REFILL_HOUR = 0;
    public static final int DAILY_COIN_REFILL_MINUTE = 0;

    // =========================
    // Аудио (музыка фона)
    // =========================
    public static final float MUSIC_OVERLAP_DURATION_DESKTOP = 0.08f;
    public static final float MUSIC_OVERLAP_DURATION_ANDROID = 0.14f;
    public static final float MUSIC_CROSSFADE_START_GUARD_ANDROID = 0.03f;
    public static final float MIN_TRACK_DURATION_FOR_OVERLAP = 1.0f;
}
