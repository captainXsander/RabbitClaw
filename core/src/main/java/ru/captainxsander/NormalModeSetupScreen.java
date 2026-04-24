package ru.captainxsander;

import com.badlogic.gdx.math.Rectangle;

import java.util.Arrays;

class NormalModeSetupScreen extends AbstractDetailMenuScreen {
    private static final int ITEMS_PER_PAGE = 5;
    private static final float MODE_TEXT_SCALE = 0.0102f;

    private final MenagerieProgress progress = new MenagerieProgress();
    private final ToyType[] availablePool;

    private final Rectangle toysPanelBounds = new Rectangle(1.6f, 2.05f, 6.9f, 3.55f);
    private final Rectangle settingsPanelBounds = new Rectangle(9.15f, 2.05f, 5.25f, 3.55f);
    private final Rectangle prevPageBounds = new Rectangle(1.8f, 1.25f, 0.95f, 0.52f);
    private final Rectangle nextPageBounds = new Rectangle(7.35f, 1.25f, 0.95f, 0.52f);

    private final Rectangle[] toyBounds = new Rectangle[ITEMS_PER_PAGE];
    private final Rectangle slipPresetBounds = new Rectangle(9.45f, 4.7f, 4.65f, 0.55f);
    private final Rectangle dropBasePresetBounds = new Rectangle(9.45f, 4.05f, 4.65f, 0.55f);
    private final Rectangle dropMinPresetBounds = new Rectangle(9.45f, 3.4f, 4.65f, 0.55f);
    private final Rectangle fakePresetBounds = new Rectangle(9.45f, 2.75f, 4.65f, 0.55f);
    private final Rectangle playBounds = new Rectangle(9.45f, 1.25f, 2.2f, 0.55f);
    private final Rectangle backBounds = new Rectangle(11.9f, 1.25f, 2.2f, 0.55f);

    private int page;

    NormalModeSetupScreen(MainGame game) {
        super(game);
        availablePool = progress.getNormalModePool();

        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            toyBounds[i] = new Rectangle(1.85f, 5.05f - i * 0.58f, 6.35f, 0.5f);
            actionBounds.add(toyBounds[i]);
        }

