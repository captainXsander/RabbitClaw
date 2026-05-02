# RabbitClaw

A [libGDX](https://libgdx.com/) project generated with [gdx-liftoff](https://github.com/libgdx/gdx-liftoff).

This project was generated with a template including simple application launchers and an `ApplicationAdapter` extension that draws libGDX logo.

## Platforms

- `core`: Main module with the application logic shared by all platforms.
- `lwjgl3`: Primary desktop platform using LWJGL3; was called 'desktop' in older docs.
- `android`: Android mobile platform. Needs Android SDK.

## Gradle

This project uses [Gradle](https://gradle.org/) to manage dependencies.
The Gradle wrapper was included, so you can run Gradle tasks using `gradlew.bat` or `./gradlew` commands.
Useful Gradle tasks and flags:

- `--continue`: when using this flag, errors will not stop the tasks from running.
- `--daemon`: thanks to this flag, Gradle daemon will be used to run chosen tasks.
- `--offline`: when using this flag, cached dependency archives will be used.
- `--refresh-dependencies`: this flag forces validation of all dependencies. Useful for snapshot versions.
- `android:lint`: performs Android project validation.
- `build`: builds sources and archives of every project.
- `cleanEclipse`: removes Eclipse project data.
- `cleanIdea`: removes IntelliJ project data.
- `clean`: removes `build` folders, which store compiled classes and built archives.
- `eclipse`: generates Eclipse project data.
- `idea`: generates IntelliJ project data.
- `lwjgl3:jar`: builds application's runnable jar, which can be found at `lwjgl3/build/libs`.
- `lwjgl3:run`: starts the application.
- `test`: runs unit tests (if any).

Note that most tasks that are not specific to a single project can be run with `name:` prefix, where the `name` should be replaced with the ID of a specific project.
For example, `core:clean` removes `build` folder only from the `core` project.

Давай запомним такой список и пойдем делать улучшения по порядку: 
7) Сделать пальцы физическими телами

## Menu button generator

To keep menu buttons visually consistent, use the generator utility:

```bash
javac tools/MenuButtonGenerator.java
java tools.MenuButtonGenerator assets \
  menu_play=Играть \
  menu_settings=Настройки \
  menu_menagerie=Зверинец \
  menu_exit=Выход
```

This generates PNG files with a unified style directly in `assets/`.

## Menu button generator

To keep menu buttons visually consistent, use the generator utility:

```bash
javac tools/MenuButtonGenerator.java
java tools.MenuButtonGenerator assets \
  menu_play=Играть \
  menu_settings=Настройки \
  menu_menagerie=Зверинец \
  menu_exit=Выход
```

This generates PNG files with a unified style directly in `assets/`.

## Menu button generator

To keep menu buttons visually consistent, use the generator utility:

```bash
javac tools/MenuButtonGenerator.java
java tools.MenuButtonGenerator assets \
  menu_play=Играть \
  menu_settings=Настройки \
  menu_menagerie=Зверинец \
  menu_exit=Выход
```

This generates PNG files with a unified style directly in `assets/`.

## Menu button generator

To keep menu buttons visually consistent, use the generator utility:

```bash
javac tools/MenuButtonGenerator.java
java tools.MenuButtonGenerator assets \
  menu_play=Играть \
  menu_settings=Настройки \
  menu_menagerie=Зверинец \
  menu_exit=Выход
```

This generates PNG files with a unified style directly in `assets/`.

## Menu button generator

To keep menu buttons visually consistent, use the generator utility:

```bash
javac tools/MenuButtonGenerator.java
java tools.MenuButtonGenerator assets \
  menu_play=Играть \
  menu_settings=Настройки \
  menu_menagerie=Зверинец \
  menu_exit=Выход
```

This generates PNG files with a unified style directly in `assets/`.

## Menu button generator

To keep menu buttons visually consistent, use the generator utility:

```bash
javac tools/MenuButtonGenerator.java
java tools.MenuButtonGenerator assets \
  menu_play=Играть \
  menu_settings=Настройки \
  menu_menagerie=Зверинец \
  menu_exit=Выход
```

This generates PNG files with a unified style directly in `assets/`.

## Menu button generator

To keep menu buttons visually consistent, use the generator utility:

```bash
javac tools/MenuButtonGenerator.java
java tools.MenuButtonGenerator assets \
  menu_play=Играть \
  menu_settings=Настройки \
  menu_menagerie=Зверинец \
  menu_exit=Выход
```

This generates PNG files with a unified style directly in `assets/`.

## Claw press tuning (продавливание клешней)

Если клешня слишком сильно продавливает кучу, проверь параметры в `core/src/main/java/ru/captainxsander/GameTuning.java`:

- `CLAW_MAX_PRESS_DEPTH` — максимальная глубина продавливания. Главный ограничитель.
- `CLAW_PRESS_SPEED_MULT` — скорость набора глубины продавливания (меньше = мягче продавливание).
- `CLAW_MIN_PRESSURE_FACTOR` — минимальный остаточный коэффициент давления в конце продавливания.
- `CLAW_INITIAL_PRESS_IMPULSE` — начальный рывок вниз при первом контакте.
- `CLAW_MOVE_SPEED_Y` — вертикальная скорость клешни, тоже влияет на интенсивность продавливания.
- `SUPPORT_CHECK_DX` и `SUPPORT_CHECK_DY` — проверка опоры под игрушкой (влияет на то, когда продавливание ограничивается).

Практика настройки:

1. Уменьшай `CLAW_MAX_PRESS_DEPTH` небольшими шагами (`0.35 -> 0.30 -> 0.27`).
2. Если всё ещё есть резкий «тычок», снижай `CLAW_INITIAL_PRESS_IMPULSE` (`0.02 -> 0.015`).
3. После каждого изменения прогоняй `lwjgl3:run` и проверяй сценарий в режиме `RESCUE`.
Важно: в коде глубина продавливания ограничивается относительно высоты первого контакта с кучей (`pressStartY`), поэтому эффект параметров теперь виден сразу и не сводится только к `CLAW_MAX_PRESS_DEPTH`.


## Claw press tuning (продавливание клешней)

Если клешня слишком сильно продавливает кучу, проверь параметры в `core/src/main/java/ru/captainxsander/GameTuning.java`:

- `CLAW_MAX_PRESS_DEPTH` — максимальная глубина продавливания. Главный ограничитель.
- `CLAW_PRESS_SPEED_MULT` — скорость набора глубины продавливания (меньше = мягче продавливание).
- `CLAW_MIN_PRESSURE_FACTOR` — минимальный остаточный коэффициент давления в конце продавливания.
- `CLAW_INITIAL_PRESS_IMPULSE` — начальный рывок вниз при первом контакте.
- `CLAW_MOVE_SPEED_Y` — вертикальная скорость клешни, тоже влияет на интенсивность продавливания.
- `SUPPORT_CHECK_DX` и `SUPPORT_CHECK_DY` — проверка опоры под игрушкой (влияет на то, когда продавливание ограничивается).

Практика настройки:

1. Уменьшай `CLAW_MAX_PRESS_DEPTH` небольшими шагами (`0.35 -> 0.30 -> 0.27`).
2. Если всё ещё есть резкий «тычок», снижай `CLAW_INITIAL_PRESS_IMPULSE` (`0.02 -> 0.015`).
3. После каждого изменения прогоняй `lwjgl3:run` и проверяй сценарий в режиме `RESCUE`.
