package tools;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Генератор меню-кнопок в едином стиле.
 *
 * Пример:
 *   javac tools/MenuButtonGenerator.java
 *   java tools.MenuButtonGenerator assets "menu_play=Играть" "menu_settings=Настройки"
 */
public final class MenuButtonGenerator {
    private static final int WIDTH = 900;
    private static final int HEIGHT = 140;

    private MenuButtonGenerator() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            printUsage();
            System.exit(1);
        }

        File outputDir = new File(args[0]);
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            throw new IOException("Не удалось создать директорию: " + outputDir.getAbsolutePath());
        }

        Map<String, String> entries = parseEntries(args);
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            BufferedImage image = renderButton(entry.getValue());
            File out = new File(outputDir, entry.getKey() + ".png");
            ImageIO.write(image, "png", out);
            System.out.println("Создано: " + out.getPath());
        }
    }

    private static Map<String, String> parseEntries(String[] args) {
        Map<String, String> entries = new LinkedHashMap<>();
        for (int i = 1; i < args.length; i++) {
            String arg = args[i];
            int idx = arg.indexOf('=');
            if (idx <= 0 || idx == arg.length() - 1) {
                throw new IllegalArgumentException("Аргумент должен быть формата file_name=Текст: " + arg);
            }
            String fileName = arg.substring(0, idx).trim();
            String text = arg.substring(idx + 1).trim();
            entries.put(fileName, text);
        }
        return entries;
    }

    private static BufferedImage renderButton(String text) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            // В оригинальных кнопках фон рисуется в коде, а PNG содержит только текст.
            // Поэтому здесь сохраняем прозрачный фон и рисуем только надпись.
            // Более мягкий и "круглый" системный шрифт.
            Font baseFont = loadBaseFont(58f);
            Map<TextAttribute, Object> attrs = new HashMap<>();
            attrs.put(TextAttribute.KERNING, TextAttribute.KERNING_ON);
            attrs.put(TextAttribute.TRACKING, -0.01f);
            Font font = baseFont.deriveFont(attrs);
            GlyphVector glyphVector = font.createGlyphVector(g.getFontRenderContext(), text);
            Rectangle bounds = glyphVector.getPixelBounds(g.getFontRenderContext(), 0, 0);
            int textX = (WIDTH - bounds.width) / 2;
            int textY = (HEIGHT - bounds.height) / 2 - bounds.y + 1;

            // Тень.
            g.setFont(font);
            g.setColor(new Color(16, 12, 28, 165));
            g.drawString(text, textX + 2, textY + 2);

            // Основной цвет.
            g.setColor(new Color(248, 236, 194, 255));
            g.drawString(text, textX, textY);
        } finally {
            g.dispose();
        }
        return image;
    }

    private static void printUsage() {
        System.out.println("Использование:");
        System.out.println("  javac tools/MenuButtonGenerator.java");
        System.out.println("  java tools.MenuButtonGenerator <output_dir> <file=text> [<file=text> ...]");
        System.out.println("Пример:");
        System.out.println("  java tools.MenuButtonGenerator assets menu_play=Играть menu_settings=Настройки");
    }

    private static Font loadBaseFont(float size) {
        // Пытаемся использовать тот же шрифт, что подключен в проекте.
        File fontFile = new File("assets/fonts/arial.ttf");
        if (!fontFile.exists()) {
            fontFile = new File("fonts/arial.ttf");
        }
        if (fontFile.exists()) {
            try (InputStream in = java.nio.file.Files.newInputStream(fontFile.toPath())) {
                return Font.createFont(Font.TRUETYPE_FONT, in).deriveFont(Font.BOLD, size);
            } catch (Exception ignored) {
                // Если не получилось прочитать TTF, откатимся на системный.
            }
        }
        return new Font("Arial", Font.BOLD, Math.round(size));
    }
}