        actionBounds.add(prevPageBounds);
        actionBounds.add(nextPageBounds);
        actionBounds.add(slipPresetBounds);
        actionBounds.add(dropBasePresetBounds);
        actionBounds.add(dropMinPresetBounds);
        actionBounds.add(fakePresetBounds);
        actionBounds.add(playBounds);
        actionBounds.add(backBounds);
        selectedIndex = ITEMS_PER_PAGE + 6;
    }

    @Override
    protected void drawContent() {
        drawMenuTitle("Режим: Обычная игра");
        drawParagraph(
            "Выберите зверей для раунда и настройте сложность. В раунде 45 игрушек, выбранные типы"
                + " распределяются равномерно.",
            new Rectangle(2.4f, 6.05f, 11.2f, 0.85f)
        );

        drawButton(toysPanelBounds, "", false);
        drawButton(settingsPanelBounds, "", false);

        drawCenteredText(bodyFont, "Звери для раунда", new Rectangle(1.9f, 5.38f, 6.2f, 0.35f), MODE_TEXT_SCALE, 1);
        drawCenteredText(bodyFont, "Сложность", new Rectangle(9.5f, 5.38f, 4.6f, 0.35f), MODE_TEXT_SCALE, 1);

        drawToyList();
        drawDifficultySection();

        drawButton(playBounds, "Играть", selectedIndex == ITEMS_PER_PAGE + 6);
        drawButton(backBounds, "Назад", selectedIndex == ITEMS_PER_PAGE + 7);
    }

    private void drawToyList() {
        int totalPages = getPageCount();
        int start = page * ITEMS_PER_PAGE;

        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            int toyIndex = start + i;
            if (toyIndex >= availablePool.length) {
                continue;
            }

            ToyType toyType = availablePool[toyIndex];
            boolean checked = Arrays.asList(game.getNormalSelectedToyTypes(availablePool)).contains(toyType);
            String label = (checked ? "☑ " : "☐ ") + getToyLabelRu(toyType);
            drawButton(toyBounds[i], label, selectedIndex == i);
        }

        drawButton(prevPageBounds, "<", selectedIndex == ITEMS_PER_PAGE && page > 0);
        drawButton(nextPageBounds, ">", selectedIndex == ITEMS_PER_PAGE + 1 && page < totalPages - 1);
        drawCenteredText(bodyFont, (page + 1) + "/" + totalPages,
            new Rectangle(4.5f, 1.25f, 1.0f, 0.5f), MODE_TEXT_SCALE, 1);
    }

    private void drawDifficultySection() {
        drawButton(slipPresetBounds, "Срыв при подъёме: " + getLevelLabel(game.getNormalBaseSlipChance(), 0.16, 0.26),
            selectedIndex == ITEMS_PER_PAGE + 2);
        drawButton(dropBasePresetBounds, "Выпадение в пути: " + getLevelLabel(game.getNormalClawDropBaseChance(), 0.18, 0.28),
            selectedIndex == ITEMS_PER_PAGE + 3);
        drawButton(dropMinPresetBounds, "Мин. выпадение: " + getLevelLabel(game.getNormalClawDropMinChance(), 0.06, 0.10),
            selectedIndex == ITEMS_PER_PAGE + 4);
        drawButton(fakePresetBounds, "Ложный захват: " + getLevelLabel(game.getNormalBaseFakeGrabChance(), 0.14, 0.24),
            selectedIndex == ITEMS_PER_PAGE + 5);

        drawCenteredText(
            bodyFont,
            "Нажмите на параметр для смены уровня",
            new Rectangle(9.35f, 2.12f, 4.9f, 0.35f),
            MODE_TEXT_SCALE,
            1
        );
    }

    @Override
    protected int getActionCount() {
        return ITEMS_PER_PAGE + 8;
    }

    @Override
    protected void onActionTriggered(int actionIndex) {
        if (actionIndex < ITEMS_PER_PAGE) {
            int toyIndex = page * ITEMS_PER_PAGE + actionIndex;
            if (toyIndex < availablePool.length) {
                game.toggleNormalToy(availablePool[toyIndex], availablePool);
            }
            return;
        }

        if (actionIndex == ITEMS_PER_PAGE) {
            if (page > 0) {
                page--;
            }
            return;
        }
        if (actionIndex == ITEMS_PER_PAGE + 1) {
            if (page < getPageCount() - 1) {
                page++;
            }
            return;
        }
        if (actionIndex == ITEMS_PER_PAGE + 2) {
            game.setNormalBaseSlipChance(nextPreset(game.getNormalBaseSlipChance(), 0.16, 0.22, 0.30));
            return;
        }
        if (actionIndex == ITEMS_PER_PAGE + 3) {
            game.setNormalClawDropBaseChance((float) nextPreset(game.getNormalClawDropBaseChance(), 0.18, 0.24, 0.32));
            return;
        }
        if (actionIndex == ITEMS_PER_PAGE + 4) {
            game.setNormalClawDropMinChance((float) nextPreset(game.getNormalClawDropMinChance(), 0.06, 0.09, 0.12));
            return;
        }
        if (actionIndex == ITEMS_PER_PAGE + 5) {
            game.setNormalBaseFakeGrabChance((float) nextPreset(game.getNormalBaseFakeGrabChance(), 0.14, 0.20, 0.28));
            return;
        }
        if (actionIndex == ITEMS_PER_PAGE + 6) {
            game.startNormalGame();
            return;
        }

        game.showPreviousMenu();
    }

    private int getPageCount() {
        return Math.max(1, (int) Math.ceil(availablePool.length / (float) ITEMS_PER_PAGE));
    }

    private String getLevelLabel(double value, double lowToMedium, double mediumToHigh) {
        if (value < lowToMedium) {
            return "Низкий";
        }
        if (value < mediumToHigh) {
            return "Средний";
        }
        return "Высокий";
    }

    private double nextPreset(double value, double low, double medium, double high) {
        double eps = 0.0001;
        if (Math.abs(value - low) < eps) {
            return medium;
        }
        if (Math.abs(value - medium) < eps) {
            return high;
        }
        return low;
    }

    private String getToyLabelRu(ToyType toyType) {
        String title = toyType.getTitle();
        return title
            .replace("Bear", "Медведь")
            .replace("Bird Small", "Птичка")
            .replace("Birdy", "Птица")
            .replace("Bull", "Бык")
            .replace("Cat", "Кот")
            .replace("Coala", "Коала")
            .replace("Cow", "Корова")
            .replace("Crabe", "Краб")
            .replace("Croc", "Крокодил")
            .replace("Cute", "Малыш")
            .replace("Deer", "Олень")
            .replace("Dog", "Собака")
            .replace("Elephant", "Слон")
            .replace("Foxy", "Лиса")
            .replace("Heart", "Сердце")
            .replace("Monkey", "Обезьяна")
            .replace("Morge", "Морж")
            .replace("Mouse", "Мышь")
            .replace("Pig", "Свинка")
            .replace("Pigi", "Поросёнок")
            .replace("Pingui Snow", "Снежный пингвин")
            .replace("Pingui", "Пингвин")
            .replace("Rabbit Big", "Большой кролик")
            .replace("Rabbit Large", "Крупный кролик")
            .replace("Rabbit", "Кролик")
            .replace("Racoon", "Енот")
            .replace("Snake", "Змея")
            .replace("Snow Bear", "Белый медведь")
            .replace("Squirell", "Белка")
            .replace("Whale", "Кит")
            .replace("Ziraffe", "Жираф");
    }
}
