package ru.captainxsander;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Align;

import java.util.Arrays;

class NormalModeSetupScreen extends AbstractDetailMenuScreen {
    private final MenagerieProgress progress = new MenagerieProgress();
    private final ToyType[] availablePool;
    private int toyCursor;

    private final Rectangle toyToggleBounds = new Rectangle(2.0f, 3.65f, 12f, 0.75f);
    private final Rectangle slipBounds = new Rectangle(2.0f, 2.8f, 12f, 0.7f);
    private final Rectangle dropBaseBounds = new Rectangle(2.0f, 2.05f, 12f, 0.7f);
    private final Rectangle dropMinBounds = new Rectangle(2.0f, 1.3f, 12f, 0.7f);
    private final Rectangle fakeGrabBounds = new Rectangle(2.0f, 0.55f, 12f, 0.7f);
    private final Rectangle playBounds = new Rectangle(10.0f, 4.55f, 3.6f, 0.75f);
    private final Rectangle backBounds = new Rectangle(2.0f, 4.55f, 3.6f, 0.75f);

    NormalModeSetupScreen(MainGame game) {
        super(game);
        availablePool = progress.getNormalModePool();

        actionBounds.add(toyToggleBounds);
        actionBounds.add(slipBounds);
        actionBounds.add(dropBaseBounds);
        actionBounds.add(dropMinBounds);
        actionBounds.add(fakeGrabBounds);
        actionBounds.add(playBounds);
        actionBounds.add(backBounds);
        selectedIndex = 5;
    }

    @Override
    protected void drawContent() {
        drawMenuTitle("Режим: Обычная игра");
        drawParagraph(
            "Выберите доступных зверей и сложность. В раунде будет 45 игрушек, выбранные звери"
                + " распределяются равномерно между спавнами.",
            new Rectangle(1.3f, 5.7f, 13.4f, 1.0f)
        );

        String toyLabel = "Нет доступных зверей";
        if (availablePool.length > 0) {
            ToyType currentToy = availablePool[Math.max(0, Math.min(toyCursor, availablePool.length - 1))];
            boolean selectedToy = Arrays.asList(game.getNormalSelectedToyTypes(availablePool)).contains(currentToy);
            toyLabel = (selectedToy ? "[Вкл] " : "[Выкл] ") + currentToy.getTitle() + "  (◀/▶ смена зверя)";
        }

        drawButton(backBounds, "Назад", selectedIndex == 6);
        drawButton(playBounds, "Играть", selectedIndex == 5);
        drawButton(toyToggleBounds, "Зверь: " + toyLabel, selectedIndex == 0);

        drawButton(slipBounds,
            String.format("BASE_SLIP_CHANCE: %.2f — шанс срыва при подъёме", game.getNormalBaseSlipChance()),
            selectedIndex == 1);
        drawButton(dropBaseBounds,
            String.format("CLAW_DROP_BASE_CHANCE: %.2f — базовый шанс выпадения", game.getNormalClawDropBaseChance()),
            selectedIndex == 2);
        drawButton(dropMinBounds,
            String.format("CLAW_DROP_MIN_CHANCE: %.2f — минимальный шанс выпадения", game.getNormalClawDropMinChance()),
            selectedIndex == 3);
        drawButton(fakeGrabBounds,
            String.format("BASE_FAKE_GRAB_CHANCE: %.2f — шанс ложного захвата", game.getNormalBaseFakeGrabChance()),
            selectedIndex == 4);

        drawCenteredText(
            bodyFont,
            "Изменение: Enter/Space на пункте или ◀/▶. Для шансов шаг 0.01.",
            new Rectangle(1.3f, 4.95f, 13.4f, 0.4f),
            0.0098f,
            Align.center
        );
    }

    @Override
    protected int getActionCount() {
        return 7;
    }

    @Override
    protected void onActionTriggered(int actionIndex) {
        switch (actionIndex) {
            case 0:
                if (availablePool.length > 0) {
                    game.toggleNormalToy(availablePool[toyCursor], availablePool);
                }
                break;
            case 1:
                game.setNormalBaseSlipChance(game.getNormalBaseSlipChance() + 0.01);
                break;
            case 2:
                game.setNormalClawDropBaseChance(game.getNormalClawDropBaseChance() + 0.01f);
                break;
            case 3:
                game.setNormalClawDropMinChance(game.getNormalClawDropMinChance() + 0.01f);
                break;
            case 4:
                game.setNormalBaseFakeGrabChance(game.getNormalBaseFakeGrabChance() + 0.01f);
                break;
            case 5:
                game.startNormalGame();
                break;
            default:
                game.showPreviousMenu();
                break;
        }
    }

    @Override
    protected boolean onExtraKeyDown(int keycode) {
        if (keycode == Input.Keys.LEFT || keycode == Input.Keys.A) {
            adjustSelectionOrValue(-1);
            return true;
        }

        if (keycode == Input.Keys.RIGHT || keycode == Input.Keys.D) {
            adjustSelectionOrValue(1);
            return true;
        }

        return false;
    }

    private void adjustSelectionOrValue(int direction) {
        if (selectedIndex == 0) {
            if (availablePool.length > 0) {
                toyCursor = (toyCursor + direction + availablePool.length) % availablePool.length;
            }
            return;
        }

        float delta = direction > 0 ? 0.01f : -0.01f;
        if (selectedIndex == 1) {
            game.setNormalBaseSlipChance(game.getNormalBaseSlipChance() + delta);
            return;
        }
        if (selectedIndex == 2) {
            game.setNormalClawDropBaseChance(game.getNormalClawDropBaseChance() + delta);
            return;
        }
        if (selectedIndex == 3) {
            game.setNormalClawDropMinChance(game.getNormalClawDropMinChance() + delta);
            return;
        }
        if (selectedIndex == 4) {
            game.setNormalBaseFakeGrabChance(game.getNormalBaseFakeGrabChance() + delta);
        }
    }
}
