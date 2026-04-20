package ru.captainxsander;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Загружает и раздаёт факты для режима "Найти зверей".
 *
 * Источник данных: assets/find_animal_facts.json.
 */
public class FindAnimalFacts {
    // Конфигурация фактов хранится в assets рядом с остальными игровыми ресурсами.
    private static final String FACTS_PATH = "find_animal_facts.json";

    // Таблица соответствия конкретной игрушки и "идентификатора животного" из JSON.
    private final Map<ToyType, String> toyTypeToAnimal = new HashMap<>();
    // Набор фактов по каждому идентификатору животного.
    private final Map<String, Array<String>> animalFacts = new HashMap<>();

    public FindAnimalFacts() {
        loadFromJson();
    }

    public FindAnimalTask createRandomTask(ToyType[] allowedToyTypes) {
        // Формируем список только тех игрушек, для которых:
        // 1) есть связь с животным;
        // 2) у животного есть хотя бы один факт.
        Array<ToyType> eligibleToyTypes = new Array<>();

        for (ToyType toyType : allowedToyTypes) {
            if (!toyTypeToAnimal.containsKey(toyType)) {
                continue;
            }

            String animalId = toyTypeToAnimal.get(toyType);
            Array<String> facts = animalFacts.get(animalId);
            if (facts == null || facts.size == 0) {
                continue;
            }

            eligibleToyTypes.add(toyType);
        }

        if (eligibleToyTypes.size == 0) {
            // Явная ошибка конфигурации — запуск режима без валидных данных невозможен.
            throw new IllegalStateException("No eligible toy types with facts for FIND_ANIMAL mode");
        }

        // Сначала выбираем животное-цель через тип игрушки, затем случайный факт для него.
        ToyType targetToyType = eligibleToyTypes.random();
        String animalId = toyTypeToAnimal.get(targetToyType);
        Array<String> facts = animalFacts.get(animalId);
        String fact = facts.random();

        return new FindAnimalTask(targetToyType, fact);
    }

    private void loadFromJson() {
        // Загружаем JSON один раз на старте раунда.
        FileHandle fileHandle = Gdx.files.internal(FACTS_PATH);
        JsonValue root = new JsonReader().parse(fileHandle);

        // toyTypeToAnimal: ключ — имя enum ToyType, значение — id животного.
        JsonValue toyToAnimalJson = root.get("toyTypeToAnimal");
        for (JsonValue entry = toyToAnimalJson.child; entry != null; entry = entry.next) {
            ToyType toyType = ToyType.valueOf(entry.name);
            toyTypeToAnimal.put(toyType, entry.asString());
        }

        // facts: для каждого id животного читаем массив строк-фактов.
        JsonValue factsJson = root.get("facts");
        for (JsonValue animalEntry = factsJson.child; animalEntry != null; animalEntry = animalEntry.next) {
            Array<String> facts = new Array<>();
            for (JsonValue fact = animalEntry.child; fact != null; fact = fact.next) {
                facts.add(fact.asString());
            }
            animalFacts.put(animalEntry.name, facts);
        }
    }

    public static final class FindAnimalTask {
        private final ToyType targetToyType;
        private final String fact;

        private FindAnimalTask(ToyType targetToyType, String fact) {
            this.targetToyType = targetToyType;
            this.fact = fact;
        }

        public ToyType getTargetToyType() {
            return targetToyType;
        }

        public String getFact() {
            return fact;
        }
    }
}
