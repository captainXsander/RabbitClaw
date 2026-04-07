# Runtime Preset Migration

## Что добавлено
- RuntimePreset
- RuntimeTuning
- RuntimeGameScreen

## Как использовать

1. Найди место где создаётся GameScreen
2. Замени:
   new GameScreen()
   на:
   new RuntimeGameScreen()

3. Запусти игру

## Управление
- 1 — лёгкий режим
- 2 — баланс
- 3 — сложный

## Важно
Старые файлы не трогаются. Это безопасный слой поверх текущей логики.

Если понравится — можно потом полностью перейти на runtime систему.
