package ru.captainxsander;

public final class GameTuning {

    private GameTuning() {}

    // =========================
    // Мир
    // =========================
    public static final float WORLD_WIDTH = 16f;
    public static final float WORLD_HEIGHT = 9f;

    /** Позиция сброса над лотком */
    public static final float TRAY_DROP_X = 14.0f;

    // =========================
    // Клешня (ВСЁ КАК БЫЛО)
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

    // =========================
    // Раскачка (если используется)
    // =========================
    public static final float SWING_SPRING = 20f;
    public static final float SWING_DAMPING = 0.9f;
    public static final float SWING_INPUT_MULTIPLIER = 30f;
    public static final float SWING_STOP_EPS = 0.001f;
    public static final float SWING_MAX = 0.35f;

    // =========================
    // Игрушки (старое)
    // =========================
    public static final float TOY_RADIUS = 0.38f;

    public static final float TOY_DENSITY = 0.7f;
    public static final float TOY_FRICTION = 1.0f;
    public static final float TOY_RESTITUTION = 0.04f;

    public static final float TOY_LINEAR_DAMPING = 0.90f;
    public static final float TOY_ANGULAR_DAMPING = 1.4f;

    public static final float TOY_TRAY_GRAVITY_SCALE = 0.26f;

    public static final float TOY_SETTLE_SPEED = 0.08f;
    public static final float TOY_SETTLE_ANGULAR_SPEED = 0.10f;
    public static final float TOY_SETTLE_TIME = 0.70f;

    public static final float TOY_BACK_ON_FLOOR_Y = 1.25f;
    public static final float TOY_BACK_ON_FLOOR_MAX_VY = 0.8f;

    // =========================
    // 🔥 НОВОЕ — ПЛАВНЫЙ СБРОС
    // =========================

    // задержка "висит в клешне"
    public static final float RELEASE_DELAY_MIN = 0.08f;
    public static final float RELEASE_DELAY_MAX = 0.20f;

    // соскальзывание
    public static final float SLIDE_TIME_MIN = 0.12f;
    public static final float SLIDE_TIME_MAX = 0.22f;

    public static final float SLIDE_SPEED = 0.6f;
    public static final float SLIDE_FALL_SPEED = -0.02f;

    // начальная гравитация
    public static final float RELEASE_GRAVITY_SCALE = 0.12f;

    // начальная скорость
    public static final float RELEASE_INITIAL_VY = -0.05f;

    public static final float RELEASE_TO_CENTER_FORCE = 0.25f;
    public static final float RELEASE_RANDOM_X = 0.5f;

    public static final float RELEASE_ANGULAR_VEL = 0.6f;

    /* =========================
   🔥 ФИЗИКА КЛЕШНИ (НОВОЕ)
   ========================= */

    /** Насколько движение клешни передаётся игрушке при отпускании */
    public static final float CLAW_RELEASE_VX_TRANSFER = 0.4f;

    /** Вертикальный "пинок" при отпускании */
    public static final float CLAW_RELEASE_IMPULSE_Y = 0.1f;

    /** Случайный импульс при захвате (чтобы было ощущение контакта) */
    public static final float CLAW_CATCH_IMPULSE_X = 0.2f;
    public static final float CLAW_CATCH_IMPULSE_Y = 0.15f;

    /** Насколько движение влияет на раскачку */
    public static final float CLAW_SWING_FROM_MOVE = 30f;

    /** Насколько раскачка гасится */
    public static final float CLAW_SWING_DAMPING = 0.9f;

    /** Насколько клешня "возвращается" в центр */
    public static final float CLAW_SWING_SPRING = 20f;

}
