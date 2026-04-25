package ru.captainxsander;

import java.util.Arrays;

/**
 * Каталог всех игрушек, которые могут появиться в игре и в зверинце.
 * Для каждой игрушки здесь хранится:
 * путь к картинке,
 * заголовок карточки,
 * текст на обратной стороне карточки.
 */
public enum ToyType {
    // Ниже перечислены все поддерживаемые игрушки проекта.
    BEAR("toys/animals/bear.png", "Bear", "Bear card text placeholder."),
    BIRDY("toys/animals/birdy.png", "Birdy", "Birdy card text placeholder."),
    BIRD_SMALL("toys/default/bird_small.png", "Bird Small", "Bird Small card text placeholder."),
    BULL("toys/animals/bull.png", "Bull", "Bull card text placeholder."),
    CAT_ANIMAL("toys/animals/cat_normal.png", "Cat", "Cat card text placeholder."),
    CAT_BORED("toys/cats/cat_bored.png", "Cat Bored", "Cat Bored card text placeholder."),
    CAT_CRYING("toys/cats/cat_crying.png", "Cat Crying", "Cat Crying card text placeholder."),
    CAT_EVIL("toys/cats/cat_evil.png", "Cat Evil", "Cat Evil card text placeholder."),
    CAT_FUNNY("toys/cats/cat_funny.png", "Cat Funny", "Cat Funny card text placeholder."),
    CAT_ILL("toys/cats/cat_ill.png", "Cat Ill", "Cat Ill card text placeholder."),
    CAT_NORMAL("toys/cats/cat_normal.png", "Cat Normal", "Cat Normal card text placeholder."),
    COALA("toys/animals/coala.png", "Coala", "Coala card text placeholder."),
    COW("toys/animals/cow.png", "Cow", "Cow card text placeholder."),
    CRABE("toys/animals/crabe.png", "Crabe", "Crabe card text placeholder."),
    CROC("toys/animals/croc.png", "Croc", "Croc card text placeholder."),
    CUTE("toys/animals/cute.png", "Cute", "Cute card text placeholder."),
    DEER("toys/animals/deer.png", "Deer", "Deer card text placeholder."),
    DOG("toys/animals/dog.png", "Dog", "Dog card text placeholder."),
    ELEPHANT("toys/animals/elephant.png", "Elephant", "Elephant card text placeholder."),
    FOXY("toys/animals/foxy.png", "Foxy", "Foxy card text placeholder."),
    HEART("toys/default/heart.png", "Heart", "Heart card text placeholder."),
    MONKEY("toys/animals/monkey.png", "Monkey", "Monkey card text placeholder."),
    MORGE("toys/animals/morge.png", "Morge", "Morge card text placeholder."),
    MOUSE("toys/animals/mouse.png", "Mouse", "Mouse card text placeholder."),
    PIG("toys/default/pig.png", "Pig", "Pig card text placeholder."),
    PIGI("toys/animals/pigi.png", "Pigi", "Pigi card text placeholder."),
    PINGUI("toys/default/pingui.png", "Pingui", "Pingui card text placeholder."),
    PINGUI_SNOW("toys/animals/pingui_snow.png", "Pingui Snow", "Pingui Snow card text placeholder."),
    RABBIT("toys/rabbit.png", "Rabbit", "Rabbit card text placeholder."),
    RABBIT_BIG("toys/default/rabbit_big.png", "Rabbit Big", "Rabbit Big card text placeholder."),
    RABBIT_LARGE("toys/animals/rabbit_large.png", "Rabbit Large", "Rabbit Large card text placeholder."),
    RACOON("toys/animals/racoon.png", "Racoon", "Racoon card text placeholder."),
    SNAKE("toys/animals/snake.png", "Snake", "Snake card text placeholder."),
    SNOW_BEAR("toys/animals/snow_bear.png", "Snow Bear", "Snow Bear card text placeholder."),
    SQUIRELL("toys/animals/squirell.png", "Squirell", "Squirell card text placeholder."),
    WHALE("toys/animals/whale.png", "Whale", "Whale card text placeholder."),
    ZIRAFFE("toys/animals/ziraffe.png", "Ziraffe", "Ziraffe card text placeholder.");

    /**
     * Базовые игрушки из пакета toys/default для первого запуска NORMAL.
     */
    public static final ToyType[] DEFAULT_POOL =
        Arrays.stream(values())
            .filter(toyType -> toyType.texturePath.startsWith("toys/default/"))
            .toArray(ToyType[]::new);

    /**
     * Полный набор зверей из пакета toys/animals для уровней RESCUE/FIND.
     */
    public static final ToyType[] ANIMAL_POOL =
        Arrays.stream(values())
            .filter(toyType -> toyType.texturePath.startsWith("toys/animals/"))
            .toArray(ToyType[]::new);

    /**
     * Набор котов с эмоциями для режима CATCH_CAT.
     */
    public static final ToyType[] CAT_EMOTION_POOL =
        Arrays.stream(values())
            .filter(toyType -> toyType.texturePath.startsWith("toys/cats/"))
            .toArray(ToyType[]::new);

    // Путь к изображению игрушки в каталоге assets.
    private final String texturePath;

    // Заголовок карточки для обратной стороны зверинца.
    private final String title;

    // Текст, который показывается на обороте карточки.
    private final String cardText;

    ToyType(String texturePath, String title, String cardText) {
        this.texturePath = texturePath;
        this.title = title;
        this.cardText = cardText;
    }

    public String getTexturePath() {
        return texturePath;
    }

    public String getTitle() {
        return title;
    }

    public String getCardText() {
        return cardText;
    }
}
