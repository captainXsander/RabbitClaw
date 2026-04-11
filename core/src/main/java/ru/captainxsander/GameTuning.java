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

    public static final float CLAW_MOVE_SPEED_X = 4.0f;
    public static final float CLAW_MOVE_SPEED_Y = 4.2f;

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
    public static final float EARLY_RELEASE_CHECK_X = 10.2f;

    public static final double BASE_SLIP_CHANCE = 0.22;
    public static final double SLIP_DIFFICULTY_MULT = 0.45;

    public static final double BASE_EARLY_RELEASE_CHANCE = 0.46;
    public static final double EARLY_RELEASE_DIFFICULTY_MULT = 0.28;


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

    // Насколько сильно скорость клешни влияет на игрушку
    public static final float RELEASE_VX_FROM_CLAW_MULT = 0.6f;

    // Случайный разброс по X
    public static final float RELEASE_RANDOM_X = 0.3f;

    // Доп. разброс при раннем сбросе
    public static final float RELEASE_RANDOM_X_EARLY = 0.3f;

    // Импульс "соскальзывания"
    public static final float RELEASE_SLIDE_IMPULSE = 0.25f;

    // Ограничение горизонтальной скорости
    public static final float RELEASE_MAX_VX = 2.0f;

    // Вертикальное падение
    public static final float RELEASE_BASE_VY = -0.6f;
    public static final float RELEASE_RANDOM_VY = 0.2f;

    // =========================
    // Зоны попадания в лоток
    // =========================

    // Почти гарант попадания
    public static final float TRAY_PERFECT_ZONE = 0.6f;

    // Зона, где ещё есть шанс
    public static final float TRAY_ASSIST_ZONE = 2.2f;

    // Насколько "помогаем" попасть
    public static final float TRAY_PERFECT_ASSIST_X = 0.35f;
    public static final float TRAY_PERFECT_ASSIST_Y = 0.35f;

    public static final float TRAY_ASSIST_X = 0.25f;
    public static final float TRAY_ASSIST_Y = 0.25f;

    // =========================
    // Поведение в лотке
    // =========================
    public static final float TOY_TRAY_GRAVITY_SCALE = 0.26f;

    // Когда считаем, что игрушка осела в лотке
    public static final float TOY_SETTLE_SPEED = 0.08f;
    public static final float TOY_SETTLE_ANGULAR_SPEED = 0.10f;
    public static final float TOY_SETTLE_TIME = 0.70f;

    // Когда промахнувшуюся игрушку возвращаем в обычную игру
    public static final float TOY_BACK_ON_FLOOR_Y = 1.25f;
    public static final float TOY_BACK_ON_FLOOR_MAX_VY = 0.8f;

    // Продавливание
    public static final float SUPPORT_CHECK_DY = 0.5f;
    public static final float SUPPORT_CHECK_DX = 0.5f;
}
