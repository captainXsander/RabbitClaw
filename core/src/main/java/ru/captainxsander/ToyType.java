package ru.captainxsander;

/**
 * Каталог всех игрушек, которые могут появиться в игре и в зверинце.
 * Для каждой игрушки здесь хранится:
 * путь к картинке,
 * заголовок карточки,
 * текст на обратной стороне карточки.
 */
public enum ToyType {
    // Ниже перечислены все поддерживаемые игрушки проекта.
    BEAR("toys/bear.png", "Bear", "Bear card text placeholder."),
    BIRDY("toys/birdy.png", "Birdy", "Birdy card text placeholder."),
    BIRD_SMALL("toys/bird_small.png", "Bird Small", "Bird Small card text placeholder."),
    BULL("toys/bull.png", "Bull", "Bull card text placeholder."),
    CAT_BORED("toys/cat_bored.png", "Cat Bored", "Cat Bored card text placeholder."),
    CAT_CRYING("toys/cat_crying.png", "Cat Crying", "Cat Crying card text placeholder."),
    CAT_EVIL("toys/cat_evil.png", "Cat Evil", "Cat Evil card text placeholder."),
    CAT_FUNNY("toys/cat_funny.png", "Cat Funny", "Cat Funny card text placeholder."),
    CAT_ILL("toys/cat_ill.png", "Cat Ill", "Cat Ill card text placeholder."),
    CAT_NORMAL("toys/cat_normal.png", "Cat Normal", "Cat Normal card text placeholder."),
    COALA("toys/coala.png", "Coala", "Coala card text placeholder."),
    COW("toys/cow.png", "Cow", "Cow card text placeholder."),
    CRABE("toys/crabe.png", "Crabe", "Crabe card text placeholder."),
    CROC("toys/croc.png", "Croc", "Croc card text placeholder."),
    CUTE("toys/cute.png", "Cute", "Cute card text placeholder."),
    DEER("toys/deer.png", "Deer", "Deer card text placeholder."),
    DOG("toys/dog.png", "Dog", "Dog card text placeholder."),
    ELEPHANT("toys/elephant.png", "Elephant", "Elephant card text placeholder."),
    FOXY("toys/foxy.png", "Foxy", "Foxy card text placeholder."),
    HEART("toys/heart.png", "Heart", "Heart card text placeholder."),
    MONKEY("toys/monkey.png", "Monkey", "Monkey card text placeholder."),
    MORGE("toys/morge.png", "Morge", "Morge card text placeholder."),
    MOUSE("toys/mouse.png", "Mouse", "Mouse card text placeholder."),
    PIG("toys/pig.png", "Pig", "Pig card text placeholder."),
    PIGI("toys/pigi.png", "Pigi", "Pigi card text placeholder."),
    PINGUI("toys/pingui.png", "Pingui", "Pingui card text placeholder."),
    PINGUI_SNOW("toys/pingui_snow.png", "Pingui Snow", "Pingui Snow card text placeholder."),
    RABBIT("toys/rabbit.png", "Rabbit", "Rabbit card text placeholder."),
    RABBIT_BIG("toys/rabbit_big.png", "Rabbit Big", "Rabbit Big card text placeholder."),
    RABBIT_LARGE("toys/rabbit_large.png", "Rabbit Large", "Rabbit Large card text placeholder."),
    RACOON("toys/racoon.png", "Racoon", "Racoon card text placeholder."),
    SNAKE("toys/snake.png", "Snake", "Snake card text placeholder."),
    SNOW_BEAR("toys/snow_bear.png", "Snow Bear", "Snow Bear card text placeholder."),
    SQUIRELL("toys/squirell.png", "Squirell", "Squirell card text placeholder."),
    WHALE("toys/whale.png", "Whale", "Whale card text placeholder."),
    ZIRAFFE("toys/ziraffe.png", "Ziraffe", "Ziraffe card text placeholder.");

    /**
     * Пул игрушек для обычной игры.
     * Он оставлен коротким, чтобы не менять текущий баланс базового режима.
     */
    public static final ToyType[] NORMAL_POOL = {
        HEART,
        COALA,
        RABBIT_BIG
    };

    // Путь к изображению игрушки в каталоге assets.
    private final String texturePath;

    // Заголовок карточки для обратной стороны зверинца.
    private final String title;

    // Текст, который показывается на обороте карточки.
    private final String cardText;

    ToyType(String texturePath, String title, String cardText) {
        // Сохраняем путь к картинке игрушки.
        this.texturePath = texturePath;

        // Сохраняем короткий заголовок карточки.
        this.title = title;

        // Сохраняем описание для обратной стороны карточки.
        this.cardText = cardText;
    }

    /**
     * Путь до картинки игрушки в assets.
     */
    public String getTexturePath() {
        return texturePath;
    }

    /**
     * Короткий заголовок карточки.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Текст на обратной стороне карточки.
     */
    public String getCardText() {
        return cardText;
    }
}
