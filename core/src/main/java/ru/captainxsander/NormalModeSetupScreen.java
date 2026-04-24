package ru.captainxsander;

import com.badlogic.gdx.math.Rectangle;

import java.util.Arrays;

class NormalModeSetupScreen extends AbstractDetailMenuScreen {
    private static final int ITEMS_PER_PAGE = 8;

    private final MenagerieProgress progress = new MenagerieProgress();
    private final ToyType[] availablePool;

    private final Rectangle toysPanelBounds = new Rectangle(1.2f, 1.35f, 8.9f, 4.55f);
    private final Rectangle settingsPanelBounds = new Rectangle(10.25f, 1.35f, 4.55f, 4.55f);
    private final Rectangle prevPageBounds = new Rectangle(1.45f, 1.55f, 1.0f, 0.55f);
    private final Rectangle nextPageBounds = new Rectangle(8.85f, 1.55f, 1.0f, 0.55f);

    private final Rectangle[] toyBounds = new Rectangle[ITEMS_PER_PAGE];
    private final Rectangle slipPresetBounds = new Rectangle(10.5f, 4.65f, 4.05f, 0.62f);
    private final Rectangle dropBasePresetBounds = new Rectangle(10.5f, 3.9f, 4.05f, 0.62f);
    private final Rectangle dropMinPresetBounds = new Rectangle(10.5f, 3.15f, 4.05f, 0.62f);
    private final Rectangle fakePresetBounds = new Rectangle(10.5f, 2.4f, 4.05f, 0.62f);
    private final Rectangle playBounds = new Rectangle(10.25f, 1.45f, 2.2f, 0.65f);
    private final Rectangle backBounds = new Rectangle(12.6f, 1.45f, 2.2f, 0.65f);

    private int page;

    NormalModeSetupScreen(MainGame game) {
        super(game);
        availablePool = progress.getNormalModePool();

        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            toyBounds[i] = new Rectangle(1.45f, 5.1f - i * 0.45f, 8.4f, 0.4f);
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
            "Выберите игрушки для раунда и настройте сложность. В раунде 45 игрушек, выбранные типы"
                + " распределяются равномерно.",
            new Rectangle(2.45f, 6.1f, 11.2f, 0.8f)
        );

        drawButton(toysPanelBounds, "", false);
        drawButton(settingsPanelBounds, "", false);

        drawCenteredText(bodyFont, "Игрушки для раунда", new Rectangle(1.45f, 5.45f, 8.4f, 0.4f), 0.0104f, 1);
        drawCenteredText(bodyFont, "Сложность", new Rectangle(10.5f, 5.45f, 4.05f, 0.4f), 0.0104f, 1);

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
            String label = (checked ? "☑ " : "☐ ") + toyType.getTitle();
            drawButton(toyBounds[i], label, selectedIndex == i);
        }

        drawButton(prevPageBounds, "<", selectedIndex == ITEMS_PER_PAGE && page > 0);
        drawButton(nextPageBounds, ">", selectedIndex == ITEMS_PER_PAGE + 1 && page < totalPages - 1);
        drawCenteredText(bodyFont, (page + 1) + "/" + totalPages,
            new Rectangle(5.05f, 1.58f, 1.4f, 0.5f), 0.0102f, 1);
    }

    private void drawDifficultySection() {
        drawButton(slipPresetBounds, "Шанс срыва: " + getLevelLabel(game.getNormalBaseSlipChance(), 0.16, 0.26),
            selectedIndex == ITEMS_PER_PAGE + 2);
        drawButton(dropBasePresetBounds, "Шанс выпадения в пути: " + getLevelLabel(game.getNormalClawDropBaseChance(), 0.18, 0.28),
            selectedIndex == ITEMS_PER_PAGE + 3);
        drawButton(dropMinPresetBounds, "Мин. шанс выпадения: " + getLevelLabel(game.getNormalClawDropMinChance(), 0.06, 0.10),
            selectedIndex == ITEMS_PER_PAGE + 4);
        drawButton(fakePresetBounds, "Ложный захват: " + getLevelLabel(game.getNormalBaseFakeGrabChance(), 0.14, 0.24),
            selectedIndex == ITEMS_PER_PAGE + 5);

        drawParagraph(
            "Нажмите на параметр, чтобы переключить уровень: Низкий → Средний → Высокий.",
            new Rectangle(10.35f, 1.95f, 4.25f, 0.55f)
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
}
